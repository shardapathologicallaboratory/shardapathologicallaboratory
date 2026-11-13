package com.lcms.model.enums;

public enum PaymentMode {
    CASH("Cash"),
    UPI("UPI (PhonePe / GPay / QR)"),
    CARD("Debit / Credit Card (POS)"),
    CREDIT_LEDGER("Credit / B2B Monthly Ledger");

    private final String displayName;

    PaymentMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
