package com.lcms.service;

import com.lcms.model.LabOrder;
import com.lcms.model.LabSample;
import com.lcms.model.OrderItem;
import com.lcms.model.enums.Department;
import com.lcms.model.enums.OrderStatus;
import com.lcms.model.enums.SampleStatus;
import com.lcms.model.enums.VialType;
import com.lcms.repository.LabOrderRepository;
import com.lcms.repository.LabSampleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CollectionService {

    private final LabSampleRepository sampleRepository;
    private final LabOrderRepository orderRepository;

    public CollectionService(LabSampleRepository sampleRepository, LabOrderRepository orderRepository) {
        this.sampleRepository = sampleRepository;
        this.orderRepository = orderRepository;
    }

    public List<LabSample> getPendingSamples() {
        return sampleRepository.findByStatusOrderBySampleSequenceIdAsc(SampleStatus.PENDING_COLLECTION);
    }

    public List<LabSample> getRecentlyCollectedSamples() {
        return sampleRepository.findTop30ByStatusOrderByCollectedAtDesc(SampleStatus.COLLECTED);
    }

    public List<LabSample> getRecollectionAlerts() {
        return sampleRepository.findRecollectionAlerts();
    }

    public Optional<LabSample> findById(Long id) {
        return sampleRepository.findById(id);
    }

    @Transactional
    public LabSample markSampleCollected(Long sampleId, String collectedBy) {
        return markSampleCollectedWithVial(sampleId, null, collectedBy);
    }

    @Transactional
    public LabSample markSampleCollectedWithVial(Long sampleId, VialType selectedVial, String collectedBy) {
        LabSample sample = sampleRepository.findById(sampleId)
                .orElseThrow(() -> new IllegalArgumentException("Sample not found: " + sampleId));

        if (selectedVial != null) {
            sample.setVialType(selectedVial);
        }
        sample.setStatus(SampleStatus.COLLECTED);
        sample.setCollectedAt(LocalDateTime.now());
        sample.setCollectedBy(collectedBy);
        LabSample saved = sampleRepository.save(sample);

        // Check if all samples for this order are collected
        LabOrder order = sample.getOrder();
        boolean allCollected = order.getSamples().stream()
                .allMatch(s -> s.getStatus() != SampleStatus.PENDING_COLLECTION);

        if (allCollected && order.getStatus() == OrderStatus.BOOKED) {
            order.setStatus(OrderStatus.COLLECTED);
            orderRepository.save(order);
        }

        return saved;
    }

    @Transactional
    public void markAllSamplesCollectedForOrder(Long orderId, String collectedBy) {
        LabOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        LocalDateTime now = LocalDateTime.now();
        for (LabSample s : order.getSamples()) {
            if (s.getStatus() == SampleStatus.PENDING_COLLECTION) {
                s.setStatus(SampleStatus.COLLECTED);
                s.setCollectedAt(now);
                s.setCollectedBy(collectedBy);
                sampleRepository.save(s);
            }
        }
        order.setStatus(OrderStatus.COLLECTED);
        orderRepository.save(order);
    }

    public List<LabSheetRow> generateLabRunSheet(Department department, LocalDateTime startTime, LocalDateTime endTime) {
        List<LabSample> samples = sampleRepository.findLabRunSheetSamples(department, startTime, endTime);
        List<LabSheetRow> rows = new ArrayList<>();
        int sNo = 1;

        for (LabSample s : samples) {
            LabOrder order = s.getOrder();

            // Find matching tests in this order that belong to this department
            List<String> testsForDept = order.getItems().stream()
                    .filter(i -> i.getDepartment() == s.getDepartment())
                    .map(OrderItem::getTestName)
                    .collect(Collectors.toList());

            String testNames = String.join(", ", testsForDept);
            if (testNames.isEmpty()) {
                testNames = s.getDepartment().getDisplayName() + " Panel";
            }

            String dateStr = s.getCollectedAt() != null
                    ? s.getCollectedAt().format(DateTimeFormatter.ofPattern("dd-MMM"))
                    : order.getOrderDate().format(DateTimeFormatter.ofPattern("dd-MMM"));

            rows.add(new LabSheetRow(
                    sNo++,
                    s.getSampleSequenceId(),
                    s.getVialType().getDisplayName(),
                    dateStr,
                    order.getCentreCode(),
                    order.getPatient().getFullName() + " (" + order.getPatient().getFormattedAgeAndSex() + ")",
                    order.getDoctor().getName() + (order.getDoctor().getQualification() != null && !order.getDoctor().getQualification().equals("-") ? ", " + order.getDoctor().getQualification() : ""),
                    testNames,
                    s.getStatus().getDisplayName()
            ));
        }

        return rows;
    }

    public record LabSheetRow(
            int sNo,
            Long sampleId,
            String sampleVial,
            String date,
            String centreNo,
            String patient,
            String doctor,
            String test,
            String currentStatus
    ) {}
}
