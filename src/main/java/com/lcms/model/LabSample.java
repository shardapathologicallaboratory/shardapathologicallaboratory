package com.lcms.model;

import com.lcms.model.enums.Department;
import com.lcms.model.enums.SampleStatus;
import com.lcms.model.enums.VialType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "lcms_lab_samples", indexes = {
    @Index(name = "idx_sample_seq_id", columnList = "sampleSequenceId", unique = true),
    @Index(name = "idx_sample_barcode", columnList = "barcode")
})
public class LabSample {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long sampleSequenceId;

    @Column(nullable = false, length = 50)
    private String barcode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private LabOrder order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Department department;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private VialType vialType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SampleStatus status = SampleStatus.PENDING_COLLECTION;

    private LocalDateTime collectedAt;

    @Column(length = 100)
    private String collectedBy;

    @Column(length = 255)
    private String flagReason;

    public LabSample() {}

    public LabSample(Long sampleSequenceId, String barcode, LabOrder order, Patient patient, Department department, VialType vialType) {
        this.sampleSequenceId = sampleSequenceId;
        this.barcode = barcode;
        this.order = order;
        this.patient = patient;
        this.department = department;
        this.vialType = vialType;
        this.status = SampleStatus.PENDING_COLLECTION;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSampleSequenceId() {
        return sampleSequenceId;
    }

    public void setSampleSequenceId(Long sampleSequenceId) {
        this.sampleSequenceId = sampleSequenceId;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
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

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public VialType getVialType() {
        return vialType;
    }

    public void setVialType(VialType vialType) {
        this.vialType = vialType;
    }

    public SampleStatus getStatus() {
        return status;
    }

    public void setStatus(SampleStatus status) {
        this.status = status;
    }

    public LocalDateTime getCollectedAt() {
        return collectedAt;
    }

    public void setCollectedAt(LocalDateTime collectedAt) {
        this.collectedAt = collectedAt;
    }

    public String getCollectedBy() {
        return collectedBy;
    }

    public void setCollectedBy(String collectedBy) {
        this.collectedBy = collectedBy;
    }

    public String getFlagReason() {
        return flagReason;
    }

    public void setFlagReason(String flagReason) {
        this.flagReason = flagReason;
    }
}
