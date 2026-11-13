package com.lcms.config;

import com.lcms.model.Doctor;
import com.lcms.model.Patient;
import com.lcms.model.User;
import com.lcms.model.enums.*;
import com.lcms.repository.DoctorRepository;
import com.lcms.repository.UserRepository;
import com.lcms.service.OrderService;
import com.lcms.service.PatientService;
import com.lcms.service.TestCatalogueService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Configuration
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PatientService patientService;
    private final TestCatalogueService catalogueService;
    private final OrderService orderService;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, DoctorRepository doctorRepository,
                           PatientService patientService, TestCatalogueService catalogueService,
                           OrderService orderService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.patientService = patientService;
        this.catalogueService = catalogueService;
        this.orderService = orderService;
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
        if (userRepository.count() > 0) {
            return;
        }

        // 1. Lab Admin / Director
        User admin = new User(
                "admin",
                passwordEncoder.encode("admin123"),
                "Dr. Aman Kumar Sharma (Lab Director)",
                "9876543210",
                Role.ROLE_ADMIN,
                Set.of(Department.values())
        );
        userRepository.save(admin);

        // 2. Receptionist
        User reception = new User(
                "reception",
                passwordEncoder.encode("reception123"),
                "Priya Sharma (Front Desk)",
                "9876543211",
                Role.ROLE_RECEPTION,
                Set.of()
        );
        userRepository.save(reception);

        // 3. Phlebotomist / Collection
        User phlebo = new User(
                "phlebotomist",
                passwordEncoder.encode("phlebo123"),
                "Sunil Soren (Phlebotomist)",
                "9876543212",
                Role.ROLE_COLLECTION,
                Set.of()
        );
        userRepository.save(phlebo);

        // 4. Hematology Technician
        User hemaTech = new User(
                "tech_hema",
                passwordEncoder.encode("hema123"),
                "Vikas Verma (Hematology Tech)",
                "9876543213",
                Role.ROLE_TECHNICIAN,
                Set.of(Department.HEMATOLOGY)
        );
        userRepository.save(hemaTech);

        // 5. Biochemistry Technician
        User bioTech = new User(
                "tech_bio",
                passwordEncoder.encode("bio123"),
                "Rakesh Kumar (Biochemistry Tech)",
                "9876543214",
                Role.ROLE_TECHNICIAN,
                Set.of(Department.BIOCHEMISTRY)
        );
        userRepository.save(bioTech);

        // 6. Clinical Pathology Technician
        User clinTech = new User(
                "tech_clinpath",
                passwordEncoder.encode("clin123"),
                "Anjali Kumari (Clinical Path Tech)",
                "9876543215",
                Role.ROLE_TECHNICIAN,
                Set.of(Department.CLINICAL_PATHOLOGY)
        );
        userRepository.save(clinTech);

        // 7. Reporting & Dispatch Desk
        User reporting = new User(
                "reporting",
                passwordEncoder.encode("report123"),
                "Rohit Raj (Report Dispatch)",
                "9876543216",
                Role.ROLE_REPORTING,
                Set.of(Department.CLINICAL_PATHOLOGY)
        );
        userRepository.save(reporting);

        // 8. Accountant
        User accountant = new User(
                "accountant",
                passwordEncoder.encode("account123"),
                "Deepak Lal (Senior Accountant)",
                "9876543217",
                Role.ROLE_ACCOUNTANT,
                Set.of()
        );
        userRepository.save(accountant);
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

    private void seedSamplePatientsAndOrders() {
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
            orderService.createOrder(
                    p1.getId(), drAgrawal, PatientSource.COLLECTION_CENTRE, "DLT-22",
                    List.of(cbc.getId(), typhidot.getId()), null,
                    BigDecimal.ZERO, "", new BigDecimal("530.00"), PaymentMode.CASH, "",
                    "Priya Sharma", "Fever x 4 days"
            );
        }

        // Order 2: Anita Devi - Walk-in - LFT, KFT, FBS
        if (lft != null && kft != null && fbs != null) {
            orderService.createOrder(
                    p2.getId(), drVerma, PatientSource.WALK_IN, "MAIN",
                    List.of(lft.getId(), kft.getId(), fbs.getId()), null,
                    new BigDecimal("50.00"), "Senior citizen discount", new BigDecimal("1300.00"), PaymentMode.UPI, "UPI/2611/849201",
                    "Priya Sharma", "Routine health checkup"
            );
        }
    }
}
