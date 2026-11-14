package com.lcms.service;

import com.lcms.model.FeeAdjustment;
import com.lcms.model.LabOrder;
import com.lcms.model.ShiftSession;
import com.lcms.model.User;
import com.lcms.model.enums.PaymentMode;
import com.lcms.repository.FeeAdjustmentRepository;
import com.lcms.repository.LabOrderRepository;
import com.lcms.repository.ShiftSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ShiftService {

    private final ShiftSessionRepository shiftRepository;
    private final FeeAdjustmentRepository adjustmentRepository;
    private final LabOrderRepository orderRepository;
    private final AuditService auditService;
    private final AtomicLong voucherSeq = new AtomicLong(1001);

    public ShiftService(ShiftSessionRepository shiftRepository,
                        FeeAdjustmentRepository adjustmentRepository,
                        LabOrderRepository orderRepository,
                        AuditService auditService) {
        this.shiftRepository = shiftRepository;
        this.adjustmentRepository = adjustmentRepository;
        this.orderRepository = orderRepository;
        this.auditService = auditService;
    }

    public Optional<ShiftSession> getCurrentShift(Long userId) {
        return shiftRepository.findTopByUserIdAndStatusOrderByStartTimeDesc(userId, "OPEN");
    }

    public List<ShiftSession> getAllShifts() {
        return shiftRepository.findAllByOrderByStartTimeDesc();
    }

    public List<FeeAdjustment> getRecentAdjustments() {
        return adjustmentRepository.findTop100ByOrderByCreatedAtDesc();
    }

    @Transactional
    public ShiftSession openShift(User user, BigDecimal openingFloat) {
        // Close any lingering open shift for this user first
        shiftRepository.findTopByUserIdAndStatusOrderByStartTimeDesc(user.getId(), "OPEN")
                .ifPresent(s -> {
                    s.setStatus("CLOSED");
                    s.setEndTime(LocalDateTime.now());
                    s.setClosingNotes("Auto-closed upon new shift opening");
                    shiftRepository.save(s);
                });

        String code = "SH-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd")) + "-" + (shiftRepository.countTodayShifts() + 1);
        BigDecimal floatAmt = openingFloat != null ? openingFloat : BigDecimal.ZERO;
        ShiftSession session = new ShiftSession(code, user, floatAmt);
        shiftRepository.save(session);

        auditService.logAction(user.getUsername(), "SHIFT_OPENED", "SHIFT", code,
                "Shift opened with float ₹" + floatAmt, null);
        return session;
    }

    @Transactional
    public void recordPaymentToActiveShift(PaymentMode mode, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) return;

        shiftRepository.findTopByStatusOrderByStartTimeDesc("OPEN").ifPresent(shift -> {
            if (mode == PaymentMode.CASH) {
                shift.setTotalCashBilled(shift.getTotalCashBilled().add(amount));
                shift.setExpectedCashInDrawer(shift.getExpectedCashInDrawer().add(amount));
            } else if (mode == PaymentMode.UPI) {
                shift.setTotalUpiBilled(shift.getTotalUpiBilled().add(amount));
            } else if (mode == PaymentMode.CARD) {
                shift.setTotalCardBilled(shift.getTotalCardBilled().add(amount));
            }
            shiftRepository.save(shift);
        });
    }

    @Transactional
    public ShiftSession closeShift(Long shiftId, BigDecimal actualCash, String notes, String closedBy) {
        ShiftSession shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new IllegalArgumentException("Shift not found: " + shiftId));

        shift.setStatus("CLOSED");
        shift.setEndTime(LocalDateTime.now());
        shift.setActualCashCounted(actualCash != null ? actualCash : BigDecimal.ZERO);
        shift.setClosingNotes(notes);
        shift.setClosedBy(closedBy);

        // Variance = actual cash - expected cash
        BigDecimal variance = shift.getActualCashCounted().subtract(shift.getExpectedCashInDrawer());
        shift.setCashVariance(variance);

        shiftRepository.save(shift);

        auditService.logAction(closedBy, "SHIFT_CLOSED", "SHIFT", shift.getSessionCode(),
                String.format("Shift closed. Expected: ₹%s, Actual: ₹%s, Variance: ₹%s",
                        shift.getExpectedCashInDrawer(), shift.getActualCashCounted(), variance), null);
        return shift;
    }

    @Transactional
    public FeeAdjustment adjustFee(Long orderId, String adjustmentType, BigDecimal correctedAmount,
                                  PaymentMode paymentMode, String reason, String authorizedBy) {
        LabOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        BigDecimal originalAmount = order.getPaidAmount();
        BigDecimal difference = correctedAmount.subtract(originalAmount).abs();

        if ("REFUND_OVERCHARGE".equals(adjustmentType)) {
            // Reception charged too much; refund the difference
            order.setNetAmount(correctedAmount);
            order.setPaidAmount(correctedAmount);
            order.setDueAmount(BigDecimal.ZERO);

            // Deduct from shift expected cash
            shiftRepository.findTopByStatusOrderByStartTimeDesc("OPEN").ifPresent(shift -> {
                shift.setTotalRefunds(shift.getTotalRefunds().add(difference));
                if (paymentMode == PaymentMode.CASH) {
                    shift.setExpectedCashInDrawer(shift.getExpectedCashInDrawer().subtract(difference));
                }
                shiftRepository.save(shift);
            });
        } else {
            // Reception charged too little; collect additional supplement
            order.setNetAmount(correctedAmount);
            order.setPaidAmount(correctedAmount);
            order.setDueAmount(BigDecimal.ZERO);

            recordPaymentToActiveShift(paymentMode, difference);
        }

        orderRepository.save(order);

        String voucherNo = "ADJ-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd")) + "-" + voucherSeq.incrementAndGet();
        FeeAdjustment adjustment = new FeeAdjustment(voucherNo, order, order.getPatient(), adjustmentType,
                originalAmount, correctedAmount, difference, paymentMode, reason, authorizedBy);
        adjustmentRepository.save(adjustment);

        auditService.logAction(authorizedBy, "FEE_ADJUSTED", "ORDER", order.getOrderNumber(),
                String.format("Fee adjustment voucher %s: %s (Original: ₹%s, Corrected: ₹%s, Diff: ₹%s). Reason: %s",
                        voucherNo, adjustmentType, originalAmount, correctedAmount, difference, reason), null);

        return adjustment;
    }
}
