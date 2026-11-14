package com.lcms.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "lcms_shift_sessions", indexes = {
    @Index(name = "idx_shift_status", columnList = "status"),
    @Index(name = "idx_shift_start", columnList = "startTime")
})
public class ShiftSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String sessionCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime startTime = LocalDateTime.now();

    private LocalDateTime endTime;

    @Column(nullable = false, length = 20)
    private String status = "OPEN"; // OPEN, CLOSED

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal openingCashFloat = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalCashBilled = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalUpiBilled = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalCardBilled = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalRefunds = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal expectedCashInDrawer = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal actualCashCounted;

    @Column(precision = 10, scale = 2)
    private BigDecimal cashVariance = BigDecimal.ZERO;

    @Column(length = 500)
    private String closingNotes;

    @Column(length = 100)
    private String closedBy;

    public ShiftSession() {}

    public ShiftSession(String sessionCode, User user, BigDecimal openingCashFloat) {
        this.sessionCode = sessionCode;
        this.user = user;
        this.openingCashFloat = openingCashFloat;
        this.expectedCashInDrawer = openingCashFloat;
        this.startTime = LocalDateTime.now();
        this.status = "OPEN";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSessionCode() {
        return sessionCode;
    }

    public void setSessionCode(String sessionCode) {
        this.sessionCode = sessionCode;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getOpeningCashFloat() {
        return openingCashFloat;
    }

    public void setOpeningCashFloat(BigDecimal openingCashFloat) {
        this.openingCashFloat = openingCashFloat;
    }

    public BigDecimal getTotalCashBilled() {
        return totalCashBilled;
    }

    public void setTotalCashBilled(BigDecimal totalCashBilled) {
        this.totalCashBilled = totalCashBilled;
    }

    public BigDecimal getTotalUpiBilled() {
        return totalUpiBilled;
    }

    public void setTotalUpiBilled(BigDecimal totalUpiBilled) {
        this.totalUpiBilled = totalUpiBilled;
    }

    public BigDecimal getTotalCardBilled() {
        return totalCardBilled;
    }

    public void setTotalCardBilled(BigDecimal totalCardBilled) {
        this.totalCardBilled = totalCardBilled;
    }

    public BigDecimal getTotalRefunds() {
        return totalRefunds;
    }

    public void setTotalRefunds(BigDecimal totalRefunds) {
        this.totalRefunds = totalRefunds;
    }

    public BigDecimal getExpectedCashInDrawer() {
        return expectedCashInDrawer;
    }

    public void setExpectedCashInDrawer(BigDecimal expectedCashInDrawer) {
        this.expectedCashInDrawer = expectedCashInDrawer;
    }

    public BigDecimal getActualCashCounted() {
        return actualCashCounted;
    }

    public void setActualCashCounted(BigDecimal actualCashCounted) {
        this.actualCashCounted = actualCashCounted;
    }

    public BigDecimal getCashVariance() {
        return cashVariance;
    }

    public void setCashVariance(BigDecimal cashVariance) {
        this.cashVariance = cashVariance;
    }

    public String getClosingNotes() {
        return closingNotes;
    }

    public void setClosingNotes(String closingNotes) {
        this.closingNotes = closingNotes;
    }

    public String getClosedBy() {
        return closedBy;
    }

    public void setClosedBy(String closedBy) {
        this.closedBy = closedBy;
    }
}
