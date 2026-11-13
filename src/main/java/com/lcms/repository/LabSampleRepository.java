package com.lcms.repository;

import com.lcms.model.LabSample;
import com.lcms.model.enums.Department;
import com.lcms.model.enums.SampleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LabSampleRepository extends JpaRepository<LabSample, Long> {
    Optional<LabSample> findBySampleSequenceId(Long sampleSequenceId);
    Optional<LabSample> findByBarcode(String barcode);
    List<LabSample> findByStatus(SampleStatus status);
    List<LabSample> findByDepartmentAndStatus(Department department, SampleStatus status);
    List<LabSample> findByOrderId(Long orderId);

    @Query("SELECT MAX(s.sampleSequenceId) FROM LabSample s")
    Long findMaxSampleSequenceId();

    long countByStatus(SampleStatus status);
}
