package com.lcms.model;

import com.lcms.model.enums.Department;
import com.lcms.model.enums.VialType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lcms_test_items")
public class TestItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Department department;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private VialType defaultVial;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal retailPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal b2bPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal collectionCentrePrice;

    private boolean active = true;

    @OneToMany(mappedBy = "testItem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("sortOrder ASC")
    private List<TestParameter> parameters = new ArrayList<>();

    public TestItem() {}

    public TestItem(String code, String name, Department department, VialType defaultVial, BigDecimal retailPrice, BigDecimal b2bPrice, BigDecimal collectionCentrePrice) {
        this.code = code;
        this.name = name;
        this.department = department;
        this.defaultVial = defaultVial;
        this.retailPrice = retailPrice;
        this.b2bPrice = b2bPrice;
        this.collectionCentrePrice = collectionCentrePrice;
    }

    public void addParameter(TestParameter parameter) {
        parameters.add(parameter);
        parameter.setTestItem(this);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public VialType getDefaultVial() {
        return defaultVial;
    }

    public void setDefaultVial(VialType defaultVial) {
        this.defaultVial = defaultVial;
    }

    public BigDecimal getRetailPrice() {
        return retailPrice;
    }

    public void setRetailPrice(BigDecimal retailPrice) {
        this.retailPrice = retailPrice;
    }

    public BigDecimal getB2bPrice() {
        return b2bPrice;
    }

    public void setB2bPrice(BigDecimal b2bPrice) {
        this.b2bPrice = b2bPrice;
    }

    public BigDecimal getCollectionCentrePrice() {
        return collectionCentrePrice;
    }

    public void setCollectionCentrePrice(BigDecimal collectionCentrePrice) {
        this.collectionCentrePrice = collectionCentrePrice;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<TestParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<TestParameter> parameters) {
        this.parameters = parameters;
    }
}
