package com.lcms.model.enums;

public enum Department {
    HEMATOLOGY("Hematology", "🩸", "CBC, ESR, Blood Group, Peripheral Smear"),
    BIOCHEMISTRY("Biochemistry", "🧪", "KFT, LFT, Electrolytes, Glucose, Lipid Profile"),
    CLINICAL_PATHOLOGY("Clinical Pathology", "🔬", "Urine Routine & Micro, Stool, Semen"),
    SEROLOGY_IMMUNOLOGY("Serology & Immunology", "🛡️", "Widal, Typhidot, Malaria Antigen, Thyroid, Allergy"),
    MICROBIOLOGY("Microbiology & Culture", "🧫", "Urine/Blood Culture & Sensitivity, Sputum AFB"),
    CYTOLOGY_HISTO("Cytology & Histopathology", "📷", "FNAC, Biopsy, Fluid Cytology (Requires Images)");

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
