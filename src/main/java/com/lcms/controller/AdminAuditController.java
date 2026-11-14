package com.lcms.controller;

import com.lcms.model.AuditLog;
import com.lcms.service.AuditService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class AdminAuditController {

    private final AuditService auditService;

    public AdminAuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/admin/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public String auditLogView(Model model) {
        List<AuditLog> logs = auditService.getRecentLogs();
        model.addAttribute("logs", logs);
        model.addAttribute("pageTitle", "Cryptographic Audit Trail & Chain of Custody");
        return "admin/audit_chain";
    }

    @PostMapping("/admin/audit/verify-chain")
    @PreAuthorize("hasRole('ADMIN')")
    public String verifyChain(RedirectAttributes redirectAttributes) {
        AuditService.AuditIntegrityResult result = auditService.verifyChainIntegrity();
        if (result.valid()) {
            redirectAttributes.addFlashAttribute("successMessage",
                    "✅ VERIFICATION PASSED: " + result.message());
        } else {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "❌ SECURITY WARNING: " + result.message());
        }
        return "redirect:/admin/audit";
    }
}
