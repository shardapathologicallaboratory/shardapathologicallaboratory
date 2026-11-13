package com.lcms.model.enums;

public enum PaymentStatus {
    PAID("Fully Paid", "bg-emerald-50 text-emerald-700 border-emerald-200"),
    PARTIAL("Partially Paid", "bg-amber-50 text-amber-700 border-amber-200"),
    DUE("Payment Due", "bg-rose-50 text-rose-700 border-rose-200");

    private final String displayName;
    private final String badgeClass;

    PaymentStatus(String displayName, String badgeClass) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadgeClass() {
        return badgeClass;
    }
}
