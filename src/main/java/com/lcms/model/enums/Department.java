package com.lcms.model.enums;

public enum Department {
    HEMATOLOGY("Hematology", "🩸", "CBC, ESR, Blood Group, Peripheral Smear"),
    BIOCHEMISTRY("Biochemistry", "🧪", "KFT, LFT, Electrolytes, Glucose, Lipid Profile"),
    CLINICAL_PATHOLOGY("Clinical Pathology", "🔬", "Urine Routine & Micro, Stool, Semen"),
    IMMUNOLOGY("Immunology", "🧬", "Thyroid (T3/T4/TSH), Hormones, Vitamin D, B12, Allergy"),
    SEROLOGY("Serology", "🛡️", "Widal, Typhidot, Malaria Antigen, Dengue, Rapid Tests"),
    CYTOLOGY("Cytology & Histopathology", "📷", "FNAC, Biopsy, Fluid Cytology (Requires Images)"),
    MICROBIOLOGY("Microbiology & Culture", "🧫", "Urine/Blood Culture & Sensitivity, Sputum AFB");

    private final String displayName;
    private final String icon;
    private final String description;

    Department(String displayName, String icon, String description) {
        this.displayName = displayName;
        this.icon = icon;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }

    public String getDescription() {
        return description;
    }
}
