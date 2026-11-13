package com.lcms.repository;

import com.lcms.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    List<Doctor> findByActiveTrueOrderByNameAsc();
    List<Doctor> findByNameContainingIgnoreCase(String query);
}
