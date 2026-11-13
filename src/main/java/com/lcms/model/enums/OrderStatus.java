package com.lcms.model.enums;

public enum OrderStatus {
    BOOKED("Order Booked", "bg-blue-50 text-blue-700 border-blue-200"),
    COLLECTED("Sample Collected", "bg-amber-50 text-amber-700 border-amber-200"),
    PROCESSING("In Testing", "bg-purple-50 text-purple-700 border-purple-200"),
    VERIFIED("Verified by Pathologist", "bg-teal-50 text-teal-700 border-teal-200"),
    PRINTED("Report Printed", "bg-emerald-50 text-emerald-700 border-emerald-200"),
    DISPATCHED("Dispatched to Patient", "bg-slate-100 text-slate-800 border-slate-300"),
    CANCELLED("Cancelled", "bg-rose-50 text-rose-700 border-rose-200");

    private final String displayName;
    private final String badgeClass;

    OrderStatus(String displayName, String badgeClass) {
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
