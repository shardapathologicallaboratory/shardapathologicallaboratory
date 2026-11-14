package com.lcms.model;

import com.lcms.model.enums.Department;
import com.lcms.model.enums.ResultFlag;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "lcms_test_results", indexes = {
    @Index(name = "idx_res_order_item", columnList = "order_item_id"),
    @Index(name = "idx_res_patient_param", columnList = "patient_id, parameter_name")
})
public class TestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private LabOrder order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Department department;

    @Column(nullable = false, length = 150)
    private String parameterName;

    @Column(length = 50)
    private String unitSnapshot;

    @Column(length = 100)
    private String rangeSnapshot;

    @Column(length = 500)
    private String valueText;

    private Double valueNumeric;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ResultFlag flag = ResultFlag.NORMAL;

    private boolean deltaAlert = false;

    private Double previousValue;

    private Double deltaPercentage;

    @Column(length = 1000)
    private String technicalComment; // Footnote from Ctrl+T (Req #15)

    @Column(nullable = false, length = 30)
    private String status = "ENTERED"; // ENTERED, VERIFIED, SENT_BACK

    private int sortOrder = 0;

    private boolean calculated = false;

    private String enteredBy;
    private LocalDateTime enteredAt;

    private String verifiedBy;
    private LocalDateTime verifiedAt;

    public TestResult() {}

    public TestResult(OrderItem orderItem, LabOrder order, Patient patient, Department department,
                      String parameterName, String unitSnapshot, String rangeSnapshot,
                      int sortOrder, boolean calculated) {
        this.orderItem = orderItem;
        this.order = order;
        this.patient = patient;
        this.department = department;
        this.parameterName = parameterName;
        this.unitSnapshot = unitSnapshot;
        this.rangeSnapshot = rangeSnapshot;
        this.sortOrder = sortOrder;
        this.calculated = calculated;
        this.flag = ResultFlag.NORMAL;
        this.status = "ENTERED";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OrderItem getOrderItem() {
        return orderItem;
    }

    public void setOrderItem(OrderItem orderItem) {
        this.orderItem = orderItem;
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

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public String getUnitSnapshot() {
        return unitSnapshot;
    }

    public void setUnitSnapshot(String unitSnapshot) {
        this.unitSnapshot = unitSnapshot;
    }

    public String getRangeSnapshot() {
        return rangeSnapshot;
    }

    public void setRangeSnapshot(String rangeSnapshot) {
        this.rangeSnapshot = rangeSnapshot;
    }

    public String getValueText() {
        return valueText;
    }

    public void setValueText(String valueText) {
        this.valueText = valueText;
    }

    public Double getValueNumeric() {
        return valueNumeric;
    }

    public void setValueNumeric(Double valueNumeric) {
        this.valueNumeric = valueNumeric;
    }

    public ResultFlag getFlag() {
        return flag;
    }

    public void setFlag(ResultFlag flag) {
        this.flag = flag;
    }

    public boolean isDeltaAlert() {
        return deltaAlert;
    }

    public void setDeltaAlert(boolean deltaAlert) {
        this.deltaAlert = deltaAlert;
    }

    public Double getPreviousValue() {
        return previousValue;
    }

    public void setPreviousValue(Double previousValue) {
        this.previousValue = previousValue;
    }

    public Double getDeltaPercentage() {
        return deltaPercentage;
    }

    public void setDeltaPercentage(Double deltaPercentage) {
        this.deltaPercentage = deltaPercentage;
    }

    public String getTechnicalComment() {
        return technicalComment;
    }

    public void setTechnicalComment(String technicalComment) {
        this.technicalComment = technicalComment;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public boolean isCalculated() {
        return calculated;
    }

    public void setCalculated(boolean calculated) {
        this.calculated = calculated;
    }

    public String getEnteredBy() {
        return enteredBy;
    }

    public void setEnteredBy(String enteredBy) {
        this.enteredBy = enteredBy;
    }

    public LocalDateTime getEnteredAt() {
        return enteredAt;
    }

    public void setEnteredAt(LocalDateTime enteredAt) {
        this.enteredAt = enteredAt;
    }

    public String getVerifiedBy() {
        return verifiedBy;
    }

    public void setVerifiedBy(String verifiedBy) {
        this.verifiedBy = verifiedBy;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }
}
