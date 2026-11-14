package com.lcms.service;

import com.lcms.model.LabOrder;
import com.lcms.model.TestResult;
import com.lcms.model.enums.OrderStatus;
import com.lcms.repository.LabOrderRepository;
import com.lcms.repository.TestResultRepository;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ReportService {

    private final LabOrderRepository orderRepository;
    private final TestResultRepository testResultRepository;
    private final BarcodeService barcodeService;
    private final AuditService auditService;

    public ReportService(LabOrderRepository orderRepository,
                         TestResultRepository testResultRepository,
                         BarcodeService barcodeService,
                         AuditService auditService) {
        this.orderRepository = orderRepository;
        this.testResultRepository = testResultRepository;
        this.barcodeService = barcodeService;
        this.auditService = auditService;
    }

    public List<LabOrder> getOrdersForReporting() {
        return orderRepository.findByStatusInOrderByOrderDateDescOrderTimeDesc(
                List.of(OrderStatus.VERIFIED, OrderStatus.PRINTED, OrderStatus.DISPATCHED)
        );
    }

    public LabOrder getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
    }

    public LabOrder getOrderByVerificationToken(String token) {
        return orderRepository.findByVerificationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Report not found or invalid token: " + token));
    }

    public List<TestResult> getResultsForOrder(Long orderId) {
        return testResultRepository.findByOrderIdOrderByDepartmentAscSortOrderAsc(orderId);
    }

    public String generateQrCodeForReport(String token, String hostAddress) {
        String url = (hostAddress != null ? hostAddress : "http://localhost:8080") + "/v/" + token;
        return barcodeService.generateQrCodeBase64(url, 200, 200);
    }

    @Transactional
    public void markAsPrinted(Long orderId, String printedBy) {
        LabOrder order = getOrderById(orderId);
        if (order.getStatus() == OrderStatus.VERIFIED) {
            order.setStatus(OrderStatus.PRINTED);
            orderRepository.save(order);
            auditService.logAction(printedBy, "REPORT_PRINTED", "ORDER", order.getOrderNumber(),
                    "Report printed by " + printedBy, null);
        }
    }

    @Transactional
    public void dispatchReport(Long orderId, String dispatchedBy, String recipientInfo) {
        LabOrder order = getOrderById(orderId);
        order.setStatus(OrderStatus.DISPATCHED);
        order.setDispatchedBy(dispatchedBy);
        order.setDispatchedTo(recipientInfo);
        order.setDispatchedAt(LocalDateTime.now());
        orderRepository.save(order);

        auditService.logAction(dispatchedBy, "REPORT_DISPATCHED", "ORDER", order.getOrderNumber(),
                "Handed over to: " + recipientInfo, null);
    }

    public String buildWhatsAppUrl(LabOrder order, String hostAddress) {
        String phone = order.getPatient().getPhone();
        if (phone == null || phone.isBlank()) {
            return null;
        }
        // Normalize Indian phone number
        phone = phone.replaceAll("[^0-9]", "");
        if (phone.length() == 10) {
            phone = "91" + phone;
        }

        String verifyUrl = (hostAddress != null ? hostAddress : "http://localhost:8080") + "/v/" + order.getVerificationToken();
        String message = String.format("Dear %s,\nYour diagnostic test report for Order #%s is ready and verified at LCMS Diagnostic Centre.\n\nYou can view and verify your authentic report online:\n%s\n\nThank you for choosing LCMS.",
                order.getPatient().getFullName(), order.getOrderNumber(), verifyUrl);

        return "https://wa.me/" + phone + "?text=" + URLEncoder.encode(message, StandardCharsets.UTF_8);
    }

    public byte[] generatePdfReport(String htmlContent) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(htmlContent, "/");
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to render PDF: " + e.getMessage(), e);
        }
    }
}
