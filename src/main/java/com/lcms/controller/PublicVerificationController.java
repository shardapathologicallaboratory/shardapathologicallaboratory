package com.lcms.controller;

import com.lcms.model.LabOrder;
import com.lcms.model.Patient;
import com.lcms.service.ReportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PublicVerificationController {

    private final ReportService reportService;

    public PublicVerificationController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/v/{token}")
    public String verifyReport(@PathVariable String token, Model model) {
        try {
            LabOrder order = reportService.getOrderByVerificationToken(token);
            Patient patient = order.getPatient();

            // Mask patient name for medical privacy (e.g. "Aman Kumar" -> "A*** K****")
            String maskedName = maskPatientName(patient.getFullName());

            model.addAttribute("order", order);
            model.addAttribute("patient", patient);
            model.addAttribute("maskedName", maskedName);
            model.addAttribute("isValid", true);
            model.addAttribute("pageTitle", "Authentic Report Verification");
        } catch (Exception e) {
            model.addAttribute("isValid", false);
            model.addAttribute("errorMessage", "Invalid, expired, or unrecognized verification token.");
            model.addAttribute("pageTitle", "Verification Unsuccessful");
        }
        return "reporting/public_verification";
    }

    private String maskPatientName(String fullName) {
        if (fullName == null || fullName.isBlank()) return "Anonymous";
        String[] parts = fullName.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String p = parts[i];
            if (p.length() <= 1) {
                sb.append(p).append("*");
            } else {
                sb.append(p.charAt(0))
                  .append("*".repeat(Math.min(p.length() - 1, 4)));
                if (p.length() > 2) {
                    sb.append(p.charAt(p.length() - 1));
                }
            }
            if (i < parts.length - 1) sb.append(" ");
        }
        return sb.toString();
    }
}
