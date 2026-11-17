package com.lcms.service;

import com.lcms.model.*;
import com.lcms.model.enums.*;
import com.lcms.repository.LabOrderRepository;
import com.lcms.repository.LabSampleRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OrderService {

    private final LabOrderRepository orderRepository;
    private final LabSampleRepository sampleRepository;
    private final PatientService patientService;
    private final TestCatalogueService catalogueService;
    private final ShiftService shiftService;
    private final AuditService auditService;
    private final AtomicLong sampleSequence = new AtomicLong(1000L);
    private final AtomicLong orderSequence = new AtomicLong(0L);

    public OrderService(LabOrderRepository orderRepository, LabSampleRepository sampleRepository,
                        PatientService patientService, TestCatalogueService catalogueService,
                        ShiftService shiftService, AuditService auditService) {
        this.orderRepository = orderRepository;
        this.sampleRepository = sampleRepository;
        this.patientService = patientService;
        this.catalogueService = catalogueService;
        this.shiftService = shiftService;
        this.auditService = auditService;
    }

    @PostConstruct
    public void initOrderSequence() {
        Long maxOrderId = orderRepository.findMaxOrderId();
        if (maxOrderId != null) {
            orderSequence.set(maxOrderId);
        }
    }

    public List<LabOrder> searchOrders(String query, LocalDate startDate, LocalDate endDate,
                                       PatientSource source, Long doctorId, OrderStatus status) {
        return orderRepository.multiVectorSearch(query, startDate, endDate, source, doctorId, status);
    }

    public Optional<LabOrder> findById(Long id) {
        return orderRepository.findById(id);
    }

    public Optional<LabOrder> findByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }

    public List<LabOrder> getOrdersForPatient(Long patientId) {
        return orderRepository.findByPatientIdOrderByOrderDateDescOrderTimeDesc(patientId);
    }

    @Transactional
    public LabOrder createOrder(
            Long patientId,
            Doctor doctor,
            PatientSource source,
            String centreCode,
            List<Long> selectedTestIds,
            List<CustomTestRequest> customTests,
            BigDecimal discountAmount,
            String discountReason,
            BigDecimal paidAmount,
            PaymentMode paymentMode,
            String paymentReference,
            String bookedBy,
            String clinicalNotes) {

        Patient patient = patientService.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + patientId));

        LocalDate today = LocalDate.now();
        Integer nextToken = getNextDailyToken(today);
        String orderNumber = generateOrderNumber(today);

        LabOrder order = new LabOrder();
        order.setOrderNumber(orderNumber);
        order.setDailyTokenNumber(nextToken);
        order.setOrderDate(today);
        order.setOrderTime(LocalDateTime.now());
        order.setPatient(patient);
        order.setDoctor(doctor);
        order.setSource(source);
        order.setCentreCode(centreCode != null && !centreCode.trim().isEmpty() ? centreCode.trim() : source.getPrefix());
        order.setBookedBy(bookedBy);
        order.setClinicalNotes(clinicalNotes);
        order.setStatus(OrderStatus.BOOKED);

        BigDecimal subtotal = BigDecimal.ZERO;
        Map<String, VialGrouping> vialMap = new LinkedHashMap<>();

        // 1. Process Catalogue Tests
        if (selectedTestIds != null) {
            for (Long testId : selectedTestIds) {
                TestItem testItem = catalogueService.findById(testId).orElse(null);
                if (testItem != null) {
                    BigDecimal price = catalogueService.resolvePrice(testItem, source);
                    subtotal = subtotal.add(price);

                    OrderItem orderItem = new OrderItem(
                            order, testItem, testItem.getName(),
                            testItem.getDepartment(), testItem.getDefaultVial(),
                            price, false
                    );
                    order.addItem(orderItem);

                    // Group by Department + VialType for minimal blood draw
                    String groupKey = testItem.getDepartment().name() + "_" + testItem.getDefaultVial().name();
                    vialMap.putIfAbsent(groupKey, new VialGrouping(testItem.getDepartment(), testItem.getDefaultVial()));
                }
            }
        }

        // 2. Process Ad-hoc Custom Tests (Req from Receptionist)
        if (customTests != null) {
            for (var custom : customTests) {
                if (custom.name() != null && !custom.name().trim().isEmpty()) {
                    BigDecimal price = custom.price() != null ? custom.price() : BigDecimal.ZERO;
                    subtotal = subtotal.add(price);

                    OrderItem orderItem = new OrderItem(
                            order, null, custom.name().trim(),
                            custom.department(), custom.vialType(),
                            price, true
                    );
                    order.addItem(orderItem);

                    String groupKey = custom.department().name() + "_" + custom.vialType().name();
                    vialMap.putIfAbsent(groupKey, new VialGrouping(custom.department(), custom.vialType()));
                }
            }
        }

        // Billing calculations
        discountAmount = discountAmount != null ? discountAmount : BigDecimal.ZERO;
        BigDecimal netAmount = subtotal.subtract(discountAmount);
        if (netAmount.compareTo(BigDecimal.ZERO) < 0) {
            netAmount = BigDecimal.ZERO;
        }

        paidAmount = paidAmount != null ? paidAmount : BigDecimal.ZERO;
        BigDecimal dueAmount = netAmount.subtract(paidAmount);
        if (dueAmount.compareTo(BigDecimal.ZERO) < 0) {
            dueAmount = BigDecimal.ZERO;
        }

        order.setSubtotal(subtotal);
        order.setDiscountAmount(discountAmount);
        order.setDiscountReason(discountReason);
        order.setNetAmount(netAmount);
        order.setPaidAmount(paidAmount);
        order.setDueAmount(dueAmount);
        order.setPaymentMode(paymentMode != null ? paymentMode : PaymentMode.CASH);
        order.setPaymentReference(paymentReference);

        if (dueAmount.compareTo(BigDecimal.ZERO) == 0) {
            order.setPaymentStatus(PaymentStatus.PAID);
        } else if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {
            order.setPaymentStatus(PaymentStatus.PARTIAL);
        } else {
            order.setPaymentStatus(PaymentStatus.DUE);
        }

        // 3. Generate Required Lab Samples (Vials)
        for (VialGrouping vg : vialMap.values()) {
            Long sampleSeqId = getNextSampleSequenceId();
            String barcode = String.format("%05d", sampleSeqId);
            LabSample sample = new LabSample(sampleSeqId, barcode, order, patient, vg.department(), vg.vialType());
            order.addSample(sample);
        }

        order.setVerificationToken(UUID.randomUUID().toString().replace("-", ""));
        LabOrder saved = orderRepository.save(order);

        // Record payment in active shift if payment was made
        if (saved.getPaidAmount() != null && saved.getPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
            shiftService.recordPaymentToActiveShift(saved.getPaymentMode(), saved.getPaidAmount());
        }

        auditService.logAction(bookedBy, "ORDER_CREATED", "ORDER", saved.getOrderNumber(),
                String.format("Patient: %s (%s), Amount: ₹%s via %s", patient.getFullName(), patient.getUhid(), saved.getPaidAmount(), saved.getPaymentMode()), null);

        return saved;
    }

    @Transactional
    public void markDispatched(Long orderId, String dispatchedBy, String dispatchedTo) {
        LabOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        order.setStatus(OrderStatus.DISPATCHED);
        order.setDispatchedAt(LocalDateTime.now());
        order.setDispatchedBy(dispatchedBy);
        order.setDispatchedTo(dispatchedTo != null && !dispatchedTo.trim().isEmpty() ? dispatchedTo.trim() : "Patient");
        orderRepository.save(order);
    }

    @Transactional
    public void cancelOrder(Long orderId, String reason) {
        LabOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        // Anti-Fraud Rule (Req #10): Block cancellation once report is printed or verified
        if (order.getStatus() == OrderStatus.VERIFIED || order.getStatus() == OrderStatus.PRINTED || order.getStatus() == OrderStatus.DISPATCHED) {
            throw new IllegalStateException("Security Rule: Order cannot be cancelled after results are verified or printed.");
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setClinicalNotes((order.getClinicalNotes() != null ? order.getClinicalNotes() + " | " : "") + "Cancelled: " + reason);
        orderRepository.save(order);
    }

    private synchronized Integer getNextDailyToken(LocalDate date) {
        Integer maxToken = orderRepository.findMaxTokenForDate(date);
        return maxToken != null ? maxToken + 1 : 1;
    }

    private synchronized Long getNextSampleSequenceId() {
        Long maxId = sampleRepository.findMaxSampleSequenceId();
        if (maxId != null && maxId > sampleSequence.get()) {
            sampleSequence.set(maxId);
        }
        return sampleSequence.incrementAndGet();
    }

    private String generateOrderNumber(LocalDate date) {
        int year = date.getYear() % 100;
        long nextOrder = orderSequence.incrementAndGet();
        return String.format("ORD-%02d-%05d", year, nextOrder);
    }

    public record CustomTestRequest(String name, Department department, VialType vialType, BigDecimal price) {}
    private record VialGrouping(Department department, VialType vialType) {}
}
