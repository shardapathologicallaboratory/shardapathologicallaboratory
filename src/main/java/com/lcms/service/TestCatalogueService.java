package com.lcms.service;

import com.lcms.model.TestItem;
import com.lcms.model.TestParameter;
import com.lcms.model.enums.Department;
import com.lcms.model.enums.PatientSource;
import com.lcms.model.enums.VialType;
import com.lcms.repository.TestItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class TestCatalogueService {

    private final TestItemRepository testItemRepository;

    public TestCatalogueService(TestItemRepository testItemRepository) {
        this.testItemRepository = testItemRepository;
    }

    public List<TestItem> getAllActiveTests() {
        return testItemRepository.findByActiveTrueOrderByDepartmentAscNameAsc();
    }

    public List<TestItem> searchTests(String query) {
        return testItemRepository.searchActiveTests(query);
    }

    public Optional<TestItem> findById(Long id) {
        return testItemRepository.findById(id);
    }

    public Optional<TestItem> findByCode(String code) {
        return testItemRepository.findByCode(code);
    }

    public BigDecimal resolvePrice(TestItem item, PatientSource source) {
        if (item == null) return BigDecimal.ZERO;
        return switch (source) {
            case B2B -> item.getB2bPrice();
            case COLLECTION_CENTRE -> item.getCollectionCentrePrice();
            case WALK_IN -> item.getRetailPrice();
        };
    }

    @Transactional
    public void seedInitialCatalogueIfEmpty() {
        seedOrUpdateFullCatalogue();
    }

    @Transactional
    public void seedOrUpdateFullCatalogue() {
        // 1. CBC
        TestItem cbc = upsertTest("CBC", "Complete Blood Count (CBC)", Department.HEMATOLOGY, VialType.EDTA,
                "400.00", "280.00", "320.00", null, null, null, null, null, false);
        if (cbc.getParameters() == null || cbc.getParameters().isEmpty()) {
            cbc.addParameter(new TestParameter("Hemoglobin (Hb)", "g/dL", "12.0 - 16.0", 12.0, 16.0, 1, false));
            cbc.addParameter(new TestParameter("Total Leukocyte Count (TLC)", "/µL", "4000 - 11000", 4000.0, 11000.0, 2, false));
            cbc.addParameter(new TestParameter("Platelet Count", "Lakh/µL", "1.50 - 4.50", 1.5, 4.5, 3, false));
            cbc.addParameter(new TestParameter("RBC Count", "mil/µL", "3.80 - 5.80", 3.8, 5.8, 4, false));
            cbc.addParameter(new TestParameter("PCV / Hematocrit", "%", "36.0 - 46.0", 36.0, 46.0, 5, true));
            cbc.addParameter(new TestParameter("MCV", "fL", "80.0 - 100.0", 80.0, 100.0, 6, true));
            cbc.addParameter(new TestParameter("MCH", "pg", "27.0 - 32.0", 27.0, 32.0, 7, true));
            cbc.addParameter(new TestParameter("MCHC", "g/dL", "31.5 - 34.5", 31.5, 34.5, 8, true));
            cbc.addParameter(new TestParameter("Neutrophils", "%", "40 - 70", 40.0, 70.0, 9, false));
            cbc.addParameter(new TestParameter("Lymphocytes", "%", "20 - 45", 20.0, 45.0, 10, false));
            cbc.addParameter(new TestParameter("Eosinophils", "%", "1 - 6", 1.0, 6.0, 11, false));
            cbc.addParameter(new TestParameter("Monocytes", "%", "2 - 10", 2.0, 10.0, 12, false));
            cbc.addParameter(new TestParameter("Basophils", "%", "0 - 1", 0.0, 1.0, 13, false));
            testItemRepository.save(cbc);
        }

        // 2-12 Hematology & Serology Singles
        upsertTest("HEAMOGLOBIN", "Hemoglobin (Hb)", Department.HEMATOLOGY, VialType.EDTA, "80.00", "55.00", "65.00", "Hemoglobin (Hb)", "g/dL", "12.0 - 16.0", 12.0, 16.0, false);
        upsertTest("TC_DC", "Total & Differential Count (TC/DC)", Department.HEMATOLOGY, VialType.EDTA, "80.00", "55.00", "65.00", "Total Leukocyte Count (TLC)", "/µL", "4000 - 11000", 4000.0, 11000.0, false);
        upsertTest("BLOOD_GROUP", "Blood Group & Rh Type", Department.HEMATOLOGY, VialType.EDTA, "100.00", "70.00", "85.00", "Blood Group & Rh Type", "", "ABO / Rh System", 0.0, 0.0, false);
        upsertTest("PBS", "Peripheral Blood Smear (PBS)", Department.HEMATOLOGY, VialType.EDTA, "150.00", "105.00", "125.00", "RBC & WBC Morphology", "", "Normocytic Normochromic", 0.0, 0.0, false);
        upsertTest("RETICULOCYTE_COUNT", "Reticulocyte Count", Department.HEMATOLOGY, VialType.EDTA, "300.00", "210.00", "250.00", "Reticulocyte Count", "%", "0.5 - 2.0", 0.5, 2.0, false);
        upsertTest("ESR", "Erythrocyte Sedimentation Rate (ESR)", Department.HEMATOLOGY, VialType.EDTA, "80.00", "55.00", "65.00", "ESR (Westergren)", "mm/1st hr", "0 - 20", 0.0, 20.0, false);
        upsertTest("MALARIA_ANTIGEN", "Malaria Antigen (Pf/Pv Rapid)", Department.SEROLOGY, VialType.EDTA, "200.00", "140.00", "170.00", "Malaria Antigen Result", "", "Negative", 0.0, 0.0, false);
        upsertTest("MALARIA_PARASITE", "Malaria Parasite (MP Smear)", Department.HEMATOLOGY, VialType.EDTA, "150.00", "105.00", "125.00", "Malaria Parasite (Thick & Thin)", "", "No Malarial Parasite Seen", 0.0, 0.0, false);
        upsertTest("WIDAL_SLIDE", "Widal Slide Method", Department.SEROLOGY, VialType.SST_GEL, "200.00", "140.00", "170.00", "Widal Slide Agglutination", "", "Negative (< 1:80)", 0.0, 0.0, false);
        upsertTest("TYPHIDOT_IGM", "Typhidot IgM", Department.SEROLOGY, VialType.SST_GEL, "300.00", "210.00", "250.00", "Typhidot IgM Antibody", "", "Negative", 0.0, 0.0, false);

        // 12. Typhidot IgG/IgM
        TestItem typhidot = upsertTest("TYPHIDOT", "Typhidot IgG/IgM Combo", Department.SEROLOGY, VialType.SST_GEL,
                "420.00", "290.00", "350.00", null, null, null, null, null, false);
        if (typhidot.getParameters() == null || typhidot.getParameters().isEmpty()) {
            typhidot.addParameter(new TestParameter("Typhidot IgM", "", "Negative", 0.0, 0.0, 1, false));
            typhidot.addParameter(new TestParameter("Typhidot IgG", "", "Negative", 0.0, 0.0, 2, false));
            testItemRepository.save(typhidot);
        }

        // 13. Routine Urine
        TestItem urine = upsertTest("URINE_RM", "Routine Urine & Microscopy", Department.CLINICAL_PATHOLOGY, VialType.URINE_CONTAINER,
                "80.00", "55.00", "65.00", null, null, null, null, null, false);
        if (urine.getParameters() == null || urine.getParameters().isEmpty()) {
            urine.addParameter(new TestParameter("Color & Appearance", "", "Pale Yellow / Clear", 0.0, 0.0, 1, false));
            urine.addParameter(new TestParameter("pH", "", "5.0 - 7.5", 5.0, 7.5, 2, false));
            urine.addParameter(new TestParameter("Specific Gravity", "", "1.010 - 1.025", 1.010, 1.025, 3, false));
            urine.addParameter(new TestParameter("Albumin / Protein", "", "Nil", 0.0, 0.0, 4, false));
            urine.addParameter(new TestParameter("Sugar / Glucose", "", "Nil", 0.0, 0.0, 5, false));
            urine.addParameter(new TestParameter("Pus Cells", "/HPF", "0 - 5", 0.0, 5.0, 6, false));
            urine.addParameter(new TestParameter("RBCs", "/HPF", "Nil / 0-2", 0.0, 2.0, 7, false));
            urine.addParameter(new TestParameter("Epithelial Cells", "/HPF", "Few / 1-4", 0.0, 4.0, 8, false));
            testItemRepository.save(urine);
        }

        // 14-17 Microbiology
        upsertTest("URINE_CULTURE", "Urine Culture & Sensitivity", Department.MICROBIOLOGY, VialType.URINE_CONTAINER, "500.00", "385.00", "440.00", "Culture Organism & Colony Count", "", "No Growth after 48 hrs", 0.0, 0.0, false);
        upsertTest("PUS_CULTURE", "Pus Culture & Sensitivity", Department.MICROBIOLOGY, VialType.SWAB, "500.00", "350.00", "420.00", "Organism Isolated", "", "No Pathogenic Growth", 0.0, 0.0, false);
        upsertTest("BLOOD_CULTURE_BACTEC", "Blood Culture (BACTEC Automated)", Department.MICROBIOLOGY, VialType.SWAB, "1700.00", "1150.00", "1400.00", "Automated Blood Culture", "", "Sterile after 5 Days", 0.0, 0.0, false);
        upsertTest("BLOOD_CULTURE", "Blood Culture & Sensitivity", Department.MICROBIOLOGY, VialType.SWAB, "700.00", "590.00", "640.00", "Blood Culture Result", "", "Sterile after 48-72 hrs", 0.0, 0.0, false);

        // 18-19 Sugars
        upsertTest("FBS", "Blood Sugar (Random / Fasting / PP)", Department.BIOCHEMISTRY, VialType.FLUORIDE, "80.00", "55.00", "65.00", "Blood Glucose", "mg/dL", "70 - 140", 70.0, 140.0, false);
        upsertTest("URINE_SUGAR", "Urine Sugar (Fasting / 1Hr / 2Hr)", Department.CLINICAL_PATHOLOGY, VialType.URINE_CONTAINER, "40.00", "30.00", "35.00", "Urine Sugar", "", "Nil", 0.0, 0.0, false);

        // 20-23 Serology Virals
        upsertTest("HIV", "HIV 1 & 2 Antibody Rapid", Department.SEROLOGY, VialType.SST_GEL, "280.00", "195.00", "235.00", "HIV 1 & 2 Antibodies", "", "Non-Reactive", 0.0, 0.0, false);
        upsertTest("HBSAG", "HBsAg (Hepatitis B Surface Antigen)", Department.SEROLOGY, VialType.SST_GEL, "300.00", "180.00", "240.00", "HBsAg", "", "Non-Reactive", 0.0, 0.0, false);
        upsertTest("HCV", "HCV (Hepatitis C Antibody)", Department.SEROLOGY, VialType.SST_GEL, "400.00", "280.00", "340.00", "Anti-HCV Antibody", "", "Non-Reactive", 0.0, 0.0, false);
        upsertTest("SYPHILIS", "Syphilis (VDRL / RPR)", Department.SEROLOGY, VialType.SST_GEL, "130.00", "100.00", "115.00", "VDRL / RPR Flocculation", "", "Non-Reactive", 0.0, 0.0, false);

        // 24. Dengue NS1 / IgG / IgM
        TestItem dengue = upsertTest("DENGUE_COMBO", "Dengue NS1 / IgG / IgM", Department.SEROLOGY, VialType.SST_GEL,
                "1000.00", "700.00", "850.00", null, null, null, null, null, false);
        if (dengue.getParameters() == null || dengue.getParameters().isEmpty()) {
            dengue.addParameter(new TestParameter("Dengue NS1 Antigen", "", "Negative", 0.0, 0.0, 1, false));
            dengue.addParameter(new TestParameter("Dengue IgM Antibody", "", "Negative", 0.0, 0.0, 2, false));
            dengue.addParameter(new TestParameter("Dengue IgG Antibody", "", "Negative", 0.0, 0.0, 3, false));
            testItemRepository.save(dengue);
        }

        // 25. LFT
        TestItem lft = upsertTest("LFT", "Liver Function Test (LFT)", Department.BIOCHEMISTRY, VialType.SST_GEL,
                "600.00", "420.00", "500.00", null, null, null, null, null, false);
        if (lft.getParameters() == null || lft.getParameters().isEmpty()) {
            lft.addParameter(new TestParameter("Total Bilirubin", "mg/dL", "0.2 - 1.2", 0.2, 1.2, 1, false));
            lft.addParameter(new TestParameter("Direct Bilirubin", "mg/dL", "0.0 - 0.3", 0.0, 0.3, 2, false));
            lft.addParameter(new TestParameter("Indirect Bilirubin", "mg/dL", "0.2 - 0.9", 0.2, 0.9, 3, true));
            lft.addParameter(new TestParameter("SGPT / ALT", "U/L", "10 - 40", 10.0, 40.0, 4, false));
            lft.addParameter(new TestParameter("SGOT / AST", "U/L", "10 - 38", 10.0, 38.0, 5, false));
            lft.addParameter(new TestParameter("Alkaline Phosphatase (ALP)", "U/L", "40 - 130", 40.0, 130.0, 6, false));
            lft.addParameter(new TestParameter("Total Protein", "g/dL", "6.4 - 8.3", 6.4, 8.3, 7, false));
            lft.addParameter(new TestParameter("Serum Albumin", "g/dL", "3.5 - 5.0", 3.5, 5.0, 8, false));
            lft.addParameter(new TestParameter("Serum Globulin", "g/dL", "2.0 - 3.5", 2.0, 3.5, 9, true));
            lft.addParameter(new TestParameter("A/G Ratio", "ratio", "1.1 - 2.2", 1.1, 2.2, 10, true));
            testItemRepository.save(lft);
        }

        // 26. KFT 8-9 Para
        TestItem kft = upsertTest("KFT", "Kidney Function Test (KFT 8-9 Para)", Department.BIOCHEMISTRY, VialType.SST_GEL,
                "850.00", "595.00", "700.00", null, null, null, null, null, false);
        if (kft.getParameters() == null || kft.getParameters().isEmpty()) {
            kft.addParameter(new TestParameter("Blood Urea", "mg/dL", "15.0 - 45.0", 15.0, 45.0, 1, false));
            kft.addParameter(new TestParameter("Serum Creatinine", "mg/dL", "0.6 - 1.2", 0.6, 1.2, 2, false));
            kft.addParameter(new TestParameter("Serum Uric Acid", "mg/dL", "3.4 - 7.0", 3.4, 7.0, 3, false));
            kft.addParameter(new TestParameter("BUN", "mg/dL", "7.0 - 20.0", 7.0, 20.0, 4, true));
            kft.addParameter(new TestParameter("Serum Calcium", "mg/dL", "8.5 - 10.5", 8.5, 10.5, 5, false));
            kft.addParameter(new TestParameter("Serum Sodium", "mEq/L", "135 - 145", 135.0, 145.0, 6, false));
            kft.addParameter(new TestParameter("Serum Potassium", "mEq/L", "3.5 - 5.1", 3.5, 5.1, 7, false));
            kft.addParameter(new TestParameter("Serum Chloride", "mEq/L", "98 - 107", 98.0, 107.0, 8, false));
            testItemRepository.save(kft);
        }

        // 27. Lipid Profile
        TestItem lipid = upsertTest("LIPID", "Lipid Profile Complete", Department.BIOCHEMISTRY, VialType.SST_GEL,
                "400.00", "150.00", "275.00", null, null, null, null, null, false);
        if (lipid.getParameters() == null || lipid.getParameters().isEmpty()) {
            lipid.addParameter(new TestParameter("Total Cholesterol", "mg/dL", "125 - 200", 125.0, 200.0, 1, false));
            lipid.addParameter(new TestParameter("Triglycerides", "mg/dL", "50 - 150", 50.0, 150.0, 2, false));
            lipid.addParameter(new TestParameter("HDL Cholesterol", "mg/dL", "40 - 60", 40.0, 60.0, 3, false));
            lipid.addParameter(new TestParameter("LDL Cholesterol", "mg/dL", "60 - 100", 60.0, 100.0, 4, true));
            lipid.addParameter(new TestParameter("VLDL Cholesterol", "mg/dL", "10 - 30", 10.0, 30.0, 5, true));
            testItemRepository.save(lipid);
        }

        // 28-45 Biochemistry Singles
        upsertTest("SGOT", "SGOT / AST", Department.BIOCHEMISTRY, VialType.SST_GEL, "160.00", "110.00", "135.00", "SGOT / AST", "U/L", "10 - 38", 10.0, 38.0, false);
        upsertTest("SGPT", "SGPT / ALT", Department.BIOCHEMISTRY, VialType.SST_GEL, "160.00", "110.00", "135.00", "SGPT / ALT", "U/L", "10 - 40", 10.0, 40.0, false);
        upsertTest("ALKP", "Alkaline Phosphatase (ALKP)", Department.BIOCHEMISTRY, VialType.SST_GEL, "160.00", "110.00", "135.00", "Alkaline Phosphatase (ALP)", "U/L", "40 - 130", 40.0, 130.0, false);
        upsertTest("TOTAL_PROTEIN", "Total Protein (Serum)", Department.BIOCHEMISTRY, VialType.SST_GEL, "190.00", "130.00", "160.00", "Total Protein", "g/dL", "6.4 - 8.3", 6.4, 8.3, false);
        upsertTest("TOTAL_BILIRUBIN", "Total Bilirubin", Department.BIOCHEMISTRY, VialType.SST_GEL, "250.00", "175.00", "210.00", "Total Bilirubin", "mg/dL", "0.2 - 1.2", 0.2, 1.2, false);
        upsertTest("UREA", "Blood Urea", Department.BIOCHEMISTRY, VialType.SST_GEL, "130.00", "100.00", "115.00", "Blood Urea", "mg/dL", "15 - 45", 15.0, 45.0, false);
        upsertTest("CREATININE", "Creatinine (Serum)", Department.BIOCHEMISTRY, VialType.SST_GEL, "140.00", "100.00", "120.00", "Serum Creatinine", "mg/dL", "0.6 - 1.2", 0.6, 1.2, false);
        upsertTest("URIC_ACID", "Uric Acid (Serum)", Department.BIOCHEMISTRY, VialType.SST_GEL, "200.00", "140.00", "170.00", "Serum Uric Acid", "mg/dL", "3.4 - 7.0", 3.4, 7.0, false);
        upsertTest("SERUM_CALCIUM", "Serum Calcium", Department.BIOCHEMISTRY, VialType.SST_GEL, "170.00", "120.00", "145.00", "Serum Calcium", "mg/dL", "8.5 - 10.5", 8.5, 10.5, false);
        upsertTest("EGFR", "Estimated GFR (eGFR)", Department.BIOCHEMISTRY, VialType.SST_GEL, "300.00", "210.00", "255.00", "eGFR", "mL/min/1.73m²", "> 90", 90.0, 150.0, true);
        upsertTest("SODIUM", "Sodium (Serum Na+)", Department.BIOCHEMISTRY, VialType.SST_GEL, "200.00", "140.00", "170.00", "Serum Sodium", "mEq/L", "135 - 145", 135.0, 145.0, false);
        upsertTest("POTASSIUM", "Potassium (Serum K+)", Department.BIOCHEMISTRY, VialType.SST_GEL, "190.00", "130.00", "160.00", "Serum Potassium", "mEq/L", "3.5 - 5.1", 3.5, 5.1, false);
        upsertTest("CHLORIDE", "Chloride (Serum Cl-)", Department.BIOCHEMISTRY, VialType.SST_GEL, "150.00", "105.00", "125.00", "Serum Chloride", "mEq/L", "98 - 107", 98.0, 107.0, false);
        upsertTest("MAGNESIUM", "Magnesium (Serum Mg)", Department.BIOCHEMISTRY, VialType.SST_GEL, "800.00", "550.00", "675.00", "Serum Magnesium", "mg/dL", "1.7 - 2.4", 1.7, 2.4, false);
        upsertTest("CHOLESTEROL", "Cholesterol (Serum Total)", Department.BIOCHEMISTRY, VialType.SST_GEL, "150.00", "105.00", "125.00", "Serum Cholesterol", "mg/dL", "< 200", 0.0, 200.0, false);
        upsertTest("TRIGLYCERIDES", "Triglycerides", Department.BIOCHEMISTRY, VialType.SST_GEL, "330.00", "230.00", "280.00", "Serum Triglycerides", "mg/dL", "< 150", 0.0, 150.0, false);
        upsertTest("AMYLASE", "Amylase (Serum)", Department.BIOCHEMISTRY, VialType.SST_GEL, "500.00", "350.00", "425.00", "Serum Amylase", "U/L", "28 - 100", 28.0, 100.0, false);
        upsertTest("LIPASE", "Lipase (Serum)", Department.BIOCHEMISTRY, VialType.SST_GEL, "500.00", "350.00", "425.00", "Serum Lipase", "U/L", "13 - 60", 13.0, 60.0, false);

        // 46-53 Coagulation, Glyco & Urine
        upsertTest("CRP", "C-Reactive Protein (CRP)", Department.BIOCHEMISTRY, VialType.SST_GEL, "400.00", "280.00", "340.00", "CRP Quantitative", "mg/L", "< 6.0", 0.0, 6.0, false);
        upsertTest("PT_INR", "Prothrombin Time (PT / INR)", Department.HEMATOLOGY, VialType.CITRATE, "500.00", "350.00", "425.00", "PT / INR Result", "Seconds", "11.0 - 13.5 (INR 0.8-1.2)", 11.0, 13.5, false);
        upsertTest("APTT", "Activated Partial Thromboplastin Time (APTT)", Department.HEMATOLOGY, VialType.CITRATE, "500.00", "350.00", "425.00", "APTT Result", "Seconds", "25.0 - 35.0", 25.0, 35.0, false);
        upsertTest("HBA1C", "HbA1c Glyco (Glycosylated Hemoglobin)", Department.BIOCHEMISTRY, VialType.EDTA, "450.00", "250.00", "350.00", "HbA1c Glycated Hemoglobin", "%", "< 5.7 (Normal)", 4.0, 5.6, false);
        upsertTest("RA_FACTOR", "Rheumatoid Arthritis Factor (RA)", Department.SEROLOGY, VialType.SST_GEL, "480.00", "335.00", "400.00", "RA Factor Quantitative", "IU/mL", "< 20", 0.0, 20.0, false);
        upsertTest("ASO", "Anti-Streptolysin O (ASO)", Department.SEROLOGY, VialType.SST_GEL, "480.00", "335.00", "400.00", "ASO Titre Quantitative", "IU/mL", "< 200", 0.0, 200.0, false);
        upsertTest("URINE_ACR", "Urine Albumin Creatinine Ratio (ACR)", Department.CLINICAL_PATHOLOGY, VialType.URINE_CONTAINER, "450.00", "315.00", "380.00", "Urine ACR", "mg/g", "< 30 (Normal)", 0.0, 30.0, false);
        upsertTest("URINE_KETONE", "Urine Ketone Bodies", Department.CLINICAL_PATHOLOGY, VialType.URINE_CONTAINER, "150.00", "105.00", "125.00", "Urine Ketones", "", "Nil / Negative", 0.0, 0.0, false);

        // 54-59 Thyroid & Hormones
        upsertTest("T3", "T3 (Triiodothyronine Total)", Department.IMMUNOLOGY, VialType.SST_GEL, "180.00", "60.00", "120.00", "T3 Total", "ng/dL", "80 - 200", 80.0, 200.0, false);
        upsertTest("T4", "T4 (Thyroxine Total)", Department.IMMUNOLOGY, VialType.SST_GEL, "180.00", "60.00", "120.00", "T4 Total", "µg/dL", "5.1 - 14.1", 5.1, 14.1, false);
        upsertTest("TSH", "Thyroid Stimulating Hormone (TSH)", Department.IMMUNOLOGY, VialType.SST_GEL, "200.00", "60.00", "130.00", "TSH Ultrasensitive", "µIU/mL", "0.35 - 4.94", 0.35, 4.94, false);

        // 57. Thyroid Profile Total
        TestItem thyroid = upsertTest("THYROID_PROFILE", "Thyroid Profile Total (T3, T4, TSH)", Department.IMMUNOLOGY, VialType.SST_GEL,
                "580.00", "115.00", "340.00", null, null, null, null, null, false);
        if (thyroid.getParameters() == null || thyroid.getParameters().isEmpty()) {
            thyroid.addParameter(new TestParameter("Total T3", "ng/dL", "80 - 200", 80.0, 200.0, 1, false));
            thyroid.addParameter(new TestParameter("Total T4", "µg/dL", "5.1 - 14.1", 5.1, 14.1, 2, false));
            thyroid.addParameter(new TestParameter("TSH Ultrasensitive", "µIU/mL", "0.35 - 4.94", 0.35, 4.94, 3, false));
            testItemRepository.save(thyroid);
        }

        upsertTest("FREE_T3", "Free T3 (FT3)", Department.IMMUNOLOGY, VialType.SST_GEL, "280.00", "125.00", "200.00", "FT3 Free Triiodothyronine", "pg/mL", "2.3 - 4.2", 2.3, 4.2, false);
        upsertTest("FREE_T4", "Free T4 (FT4)", Department.IMMUNOLOGY, VialType.SST_GEL, "280.00", "125.00", "200.00", "FT4 Free Thyroxine", "ng/dL", "0.89 - 1.76", 0.89, 1.76, false);

        // 60. FNAC
        TestItem fnac = upsertTest("FNAC", "Fine Needle Aspiration Cytology (FNAC)", Department.CYTOLOGY, VialType.PLAIN,
                "1000.00", "700.00", "850.00", null, null, null, null, null, false);
        if (fnac.getParameters() == null || fnac.getParameters().isEmpty()) {
            fnac.addParameter(new TestParameter("Site of Aspiration", "", "Specified by Doctor", 0.0, 0.0, 1, false));
            fnac.addParameter(new TestParameter("Gross Examination", "", "Narrative", 0.0, 0.0, 2, false));
            fnac.addParameter(new TestParameter("Microscopic Description", "", "Narrative", 0.0, 0.0, 3, false));
            fnac.addParameter(new TestParameter("Cytological Impression", "", "Narrative", 0.0, 0.0, 4, false));
            testItemRepository.save(fnac);
        }

        // 61-72 Specialized Immunology & Prenatal
        upsertTest("PSA", "Prostate Specific Antigen (PSA Total)", Department.IMMUNOLOGY, VialType.SST_GEL, "800.00", "350.00", "575.00", "Total PSA", "ng/mL", "0.0 - 4.0", 0.0, 4.0, false);
        upsertTest("VIT_B12", "Vitamin B12", Department.IMMUNOLOGY, VialType.SST_GEL, "1150.00", "390.00", "770.00", "Vitamin B12", "pg/mL", "211 - 911", 211.0, 911.0, false);
        upsertTest("VIT_D3", "Vitamin D3 (25-OH)", Department.IMMUNOLOGY, VialType.SST_GEL, "1500.00", "550.00", "1000.00", "25-OH Vitamin D3", "ng/mL", "30.0 - 100.0", 30.0, 100.0, false);
        upsertTest("SERUM_ALBUMIN", "Serum Albumin", Department.BIOCHEMISTRY, VialType.SST_GEL, "240.00", "200.00", "220.00", "Serum Albumin", "g/dL", "3.5 - 5.0", 3.5, 5.0, false);
        upsertTest("BETA_HCG", "Beta HCG (Human Chorionic Gonadotropin)", Department.IMMUNOLOGY, VialType.SST_GEL, "1000.00", "250.00", "625.00", "Total Beta hCG", "mIU/mL", "< 5.0 (Non-Pregnant)", 0.0, 5.0, false);
        upsertTest("ANA", "Antinuclear Antibodies (ANA)", Department.IMMUNOLOGY, VialType.SST_GEL, "1600.00", "230.00", "900.00", "ANA Result", "", "Negative (< 1.0)", 0.0, 0.0, false);
        upsertTest("ANTI_CCP", "Anti-CCP Antibody", Department.IMMUNOLOGY, VialType.SST_GEL, "1600.00", "600.00", "1100.00", "Anti-CCP Titre", "U/mL", "< 17", 0.0, 17.0, false);
        upsertTest("SERUM_IGE", "Serum IgE (Total)", Department.IMMUNOLOGY, VialType.SST_GEL, "800.00", "560.00", "680.00", "Total IgE", "IU/mL", "< 100", 0.0, 100.0, false);
        upsertTest("TB_GOLD", "TB Gold (Quantiferon)", Department.IMMUNOLOGY, VialType.SST_GEL, "2500.00", "1700.00", "2100.00", "TB Gold Gamma Interferon", "", "Negative", 0.0, 0.0, false);
        upsertTest("DUAL_MARKER", "Dual Marker (First Trimester Prenatal)", Department.IMMUNOLOGY, VialType.SST_GEL, "2500.00", "1400.00", "1950.00", "Prenatal Risk Calculation", "", "Screen Negative / Low Risk", 0.0, 0.0, false);
        upsertTest("TRIPLE_MARKER", "Triple Marker (Second Trimester Prenatal)", Department.IMMUNOLOGY, VialType.SST_GEL, "2700.00", "1100.00", "1900.00", "Triple Marker Risk Assessment", "", "Low Risk", 0.0, 0.0, false);
        upsertTest("QUADRUPLE_MARKER", "Quadruple Marker Prenatal Panel", Department.IMMUNOLOGY, VialType.SST_GEL, "3700.00", "2000.00", "2850.00", "Quadruple Screen Assessment", "", "Low Risk", 0.0, 0.0, false);

        // 73-79 Routine & Occult Blood
        upsertTest("MANTOUX", "Mantoux Test (PPD)", Department.CLINICAL_PATHOLOGY, VialType.PLAIN, "150.00", "105.00", "125.00", "Induration Diameter", "mm", "< 5 mm (Negative)", 0.0, 5.0, false);
        upsertTest("AEC", "Absolute Eosinophil Count (AEC)", Department.HEMATOLOGY, VialType.EDTA, "160.00", "110.00", "135.00", "Absolute Eosinophil Count", "/µL", "40 - 450", 40.0, 450.0, false);
        upsertTest("ANC", "Absolute Neutrophil Count (ANC)", Department.HEMATOLOGY, VialType.EDTA, "160.00", "110.00", "135.00", "Absolute Neutrophil Count", "/µL", "2000 - 7000", 2000.0, 7000.0, false);
        upsertTest("HPLC", "HPLC (Hemoglobin Variant Analysis)", Department.HEMATOLOGY, VialType.EDTA, "1100.00", "450.00", "775.00", "HbA2 Percentage", "%", "1.5 - 3.5 (Normal)", 1.5, 3.5, false);
        upsertTest("BT_CT", "Bleeding Time & Clotting Time (BT/CT)", Department.HEMATOLOGY, VialType.PLAIN, "80.00", "55.00", "65.00", "Bleeding Time (Duke's)", "Minutes", "2 - 7", 2.0, 7.0, false);
        upsertTest("URINE_OCCULT_BLOOD", "Urine for Occult Blood", Department.CLINICAL_PATHOLOGY, VialType.URINE_CONTAINER, "150.00", "105.00", "125.00", "Urine Occult Blood", "", "Negative", 0.0, 0.0, false);
        upsertTest("STOOL_OCCULT_BLOOD", "Stool for Occult Blood", Department.CLINICAL_PATHOLOGY, VialType.STOOL_CONTAINER, "100.00", "70.00", "85.00", "Stool Occult Blood (FOB)", "", "Negative", 0.0, 0.0, false);

        // 80-87 Hormones & Allergies
        upsertTest("FSH", "Follicle Stimulating Hormone (FSH)", Department.IMMUNOLOGY, VialType.SST_GEL, "500.00", "130.00", "315.00", "FSH Serum", "mIU/mL", "1.5 - 12.4", 1.5, 12.4, false);
        upsertTest("LH", "Luteinizing Hormone (LH)", Department.IMMUNOLOGY, VialType.SST_GEL, "500.00", "130.00", "315.00", "LH Serum", "mIU/mL", "1.7 - 8.6", 1.7, 8.6, false);
        upsertTest("PROLACTIN", "Prolactin (Serum)", Department.IMMUNOLOGY, VialType.SST_GEL, "500.00", "130.00", "315.00", "Serum Prolactin", "ng/mL", "4.0 - 23.0", 4.0, 23.0, false);

        TestItem fshLhPrl = upsertTest("FSH_LH_PROLACTIN", "FSH - LH - Prolactin Panel", Department.IMMUNOLOGY, VialType.SST_GEL,
                "1400.00", "390.00", "895.00", null, null, null, null, null, false);
        if (fshLhPrl.getParameters() == null || fshLhPrl.getParameters().isEmpty()) {
            fshLhPrl.addParameter(new TestParameter("FSH", "mIU/mL", "1.5 - 12.4", 1.5, 12.4, 1, false));
            fshLhPrl.addParameter(new TestParameter("LH", "mIU/mL", "1.7 - 8.6", 1.7, 8.6, 2, false));
            fshLhPrl.addParameter(new TestParameter("Prolactin", "ng/mL", "4.0 - 23.0", 4.0, 23.0, 3, false));
            testItemRepository.save(fshLhPrl);
        }

        upsertTest("TESTOSTERONE", "Testosterone (Total)", Department.IMMUNOLOGY, VialType.SST_GEL, "1400.00", "190.00", "795.00", "Total Testosterone", "ng/dL", "240 - 950", 240.0, 950.0, false);
        upsertTest("ALLERGY_FOOD", "Allergy Panel (Food - Comprehensive)", Department.IMMUNOLOGY, VialType.SST_GEL, "4600.00", "4000.00", "4300.00", "Food Allergen Profile", "", "Class 0 (< 0.35 kU/L Negative)", 0.0, 0.0, false);
        upsertTest("ALLERGY_PEDIATRIC", "Allergy Panel (Pediatric)", Department.IMMUNOLOGY, VialType.SST_GEL, "5000.00", "4500.00", "4750.00", "Pediatric Allergen Panel", "", "Class 0 (Negative)", 0.0, 0.0, false);
        upsertTest("ALLERGY_RESP", "Allergy Panel (Respiratory Inhalant)", Department.IMMUNOLOGY, VialType.SST_GEL, "4600.00", "4000.00", "4300.00", "Inhalant Allergen Profile", "", "Class 0 (Negative)", 0.0, 0.0, false);

        // 88-96 FNAC, Histopathology & Cytology
        upsertTest("FNAC_1_SAMPLE", "FNAC (1 Sample / Site)", Department.CYTOLOGY, VialType.PLAIN, "1000.00", "650.00", "825.00", "Cytological Impression (1 Site)", "", "Narrative Report", 0.0, 0.0, false);
        upsertTest("FNAC_2_SAMPLE", "FNAC (2 Samples / Sites)", Department.CYTOLOGY, VialType.PLAIN, "1500.00", "975.00", "1235.00", "Cytological Impression (2 Sites)", "", "Narrative Report", 0.0, 0.0, false);
        upsertTest("HISTOPATHOLOGY_1", "Histopathology (1 Tissue / Small)", Department.CYTOLOGY, VialType.PLAIN, "800.00", "600.00", "700.00", "Microscopic Diagnosis (1 Specimen)", "", "Narrative Biopsy Report", 0.0, 0.0, false);
        upsertTest("HISTOPATHOLOGY_2", "Histopathology (2 Tissues / Medium)", Department.CYTOLOGY, VialType.PLAIN, "1600.00", "1200.00", "1400.00", "Microscopic Diagnosis (2 Specimens)", "", "Narrative Biopsy Report", 0.0, 0.0, false);
        upsertTest("HISTOPATHOLOGY_3", "Histopathology (3 Tissues / Large)", Department.CYTOLOGY, VialType.PLAIN, "2400.00", "1800.00", "2100.00", "Microscopic Diagnosis (3 Specimens)", "", "Narrative Biopsy Report", 0.0, 0.0, false);
        upsertTest("HISTOPATHOLOGY_4", "Histopathology (4 Tissues / Extensive)", Department.CYTOLOGY, VialType.PLAIN, "3200.00", "2400.00", "2800.00", "Microscopic Diagnosis (4 Specimens)", "", "Narrative Biopsy Report", 0.0, 0.0, false);
        upsertTest("HISTOPATHOLOGY_5", "Histopathology (5 Tissues)", Department.CYTOLOGY, VialType.PLAIN, "4000.00", "3000.00", "3500.00", "Microscopic Diagnosis (5 Specimens)", "", "Narrative Biopsy Report", 0.0, 0.0, false);
        upsertTest("HISTOPATHOLOGY_6", "Histopathology (6 Tissues / Complex)", Department.CYTOLOGY, VialType.PLAIN, "4800.00", "3600.00", "4200.00", "Microscopic Diagnosis (6 Specimens)", "", "Narrative Biopsy Report", 0.0, 0.0, false);
        upsertTest("PAP_SMEAR", "Pap Smear (Cervical Cytology)", Department.CYTOLOGY, VialType.PLAIN, "1000.00", "700.00", "850.00", "Bethesda Cytological Findings", "", "NILM (Negative for Intraepithelial Lesion)", 0.0, 0.0, false);

        // 97-105 Special Markers & Endocrine
        upsertTest("AMH", "Anti-Mullerian Hormone (AMH)", Department.IMMUNOLOGY, VialType.SST_GEL, "2000.00", "1200.00", "1600.00", "AMH Serum Level", "ng/mL", "1.0 - 4.0 (Optimal Fertility)", 1.0, 4.0, false);
        upsertTest("PCR_MTB", "PCR for MTB (Tuberculosis DNA)", Department.MICROBIOLOGY, VialType.SWAB, "2800.00", "900.00", "1850.00", "MTB DNA Real-Time PCR", "", "Target Not Detected (Negative)", 0.0, 0.0, false);
        upsertTest("CA_125", "CA-125 (Ovarian Tumor Marker)", Department.IMMUNOLOGY, VialType.SST_GEL, "1800.00", "500.00", "1150.00", "CA-125 Serum", "U/mL", "< 35.0", 0.0, 35.0, false);
        upsertTest("TORCH_IGG", "TORCH Panel IgG", Department.IMMUNOLOGY, VialType.SST_GEL, "1500.00", "500.00", "1000.00", "TORCH IgG Profile (Toxo, Rubella, CMV, HSV)", "", "Antibody Index Values", 0.0, 0.0, false);
        upsertTest("TORCH_IGM", "TORCH Panel IgM", Department.IMMUNOLOGY, VialType.SST_GEL, "1500.00", "500.00", "1000.00", "TORCH IgM Profile (Acute Marker)", "", "Negative (< 0.9 Index)", 0.0, 0.0, false);
        upsertTest("INSULIN_PP", "Insulin Post Prandial (PP)", Department.BIOCHEMISTRY, VialType.SST_GEL, "1500.00", "350.00", "925.00", "Post Prandial Insulin", "µIU/mL", "16.0 - 166.0", 16.0, 166.0, false);
        upsertTest("INSULIN_FASTING", "Insulin Fasting", Department.BIOCHEMISTRY, VialType.SST_GEL, "1500.00", "350.00", "925.00", "Fasting Serum Insulin", "µIU/mL", "2.6 - 24.9", 2.6, 24.9, false);

        // 104. Thyroid Profile Free
        TestItem freeThyroid = upsertTest("FREE_THYROID_PROFILE", "Thyroid Profile Free (FT3, FT4, TSH)", Department.IMMUNOLOGY, VialType.SST_GEL,
                "750.00", "250.00", "500.00", null, null, null, null, null, false);
        if (freeThyroid.getParameters() == null || freeThyroid.getParameters().isEmpty()) {
            freeThyroid.addParameter(new TestParameter("Free T3 (FT3)", "pg/mL", "2.3 - 4.2", 2.3, 4.2, 1, false));
            freeThyroid.addParameter(new TestParameter("Free T4 (FT4)", "ng/dL", "0.89 - 1.76", 0.89, 1.76, 2, false));
            freeThyroid.addParameter(new TestParameter("TSH Ultrasensitive", "µIU/mL", "0.35 - 4.94", 0.35, 4.94, 3, false));
            testItemRepository.save(freeThyroid);
        }

        // 105. Estradiol
        upsertTest("ESTRADIOL", "Estradiol (E2 Serum)", Department.IMMUNOLOGY, VialType.SST_GEL, "1500.00", "200.00", "850.00", "Serum Estradiol (E2)", "pg/mL", "Phase Dependent (12.5 - 498)", 12.5, 498.0, false);
    }

    private TestItem upsertTest(String code, String name, Department department, VialType vial,
                                String retail, String b2b, String cc,
                                String defaultParamName, String unit, String refRange,
                                Double min, Double max, boolean calculated) {
        TestItem item = testItemRepository.findByCode(code).orElseGet(() -> {
            TestItem t = new TestItem();
            t.setCode(code);
            return t;
        });
        item.setName(name);
        item.setDepartment(department);
        item.setDefaultVial(vial);
        item.setRetailPrice(new BigDecimal(retail));
        item.setB2bPrice(new BigDecimal(b2b));
        item.setCollectionCentrePrice(new BigDecimal(cc));
        item.setActive(true);

        if (defaultParamName != null && (item.getParameters() == null || item.getParameters().isEmpty())) {
            item.addParameter(new TestParameter(defaultParamName, unit, refRange, min, max, 1, calculated));
        }
        return testItemRepository.save(item);
    }
}
