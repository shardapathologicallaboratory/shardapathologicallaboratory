package com.lcms.model;

import com.lcms.model.enums.Department;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "lcms_recheck_flags")
public class RecheckFlag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private LabOrder order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Department sourceDepartment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Department targetDepartment;

    @Column(nullable = false, length = 100)
    private String raisedBy;

    @Column(nullable = false, length = 500)
    private String reason;

    private boolean resolved = false;

    @Column(length = 500)
    private String resolutionNote;

    @Column(length = 100)
    private String resolvedBy;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime resolvedAt;

    public RecheckFlag() {}

    public RecheckFlag(LabOrder order, Department sourceDepartment, Department targetDepartment, String raisedBy, String reason) {
        this.order = order;
        this.sourceDepartment = sourceDepartment;
        this.targetDepartment = targetDepartment;
        this.raisedBy = raisedBy;
        this.reason = reason;
        this.resolved = false;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LabOrder getOrder() {
        return order;
    }

    public void setOrder(LabOrder order) {
        this.order = order;
    }

    public Department getSourceDepartment() {
        return sourceDepartment;
    }

    public void setSourceDepartment(Department sourceDepartment) {
        this.sourceDepartment = sourceDepartment;
    }

    public Department getTargetDepartment() {
        return targetDepartment;
    }

    public void setTargetDepartment(Department targetDepartment) {
        this.targetDepartment = targetDepartment;
    }

    public String getRaisedBy() {
        return raisedBy;
    }

    public void setRaisedBy(String raisedBy) {
        this.raisedBy = raisedBy;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    public String getResolutionNote() {
        return resolutionNote;
    }

    public void setResolutionNote(String resolutionNote) {
        this.resolutionNote = resolutionNote;
    }

    public String getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(String resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}
