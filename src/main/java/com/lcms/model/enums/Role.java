package com.lcms.model.enums;

public enum Role {
    ROLE_ADMIN("Lab Admin / Director"),
    ROLE_ACCOUNTANT("Accountant & Finance"),
    ROLE_RECEPTION("Reception & Registration"),
    ROLE_COLLECTION("Phlebotomist / Collection"),
    ROLE_TECHNICIAN("Lab Technician"),
    ROLE_REPORTING("Reporting Desk (Print Only)");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
