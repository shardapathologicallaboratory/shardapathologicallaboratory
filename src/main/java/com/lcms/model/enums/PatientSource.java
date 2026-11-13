package com.lcms.model.enums;

public enum PatientSource {
    WALK_IN("In-Person Walk-in", "MAIN"),
    B2B("B2B Lab-to-Lab Outsource", "B2B"),
    COLLECTION_CENTRE("Collection Centre / Franchisee", "CC");

    private final String displayName;
    private final String prefix;

    PatientSource(String displayName, String prefix) {
        this.displayName = displayName;
        this.prefix = prefix;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPrefix() {
        return prefix;
    }
}
