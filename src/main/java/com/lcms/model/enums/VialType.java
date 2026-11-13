package com.lcms.model.enums;

public enum VialType {
    EDTA("EDTA (Purple / Lavender)", "🟣", "#9333ea"),
    SST_GEL("SST Gel / Serum (Gold / Yellow)", "🟡", "#eab308"),
    PLAIN("Plain / Red Top", "🔴", "#ef4444"),
    FLUORIDE("Fluoride Plasma (Grey)", "⚪", "#94a3b8"),
    CITRATE("Sodium Citrate (Blue)", "🔵", "#3b82f6"),
    URINE_CONTAINER("Sterile Urine Container", "🟡", "#f59e0b"),
    STOOL_CONTAINER("Sterile Stool Container", "🟤", "#78350f"),
    SWAB("Culture Swab / Bottle", "🧪", "#10b981");

    private final String displayName;
    private final String emoji;
    private final String colorHex;

    VialType(String displayName, String emoji, String colorHex) {
        this.displayName = displayName;
        this.emoji = emoji;
        this.colorHex = colorHex;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmoji() {
        return emoji;
    }

    public String getColorHex() {
        return colorHex;
    }
}
