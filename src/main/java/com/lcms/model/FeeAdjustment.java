package com.lcms.model;

import com.lcms.model.enums.PaymentMode;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "lcms_fee_adjustments", indexes = {
    @Index(name = "idx_adj_voucher", columnList = "voucherNumber", unique = true),
    @Index(name = "idx_adj_order", columnList = "order_id")
})
public class FeeAdjustment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String voucherNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private LabOrder order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(nullable = false, length = 30)
    private String adjustmentType; // REFUND_OVERCHARGE, SUPPLEMENT_UNDERCHARGE

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal originalAmount;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal correctedAmount;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal differenceAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMode paymentMode = PaymentMode.CASH;

    @Column(nullable = false, length = 500)
    private String reason;

    @Column(nullable = false, length = 100)
    private String authorizedBy;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public FeeAdjustment() {}

    public FeeAdjustment(String voucherNumber, LabOrder order, Patient patient, String adjustmentType,
                         BigDecimal originalAmount, BigDecimal correctedAmount, BigDecimal differenceAmount,
                         PaymentMode paymentMode, String reason, String authorizedBy) {
        this.voucherNumber = voucherNumber;
        this.order = order;
        this.patient = patient;
        this.adjustmentType = adjustmentType;
        this.originalAmount = originalAmount;
        this.correctedAmount = correctedAmount;
        this.differenceAmount = differenceAmount;
        this.paymentMode = paymentMode;
        this.reason = reason;
        this.authorizedBy = authorizedBy;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVoucherNumber() {
        return voucherNumber;
    }

    public void setVoucherNumber(String voucherNumber) {
        this.voucherNumber = voucherNumber;
    }

    public LabOrder getOrder() {
        return order;
    }

    public void setOrder(LabOrder order) {
        this.order = order;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public String getAdjustmentType() {
        return adjustmentType;
    }

    public void setAdjustmentType(String adjustmentType) {
        this.adjustmentType = adjustmentType;
    }

    public BigDecimal getOriginalAmount() {
        return originalAmount;
    }

    public void setOriginalAmount(BigDecimal originalAmount) {
        this.originalAmount = originalAmount;
    }

    public BigDecimal getCorrectedAmount() {
        return correctedAmount;
    }

    public void setCorrectedAmount(BigDecimal correctedAmount) {
        this.correctedAmount = correctedAmount;
    }

    public BigDecimal getDifferenceAmount() {
        return differenceAmount;
    }

    public void setDifferenceAmount(BigDecimal differenceAmount) {
        this.differenceAmount = differenceAmount;
    }

    public PaymentMode getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(PaymentMode paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getAuthorizedBy() {
        return authorizedBy;
    }

    public void setAuthorizedBy(String authorizedBy) {
        this.authorizedBy = authorizedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
