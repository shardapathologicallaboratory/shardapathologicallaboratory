package com.lcms.controller;

import com.lcms.model.FeeAdjustment;
import com.lcms.model.LabOrder;
import com.lcms.model.ShiftSession;
import com.lcms.model.User;
import com.lcms.model.enums.PaymentMode;
import com.lcms.security.CustomUserDetails;
import com.lcms.service.OrderService;
import com.lcms.service.ShiftService;
import com.lcms.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Controller
public class AccountsController {

    private final ShiftService shiftService;
    private final OrderService orderService;
    private final UserService userService;

    public AccountsController(ShiftService shiftService, OrderService orderService, UserService userService) {
        this.shiftService = shiftService;
        this.orderService = orderService;
        this.userService = userService;
    }

    @GetMapping("/accountant")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'RECEPTION')")
    public String accountsDashboard(Model model, @AuthenticationPrincipal CustomUserDetails authUser) {
        User user = userService.findByUsername(authUser.getUsername()).orElse(null);
        Optional<ShiftSession> activeShift = user != null ? shiftService.getCurrentShift(user.getId()) : Optional.empty();

        List<ShiftSession> allShifts = shiftService.getAllShifts();
        List<FeeAdjustment> adjustments = shiftService.getRecentAdjustments();

        model.addAttribute("activeShift", activeShift.orElse(null));
        model.addAttribute("allShifts", allShifts);
        model.addAttribute("adjustments", adjustments);
        model.addAttribute("pageTitle", "Accounts & Shift Cash Handover");
        return "accounts/shift_dashboard";
    }

    @PostMapping("/accountant/shift/open")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'RECEPTION')")
    public String openShift(@RequestParam(defaultValue = "0.00") BigDecimal openingFloat,
                            @AuthenticationPrincipal CustomUserDetails authUser,
                            RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(authUser.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        ShiftSession session = shiftService.openShift(user, openingFloat);
        redirectAttributes.addFlashAttribute("successMessage", "Shift " + session.getSessionCode() + " successfully opened with starting float ₹" + openingFloat);
        return "redirect:/accountant";
    }

    @PostMapping("/accountant/shift/close")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'RECEPTION')")
    public String closeShift(@RequestParam Long shiftId,
                             @RequestParam BigDecimal actualCash,
                             @RequestParam(required = false) String closingNotes,
                             @AuthenticationPrincipal CustomUserDetails authUser,
                             RedirectAttributes redirectAttributes) {
        ShiftSession session = shiftService.closeShift(shiftId, actualCash, closingNotes, authUser.getFullName());
        redirectAttributes.addFlashAttribute("successMessage",
                String.format("Shift %s closed! Expected: ₹%s | Counted: ₹%s | Variance: ₹%s",
                        session.getSessionCode(), session.getExpectedCashInDrawer(), actualCash, session.getCashVariance()));
        return "redirect:/accountant";
    }

    @PostMapping("/accountant/adjust-fee")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'RECEPTION')")
    public String adjustFee(@RequestParam Long orderId,
                            @RequestParam String adjustmentType,
                            @RequestParam BigDecimal correctedAmount,
                            @RequestParam(defaultValue = "CASH") PaymentMode paymentMode,
                            @RequestParam String reason,
                            @AuthenticationPrincipal CustomUserDetails authUser,
                            RedirectAttributes redirectAttributes) {
        try {
            FeeAdjustment adj = shiftService.adjustFee(orderId, adjustmentType, correctedAmount, paymentMode, reason, authUser.getFullName());
            redirectAttributes.addFlashAttribute("successMessage",
                    String.format("Fee adjustment voucher %s issued! %s difference of ₹%s processed.",
                            adj.getVoucherNumber(),
                            adj.getAdjustmentType().equals("REFUND_OVERCHARGE") ? "Refund" : "Additional payment",
                            adj.getDifferenceAmount()));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Adjustment failed: " + e.getMessage());
        }
        return "redirect:/accountant";
    }

    @PostMapping("/reception/orders/{orderId}/adjust-fee")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION', 'ACCOUNTANT')")
    public String adjustFeeFromReception(@PathVariable Long orderId,
                                         @RequestParam String adjustmentType,
                                         @RequestParam BigDecimal correctedAmount,
                                         @RequestParam(defaultValue = "CASH") PaymentMode paymentMode,
                                         @RequestParam String reason,
                                         @AuthenticationPrincipal CustomUserDetails authUser,
                                         RedirectAttributes redirectAttributes) {
        try {
            FeeAdjustment adj = shiftService.adjustFee(orderId, adjustmentType, correctedAmount, paymentMode, reason, authUser.getFullName());
            redirectAttributes.addFlashAttribute("successMessage",
                    String.format("Fee adjustment voucher %s issued! %s difference of ₹%s processed.",
                            adj.getVoucherNumber(),
                            adj.getAdjustmentType().equals("REFUND_OVERCHARGE") ? "Refund" : "Additional payment",
                            adj.getDifferenceAmount()));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Adjustment failed: " + e.getMessage());
        }
        return "redirect:/patients/search";
    }
}
