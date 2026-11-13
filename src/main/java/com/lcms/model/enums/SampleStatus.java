package com.lcms.model.enums;

public enum SampleStatus {
    PENDING_COLLECTION("Pending Collection"),
    COLLECTED("Collected"),
    RECEIVED("Received at Lab"),
    PROCESSING("Processing on Bench"),
    FLAGGED_RECHECK("Flagged for Recheck"),
    RECOLLECTION_REQUESTED("Recollection Requested");

    private final String displayName;

    SampleStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
