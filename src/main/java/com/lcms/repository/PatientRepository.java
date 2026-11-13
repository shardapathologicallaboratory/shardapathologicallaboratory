package com.lcms.repository;

import com.lcms.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByUhid(String uhid);
    List<Patient> findByPhone(String phone);

    @Query("SELECT p FROM Patient p WHERE " +
           "LOWER(p.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "p.uhid LIKE CONCAT('%', :query, '%') OR " +
           "p.phone LIKE CONCAT('%', :query, '%')")
    List<Patient> searchPatients(@Param("query") String query);

    @Query("SELECT COUNT(p) FROM Patient p")
    long countTotalPatients();
}
