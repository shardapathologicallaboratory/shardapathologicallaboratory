package com.lcms.config;

import com.lcms.model.Doctor;
import com.lcms.model.User;
import com.lcms.model.enums.Department;
import com.lcms.model.enums.Role;
import com.lcms.repository.DoctorRepository;
import com.lcms.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, DoctorRepository doctorRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        seedDoctors();
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
                Set.of(Department.CLINICAL_PATHOLOGY) // Can enter manual urine/stool tests
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
}
