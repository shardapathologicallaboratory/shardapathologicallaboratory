package com.lcms.config;

import com.lcms.model.Doctor;
import com.lcms.model.LabOrder;
import com.lcms.model.Patient;
import com.lcms.model.TestResult;
import com.lcms.model.User;
import com.lcms.model.enums.*;
import com.lcms.repository.DoctorRepository;
import com.lcms.repository.TestResultRepository;
import com.lcms.repository.UserRepository;
import com.lcms.service.ClinicalWorksheetService;
import com.lcms.service.OrderService;
import com.lcms.service.PatientService;
import com.lcms.service.TestCatalogueService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Configuration
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PatientService patientService;
    private final TestCatalogueService catalogueService;
    private final OrderService orderService;
    private final ClinicalWorksheetService worksheetService;
    private final TestResultRepository testResultRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, DoctorRepository doctorRepository,
                           PatientService patientService, TestCatalogueService catalogueService,
                           OrderService orderService, ClinicalWorksheetService worksheetService,
                           TestResultRepository testResultRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.patientService = patientService;
        this.catalogueService = catalogueService;
        this.orderService = orderService;
        this.worksheetService = worksheetService;
        this.testResultRepository = testResultRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        seedDoctors();
        catalogueService.seedInitialCatalogueIfEmpty();
        seedSamplePatientsAndOrders();
    }

    private void seedUsers() {
        Set<Department> allActiveDepts = Set.of(
                Department.HEMATOLOGY,
                Department.BIOCHEMISTRY,
                Department.CLINICAL_PATHOLOGY,
                Department.IMMUNOLOGY,
                Department.SEROLOGY,
                Department.CYTOLOGY,
                Department.MICROBIOLOGY
        );

        // 1. Lab Admin - Sandeep
        createOrUpdateUser("sandeep", "sandeep123", "Sandeep (Lab Admin)", "9876543201",
                Role.ROLE_ADMIN, allActiveDepts);

        createOrUpdateUser("admin", "admin123", "System Administrator", "9876543210",
                Role.ROLE_ADMIN, allActiveDepts);

        // 2. Biochemistry - (pancham, rahul, sanjeet)
        createOrUpdateUser("pancham", "pancham123", "Pancham", "9876543221",
                Role.ROLE_TECHNICIAN, Set.of(Department.BIOCHEMISTRY, Department.IMMUNOLOGY));

        createOrUpdateUser("rahul", "rahul123", "Rahul", "9876543222",
                Role.ROLE_TECHNICIAN, Set.of(Department.BIOCHEMISTRY));

        createOrUpdateUser("sanjeet", "sanjeet123", "Sanjeet", "9876543223",
                Role.ROLE_TECHNICIAN, Set.of(Department.BIOCHEMISTRY));

        // 3. Haemoto & Clinical - (satyam, sudhir)
        createOrUpdateUser("satyam", "satyam123", "Satyam", "9876543224",
                Role.ROLE_TECHNICIAN, Set.of(Department.HEMATOLOGY, Department.CLINICAL_PATHOLOGY));

        createOrUpdateUser("sudhir", "sudhir123", "Sudhir", "9876543225",
                Role.ROLE_TECHNICIAN, Set.of(Department.HEMATOLOGY, Department.CLINICAL_PATHOLOGY));

        // 4. Immunology, Serology & Cytology - (pancham, neha)
        createOrUpdateUser("neha", "neha123", "Neha", "9876543226",
                Role.ROLE_TECHNICIAN, Set.of(Department.IMMUNOLOGY, Department.SEROLOGY, Department.CYTOLOGY));

        // 5. Reception - (sapna, sonu)
        createOrUpdateUser("sapna", "sapna123", "Sapna", "9876543227",
                Role.ROLE_RECEPTION, Set.of());

        createOrUpdateUser("sonu", "sonu123", "Sonu", "9876543228",
                Role.ROLE_RECEPTION, Set.of());

        // 6. Collection - (gautam, adarsh, payal)
        createOrUpdateUser("gautam", "gautam123", "Gautam", "9876543229",
                Role.ROLE_COLLECTION, Set.of());

        createOrUpdateUser("adarsh", "adarsh123", "Adarsh", "9876543230",
                Role.ROLE_COLLECTION, Set.of());

        createOrUpdateUser("payal", "payal123", "Payal", "9876543231",
                Role.ROLE_COLLECTION, Set.of());

        // 7. Reporting - Aman
        createOrUpdateUser("aman", "aman123", "Aman (Reporting Desk)", "9876543232",
                Role.ROLE_REPORTING, Set.of());

        createOrUpdateUser("reporting", "report123", "Reporting Desk", "9876543233",
                Role.ROLE_REPORTING, Set.of());

        // 8. Finance / Accountant
        createOrUpdateUser("accountant", "account123", "Senior Accountant", "9876543234",
                Role.ROLE_ACCOUNTANT, Set.of());
    }

    private void createOrUpdateUser(String username, String rawPassword, String fullName, String phone, Role role, Set<Department> departments) {
        userRepository.findByUsername(username).ifPresentOrElse(u -> {
            u.setFullName(fullName);
            u.setRole(role);
            u.setAllowedDepartments(departments);
            u.setActive(true);
            userRepository.save(u);
        }, () -> {
            User newUser = new User(username, passwordEncoder.encode(rawPassword), fullName, phone, role, departments);
            userRepository.save(newUser);
        });
    }

    private void seedDoctors() {
        if (doctorRepository.count() > 0) {
            return;
        }

        doctorRepository.save(new Doctor(
                "Dr. Vikash Chandra Agrawal",
                "MD, DCH (Pediatrics)",
                "Agrawal Child Care Hospital",
                "9431100001",
                "dr.agrawal@example.com"
        ));

        doctorRepository.save(new Doctor(
                "Dr. S. K. Verma",
                "MBBS, MD (General Medicine)",
                "City Life Clinic",
                "9431100002",
                "dr.verma@example.com"
        ));

        doctorRepository.save(new Doctor(
                "Dr. Anita Sinha",
                "MBBS, MS (Obs & Gynae)",
                "Maternity & Nursing Home",
                "9431100003",
                "dr.anita@example.com"
        ));

        doctorRepository.save(new Doctor(
                "Self / Walk-in Patient",
                "-",
                "Direct Walk-in",
                "-",
                "-"
        ));
    }

    @Transactional
    public void seedSamplePatientsAndOrders() {
        if (!patientService.search("").isEmpty()) {
            return;
        }

        // 1. Patient: Shivansh Tiwari (matches the user's authentic reference lab sheet!)
        Patient p1 = new Patient("PAT-26-00001", "Master", "Shivansh Tiwari", 3, 6, Gender.MALE, "9876543210", "DLT Road, Ranchi");
        p1.setBloodGroup("O+");
        patientService.registerPatient(p1);

        // 2. Patient: Anita Devi
        Patient p2 = new Patient("PAT-26-00002", "Mrs.", "Anita Devi", 45, 0, Gender.FEMALE, "9876543222", "Kanke Road, Ranchi");
        p2.setBloodGroup("B+");
        patientService.registerPatient(p2);

        // 3. Patient: Rajesh Verma
        Patient p3 = new Patient("PAT-26-00003", "Mr.", "Rajesh Verma", 52, 0, Gender.MALE, "9876543233", "Bariatu, Ranchi");
        p3.setBloodGroup("A+");
        patientService.registerPatient(p3);

        List<Doctor> doctors = doctorRepository.findAll();
        Doctor drAgrawal = doctors.stream().filter(d -> d.getName().contains("Agrawal")).findFirst().orElse(doctors.get(0));
        Doctor drVerma = doctors.stream().filter(d -> d.getName().contains("Verma")).findFirst().orElse(doctors.get(0));

        var cbc = catalogueService.findByCode("CBC").orElse(null);
        var typhidot = catalogueService.findByCode("TYPHIDOT").orElse(null);
        var lft = catalogueService.findByCode("LFT").orElse(null);
        var kft = catalogueService.findByCode("KFT").orElse(null);
        var fbs = catalogueService.findByCode("FBS").orElse(null);

        // Order 1: Shivansh Tiwari - Collection Centre (DLT-22) - CBC & Typhidot
        if (cbc != null && typhidot != null) {
            LabOrder order1 = orderService.createOrder(
                    p1.getId(), drAgrawal, PatientSource.COLLECTION_CENTRE, "DLT-22",
                    List.of(cbc.getId(), typhidot.getId()), null,
                    BigDecimal.ZERO, "", new BigDecimal("530.00"), PaymentMode.CASH, "",
                    "Sapna", "Fever x 4 days"
            );

            // Populate sample verified results for CBC & Typhidot
            List<TestResult> results1 = worksheetService.getOrInitializeResults(order1);
            Map<Long, String> values = new java.util.HashMap<>();
            for (TestResult r : results1) {
                String name = r.getParameterName().toLowerCase();
                if (name.contains("hemoglobin")) {
                    values.put(r.getId(), "13.8");
                } else if (name.contains("total leucocyte") || name.contains("tlc") || name.contains("wbc")) {
                    values.put(r.getId(), "7400");
                } else if (name.contains("platelet")) {
                    values.put(r.getId(), "2.6");
                } else if (name.contains("rbc")) {
                    values.put(r.getId(), "4.5");
                } else if (name.contains("typhidot igm")) {
                    values.put(r.getId(), "Positive (+)");
                } else if (name.contains("typhidot igg")) {
                    values.put(r.getId(), "Negative (-)");
                }
            }
            worksheetService.saveWorksheetResults(order1.getId(), values, java.util.Map.of(), "satyam", java.util.Set.of(), true);
            worksheetService.verifyAllResultsForOrder(order1.getId(), "Dr. S. K. Verma, MD (Path)");
        }

        // Order 2: Anita Devi - Walk-in - LFT, KFT, FBS
        if (lft != null && kft != null && fbs != null) {
            orderService.createOrder(
                    p2.getId(), drVerma, PatientSource.WALK_IN, "MAIN",
                    List.of(lft.getId(), kft.getId(), fbs.getId()), null,
                    new BigDecimal("50.00"), "Senior citizen discount", new BigDecimal("1300.00"), PaymentMode.UPI, "UPI/2611/849201",
                    "Sapna", "Routine health checkup"
            );
        }
    }
}
