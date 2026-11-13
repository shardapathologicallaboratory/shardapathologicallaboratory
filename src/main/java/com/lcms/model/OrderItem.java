package com.lcms.model;

import com.lcms.model.enums.Department;
import com.lcms.model.enums.VialType;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "lcms_order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private LabOrder order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_item_id")
    private TestItem testItem;

    @Column(nullable = false, length = 150)
    private String testName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Department department;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private VialType vialType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    private boolean customTest = false;

    @Column(length = 50)
    private String status = "BOOKED";

    public OrderItem() {}

    public OrderItem(LabOrder order, TestItem testItem, String testName, Department department, VialType vialType, BigDecimal price, boolean customTest) {
        this.order = order;
        this.testItem = testItem;
        this.testName = testName;
        this.department = department;
        this.vialType = vialType;
        this.price = price;
        this.customTest = customTest;
        this.status = "BOOKED";
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

    public TestItem getTestItem() {
        return testItem;
    }

    public void setTestItem(TestItem testItem) {
        this.testItem = testItem;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public boolean isCustomTest() {
        return customTest;
    }

    public void setCustomTest(boolean customTest) {
        this.customTest = customTest;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
