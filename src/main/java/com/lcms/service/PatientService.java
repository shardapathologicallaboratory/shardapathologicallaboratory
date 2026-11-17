package com.lcms.service;

import com.lcms.model.Patient;
import com.lcms.repository.PatientRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final AtomicLong patientCounter = new AtomicLong(0L);

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @PostConstruct
    public void initPatientCounter() {
        Long maxId = patientRepository.findMaxPatientId();
        if (maxId != null) {
            patientCounter.set(maxId);
        }
    }

    public List<Patient> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            return patientRepository.findAll();
        }
        return patientRepository.searchPatients(query.trim());
    }

    public Optional<Patient> findById(Long id) {
        return patientRepository.findById(id);
    }

    public Optional<Patient> findByUhid(String uhid) {
        return patientRepository.findByUhid(uhid);
    }

    @Transactional
    public Patient registerPatient(Patient patient) {
        if (patient.getUhid() == null || patient.getUhid().trim().isEmpty()) {
            patient.setUhid(generateNextUhid());
        }
        patient.setLocked(true);
        patient.setCreatedAt(LocalDateTime.now());
        patient.setUpdatedAt(LocalDateTime.now());
        return patientRepository.save(patient);
    }

    @Transactional
    public Patient updateDemographics(Long id, String salutation, String fullName, Integer ageYears, Integer ageMonths, String phone, String address, String bloodGroup) {
        Patient p = patientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found with id: " + id));

        p.setSalutation(salutation);
        p.setFullName(fullName);
        p.setAgeYears(ageYears);
        p.setAgeMonths(ageMonths);
        p.setPhone(phone);
        p.setAddress(address);
        p.setBloodGroup(bloodGroup);
        p.setUpdatedAt(LocalDateTime.now());
        p.setLocked(true); // Re-lock after intentional edit
        return patientRepository.save(p);
    }

    private String generateNextUhid() {
        int year = LocalDate.now().getYear() % 100;
        long nextId = patientCounter.incrementAndGet();
        return String.format("PAT-%02d-%05d", year, nextId);
    }
}
