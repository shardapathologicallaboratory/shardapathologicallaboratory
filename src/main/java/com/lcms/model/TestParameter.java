package com.lcms.model;

import jakarta.persistence.*;

@Entity
@Table(name = "lcms_test_parameters")
public class TestParameter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_item_id")
    private TestItem testItem;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 50)
    private String unit;

    @Column(length = 100)
    private String normalRangeDisplay;

    private Double normalMin;
    private Double normalMax;

    private Double criticalMin;
    private Double criticalMax;

    private int sortOrder = 0;

    private boolean calculated = false;

    public TestParameter() {}

    public TestParameter(String name, String unit, String normalRangeDisplay, Double normalMin, Double normalMax, int sortOrder, boolean calculated) {
        this.name = name;
        this.unit = unit;
        this.normalRangeDisplay = normalRangeDisplay;
        this.normalMin = normalMin;
        this.normalMax = normalMax;
        this.sortOrder = sortOrder;
        this.calculated = calculated;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TestItem getTestItem() {
        return testItem;
    }

    public void setTestItem(TestItem testItem) {
        this.testItem = testItem;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getNormalRangeDisplay() {
        return normalRangeDisplay;
    }

    public void setNormalRangeDisplay(String normalRangeDisplay) {
        this.normalRangeDisplay = normalRangeDisplay;
    }

    public Double getNormalMin() {
        return normalMin;
    }

    public void setNormalMin(Double normalMin) {
        this.normalMin = normalMin;
    }

    public Double getNormalMax() {
        return normalMax;
    }

    public void setNormalMax(Double normalMax) {
        this.normalMax = normalMax;
    }

    public Double getCriticalMin() {
        return criticalMin;
    }

    public void setCriticalMin(Double criticalMin) {
        this.criticalMin = criticalMin;
    }

    public Double getCriticalMax() {
        return criticalMax;
    }

    public void setCriticalMax(Double criticalMax) {
        this.criticalMax = criticalMax;
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
}
