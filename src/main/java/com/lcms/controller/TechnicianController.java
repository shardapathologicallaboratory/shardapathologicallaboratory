package com.lcms.controller;

import com.lcms.model.LabOrder;
import com.lcms.model.RecheckFlag;
import com.lcms.model.TestResult;
import com.lcms.model.User;
import com.lcms.model.enums.Department;
import com.lcms.model.enums.Role;
import com.lcms.security.CustomUserDetails;
import com.lcms.service.ClinicalWorksheetService;
import com.lcms.service.OrderService;
import com.lcms.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequestMapping("/technician")
@PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'REPORTING')")
public class TechnicianController {

    private final ClinicalWorksheetService worksheetService;
    private final OrderService orderService;
    private final UserService userService;

    public TechnicianController(ClinicalWorksheetService worksheetService, OrderService orderService, UserService userService) {
        this.worksheetService = worksheetService;
        this.orderService = orderService;
        this.userService = userService;
    }

    @GetMapping
    public String technicianDashboard(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "department", required = false) Department department,
            Model model) {

        User user = userService.findByUsername(userDetails.getUsername()).orElse(userDetails.getUser());
        List<LabOrder> activeOrders = orderService.searchOrders(null, null, null, null, null, null);

        // Fetch unresolved recheck flags if user is technician
        List<RecheckFlag> myDeptFlags = new ArrayList<>();
        if (!user.getAllowedDepartments().isEmpty()) {
            for (Department d : user.getAllowedDepartments()) {
                myDeptFlags.addAll(worksheetService.getUnresolvedFlagsForDepartment(d));
            }
        } else if (user.getRole() == Role.ROLE_ADMIN) {
            for (Department d : Department.values()) {
                myDeptFlags.addAll(worksheetService.getUnresolvedFlagsForDepartment(d));
            }
        }

        model.addAttribute("currentUser", user);
        model.addAttribute("orders", activeOrders);
        model.addAttribute("selectedDept", department);
        model.addAttribute("departments", Department.values());
        model.addAttribute("myDeptFlags", myDeptFlags);

        return "technician/dashboard";
    }

    @GetMapping("/worksheet/{orderId}")
    public String openWorksheet(
            @PathVariable("orderId") Long orderId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        User user = userService.findByUsername(userDetails.getUsername()).orElse(userDetails.getUser());
        LabOrder order = orderService.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        List<TestResult> results = worksheetService.getOrInitializeResults(order);
        List<RecheckFlag> flags = worksheetService.getFlagsForOrder(orderId);

        boolean isLabAdmin = user.getRole() == Role.ROLE_ADMIN;
        Set<Department> allowedDepts = user.getAllowedDepartments();

        model.addAttribute("order", order);
        model.addAttribute("patient", order.getPatient());
        model.addAttribute("results", results);
        model.addAttribute("flags", flags);
        model.addAttribute("currentUser", user);
        model.addAttribute("isLabAdmin", isLabAdmin);
        model.addAttribute("allowedDepts", allowedDepts);
        model.addAttribute("allDepartments", Department.values());

        return "technician/worksheet";
    }

    @PostMapping("/worksheet/{orderId}/save")
    public String saveWorksheet(
            @PathVariable("orderId") Long orderId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Map<String, String> allParams,
            RedirectAttributes redirectAttributes) {

        try {
            User user = userService.findByUsername(userDetails.getUsername()).orElse(userDetails.getUser());
            boolean isLabAdmin = user.getRole() == Role.ROLE_ADMIN;

            Map<Long, String> values = new HashMap<>();
            Map<Long, String> comments = new HashMap<>();

            for (var entry : allParams.entrySet()) {
                if (entry.getKey().startsWith("val_")) {
                    Long resId = Long.parseLong(entry.getKey().substring(4));
                    values.put(resId, entry.getValue());
                } else if (entry.getKey().startsWith("cmt_")) {
                    Long resId = Long.parseLong(entry.getKey().substring(4));
                    comments.put(resId, entry.getValue());
                }
            }

            worksheetService.saveWorksheetResults(
                    orderId, values, comments,
                    userDetails.getFullName(), user.getAllowedDepartments(), isLabAdmin
            );

            redirectAttributes.addFlashAttribute("successMessage", "Results saved and automated calculations updated successfully!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to save: " + ex.getMessage());
        }

        return "redirect:/technician/worksheet/" + orderId;
    }

    @PostMapping("/worksheet/{orderId}/raise-flag")
    public String raiseFlag(
            @PathVariable("orderId") Long orderId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("sourceDept") Department sourceDept,
            @RequestParam("targetDept") Department targetDept,
            @RequestParam("reason") String reason,
            RedirectAttributes redirectAttributes) {

        try {
            worksheetService.raiseRecheckFlag(orderId, sourceDept, targetDept, userDetails.getFullName(), reason);
            redirectAttributes.addFlashAttribute("successMessage", "Recheck Flag raised to " + targetDept.getDisplayName() + " successfully!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/technician/worksheet/" + orderId;
    }

    @PostMapping("/flags/{flagId}/resolve")
    public String resolveFlag(
            @PathVariable("flagId") Long flagId,
            @RequestParam("orderId") Long orderId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("resolutionNote") String resolutionNote,
            RedirectAttributes redirectAttributes) {

        try {
            worksheetService.resolveRecheckFlag(flagId, userDetails.getFullName(), resolutionNote);
            redirectAttributes.addFlashAttribute("successMessage", "Recheck flag resolved with note: " + resolutionNote);
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/technician/worksheet/" + orderId;
    }

    @PostMapping("/worksheet/{orderId}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public String verifyOrder(
            @PathVariable("orderId") Long orderId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            worksheetService.verifyAllResultsForOrder(orderId, userDetails.getFullName());
            redirectAttributes.addFlashAttribute("successMessage", "Order #" + orderId + " has been officially VERIFIED by Pathologist. Ready for report printing!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/technician/worksheet/" + orderId;
    }
}
