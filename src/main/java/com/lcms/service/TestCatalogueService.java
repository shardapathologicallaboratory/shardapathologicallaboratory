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
        if (testItemRepository.count() > 0) {
            return;
        }

        // 1. Complete Blood Count (CBC) - Hematology
        TestItem cbc = new TestItem("CBC", "Complete Blood Count (CBC)", Department.HEMATOLOGY, VialType.EDTA,
                new BigDecimal("350.00"), new BigDecimal("180.00"), new BigDecimal("250.00"));
        cbc.addParameter(new TestParameter("Hemoglobin (Hb)", "g/dL", "12.0 - 15.0", 12.0, 15.0, 1, false));
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

        // 2. Liver Function Test (LFT) - Biochemistry
        TestItem lft = new TestItem("LFT", "Liver Function Test (LFT)", Department.BIOCHEMISTRY, VialType.SST_GEL,
                new BigDecimal("650.00"), new BigDecimal("320.00"), new BigDecimal("450.00"));
        lft.addParameter(new TestParameter("Total Bilirubin", "mg/dL", "0.2 - 1.2", 0.2, 1.2, 1, false));
        lft.addParameter(new TestParameter("Direct Bilirubin (Conjugated)", "mg/dL", "0.0 - 0.3", 0.0, 0.3, 2, false));
        lft.addParameter(new TestParameter("Indirect Bilirubin", "mg/dL", "0.2 - 0.9", 0.2, 0.9, 3, true));
        lft.addParameter(new TestParameter("SGPT / ALT", "U/L", "10 - 40", 10.0, 40.0, 4, false));
        lft.addParameter(new TestParameter("SGOT / AST", "U/L", "10 - 38", 10.0, 38.0, 5, false));
        lft.addParameter(new TestParameter("Alkaline Phosphatase (ALP)", "U/L", "40 - 130", 40.0, 130.0, 6, false));
        lft.addParameter(new TestParameter("Total Protein", "g/dL", "6.4 - 8.3", 6.4, 8.3, 7, false));
        lft.addParameter(new TestParameter("Serum Albumin", "g/dL", "3.5 - 5.0", 3.5, 5.0, 8, false));
        lft.addParameter(new TestParameter("Serum Globulin", "g/dL", "2.0 - 3.5", 2.0, 3.5, 9, true));
        lft.addParameter(new TestParameter("A/G Ratio", "ratio", "1.1 - 2.2", 1.1, 2.2, 10, true));
        testItemRepository.save(lft);

        // 3. Kidney Function Test (KFT) - Biochemistry
        TestItem kft = new TestItem("KFT", "Kidney Function Test (KFT)", Department.BIOCHEMISTRY, VialType.SST_GEL,
                new BigDecimal("600.00"), new BigDecimal("300.00"), new BigDecimal("420.00"));
        kft.addParameter(new TestParameter("Blood Urea", "mg/dL", "15.0 - 45.0", 15.0, 45.0, 1, false));
        kft.addParameter(new TestParameter("Serum Creatinine", "mg/dL", "0.6 - 1.2", 0.6, 1.2, 2, false));
        kft.addParameter(new TestParameter("Serum Uric Acid", "mg/dL", "3.4 - 7.0", 3.4, 7.0, 3, false));
        kft.addParameter(new TestParameter("Blood Urea Nitrogen (BUN)", "mg/dL", "7.0 - 20.0", 7.0, 20.0, 4, true));
        testItemRepository.save(kft);

        // 4. Lipid Profile - Biochemistry
        TestItem lipid = new TestItem("LIPID", "Lipid Profile", Department.BIOCHEMISTRY, VialType.SST_GEL,
                new BigDecimal("700.00"), new BigDecimal("350.00"), new BigDecimal("500.00"));
        lipid.addParameter(new TestParameter("Total Cholesterol", "mg/dL", "125 - 200", 125.0, 200.0, 1, false));
        lipid.addParameter(new TestParameter("Triglycerides", "mg/dL", "50 - 150", 50.0, 150.0, 2, false));
        lipid.addParameter(new TestParameter("HDL Cholesterol", "mg/dL", "40 - 60", 40.0, 60.0, 3, false));
        lipid.addParameter(new TestParameter("LDL Cholesterol", "mg/dL", "60 - 100", 60.0, 100.0, 4, true));
        lipid.addParameter(new TestParameter("VLDL Cholesterol", "mg/dL", "10 - 30", 10.0, 30.0, 5, true));
        testItemRepository.save(lipid);

        // 5. Fasting Blood Sugar (FBS) - Biochemistry
        TestItem fbs = new TestItem("FBS", "Fasting Blood Sugar (Glucose)", Department.BIOCHEMISTRY, VialType.FLUORIDE,
                new BigDecimal("100.00"), new BigDecimal("40.00"), new BigDecimal("70.00"));
        fbs.addParameter(new TestParameter("Fasting Glucose", "mg/dL", "70 - 100", 70.0, 100.0, 1, false));
        testItemRepository.save(fbs);

        // 6. Urine Routine & Microscopy - Clinical Pathology
        TestItem urine = new TestItem("URINE_RM", "Urine Routine & Microscopy", Department.CLINICAL_PATHOLOGY, VialType.URINE_CONTAINER,
                new BigDecimal("150.00"), new BigDecimal("70.00"), new BigDecimal("100.00"));
        urine.addParameter(new TestParameter("Color & Appearance", "", "Pale Yellow / Clear", 0.0, 0.0, 1, false));
        urine.addParameter(new TestParameter("Specific Gravity", "", "1.010 - 1.025", 1.010, 1.025, 2, false));
        urine.addParameter(new TestParameter("pH", "", "5.0 - 7.5", 5.0, 7.5, 3, false));
        urine.addParameter(new TestParameter("Urine Albumin (Protein)", "", "Nil", 0.0, 0.0, 4, false));
        urine.addParameter(new TestParameter("Urine Sugar", "", "Nil", 0.0, 0.0, 5, false));
        urine.addParameter(new TestParameter("Pus Cells", "/HPF", "0 - 5", 0.0, 5.0, 6, false));
        urine.addParameter(new TestParameter("RBCs", "/HPF", "Nil / 0-2", 0.0, 2.0, 7, false));
        urine.addParameter(new TestParameter("Epithelial Cells", "/HPF", "Few / 1-4", 0.0, 4.0, 8, false));
        testItemRepository.save(urine);

        // 7. Typhidot IgM & IgG - Serology & Immunology
        TestItem typhidot = new TestItem("TYPHIDOT", "Typhidot IgM & IgG", Department.SEROLOGY_IMMUNOLOGY, VialType.PLAIN,
                new BigDecimal("400.00"), new BigDecimal("200.00"), new BigDecimal("280.00"));
        typhidot.addParameter(new TestParameter("Typhidot IgM", "", "Negative", 0.0, 0.0, 1, false));
        typhidot.addParameter(new TestParameter("Typhidot IgG", "", "Negative", 0.0, 0.0, 2, false));
        testItemRepository.save(typhidot);

        // 8. FNAC / Cytology (Req #16 with Image Support)
        TestItem fnac = new TestItem("FNAC", "Fine Needle Aspiration Cytology (FNAC)", Department.CYTOLOGY_HISTO, VialType.PLAIN,
                new BigDecimal("1200.00"), new BigDecimal("700.00"), new BigDecimal("900.00"));
        fnac.addParameter(new TestParameter("Site of Aspiration", "", "Specified by Doctor", 0.0, 0.0, 1, false));
        fnac.addParameter(new TestParameter("Gross Examination", "", "Narrative", 0.0, 0.0, 2, false));
        fnac.addParameter(new TestParameter("Microscopic Description", "", "Narrative", 0.0, 0.0, 3, false));
        fnac.addParameter(new TestParameter("Cytological Impression", "", "Narrative", 0.0, 0.0, 4, false));
        testItemRepository.save(fnac);
    }
}
