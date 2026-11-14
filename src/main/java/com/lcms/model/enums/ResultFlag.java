package com.lcms.model.enums;

public enum ResultFlag {
    NORMAL("Normal", "bg-emerald-50 text-emerald-700 border-emerald-200"),
    LOW("Low", "bg-amber-50 text-amber-700 border-amber-200"),
    HIGH("High", "bg-amber-50 text-amber-700 border-amber-200"),
    CRITICAL_LOW("Critical Low 🚨", "bg-rose-100 text-rose-800 border-rose-300 font-bold"),
    CRITICAL_HIGH("Critical High 🚨", "bg-rose-100 text-rose-800 border-rose-300 font-bold"),
    ABNORMAL("Abnormal", "bg-amber-50 text-amber-700 border-amber-200"),
    NO_RANGE("No Range", "bg-slate-100 text-slate-600 border-slate-200");

    private final String displayName;
    private final String badgeClass;

    ResultFlag(String displayName, String badgeClass) {
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
