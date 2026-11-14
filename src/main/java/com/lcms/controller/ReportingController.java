package com.lcms.controller;

import com.lcms.model.LabOrder;
import com.lcms.model.TestResult;
import com.lcms.model.enums.Department;
import com.lcms.security.CustomUserDetails;
import com.lcms.service.ClinicalWorksheetService;
import com.lcms.service.ReportService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class ReportingController {

    private final ReportService reportService;
    private final ClinicalWorksheetService worksheetService;

    public ReportingController(ReportService reportService, ClinicalWorksheetService worksheetService) {
        this.reportService = reportService;
        this.worksheetService = worksheetService;
    }

    @GetMapping("/reporting")
    public String reportingDesk(Model model, HttpServletRequest request) {
        List<LabOrder> orders = reportService.getOrdersForReporting();
        String hostUrl = getBaseUrl(request);

        Map<Long, String> whatsAppMap = new HashMap<>();
        for (LabOrder o : orders) {
            String wa = reportService.buildWhatsAppUrl(o, hostUrl);
            if (wa != null) whatsAppMap.put(o.getId(), wa);
        }

        model.addAttribute("orders", orders);
        model.addAttribute("whatsAppMap", whatsAppMap);
        model.addAttribute("pageTitle", "Reporting & Dispatch Desk");
        return "reporting/dashboard";
    }

    @GetMapping("/reports/{id}/view")
    public String viewReport(@PathVariable Long id, Model model,
                             @AuthenticationPrincipal CustomUserDetails user,
                             HttpServletRequest request) {
        LabOrder order = reportService.getOrderById(id);
        List<TestResult> results = reportService.getResultsForOrder(id);

        if (user != null) {
            reportService.markAsPrinted(id, user.getFullName());
        }

        String hostUrl = getBaseUrl(request);
        String qrCodeBase64 = reportService.generateQrCodeForReport(order.getVerificationToken(), hostUrl);

        // Group results by Department
        Map<Department, List<TestResult>> groupedResults = results.stream()
                .collect(Collectors.groupingBy(TestResult::getDepartment, LinkedHashMap::new, Collectors.toList()));

        model.addAttribute("order", order);
        model.addAttribute("groupedResults", groupedResults);
        model.addAttribute("qrCodeBase64", qrCodeBase64);
        model.addAttribute("pageTitle", "Report #" + order.getOrderNumber() + " - " + order.getPatient().getFullName());
        return "reporting/view_report";
    }

    @PostMapping("/reporting/{id}/dispatch")
    public String dispatchReport(@PathVariable Long id,
                                 @RequestParam String recipientInfo,
                                 @AuthenticationPrincipal CustomUserDetails user,
                                 RedirectAttributes redirectAttributes) {
        String actor = user != null ? user.getFullName() : "Reporting Desk";
        reportService.dispatchReport(id, actor, recipientInfo);
        redirectAttributes.addFlashAttribute("successMessage", "Report dispatched and handed over to " + recipientInfo);
        return "redirect:/reporting";
    }

    @PostMapping("/reporting/{id}/amend")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    public String amendReport(@PathVariable Long id,
                              @RequestParam String amendmentReason,
                              @AuthenticationPrincipal CustomUserDetails user,
                              RedirectAttributes redirectAttributes) {
        String actor = user != null ? user.getFullName() : "Pathologist";
        worksheetService.amendReport(id, actor, amendmentReason);
        redirectAttributes.addFlashAttribute("successMessage", "Report successfully amended. New Version issued.");
        return "redirect:/reports/" + id + "/view";
    }

    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        if ((scheme.equals("http") && serverPort == 80) || (scheme.equals("https") && serverPort == 443)) {
            return scheme + "://" + serverName;
        }
        return scheme + "://" + serverName + ":" + serverPort;
    }
}
