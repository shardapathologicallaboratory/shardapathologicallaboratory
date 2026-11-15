package com.lcms.repository;

import com.lcms.model.LabSample;
import com.lcms.model.enums.Department;
import com.lcms.model.enums.SampleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LabSampleRepository extends JpaRepository<LabSample, Long> {
    Optional<LabSample> findBySampleSequenceId(Long sampleSequenceId);
    Optional<LabSample> findByBarcode(String barcode);
    List<LabSample> findByStatusOrderBySampleSequenceIdAsc(SampleStatus status);
    List<LabSample> findTop30ByStatusOrderByCollectedAtDesc(SampleStatus status);
    List<LabSample> findByDepartmentAndStatus(Department department, SampleStatus status);
    List<LabSample> findByOrderId(Long orderId);

    @Query("SELECT s FROM LabSample s WHERE s.status = 'RECOLLECTION_REQUESTED' ORDER BY s.id DESC")
    List<LabSample> findRecollectionAlerts();

    @Query("SELECT MAX(s.sampleSequenceId) FROM LabSample s")
    Long findMaxSampleSequenceId();

    long countByStatus(SampleStatus status);

    @Query("SELECT s FROM LabSample s " +
           "JOIN FETCH s.order o " +
           "JOIN FETCH s.patient p " +
           "JOIN FETCH o.doctor d " +
           "WHERE (:department IS NULL OR s.department = :department) " +
           "AND (:startTime IS NULL OR s.collectedAt >= :startTime OR (s.collectedAt IS NULL AND o.orderTime >= :startTime)) " +
           "AND (:endTime IS NULL OR s.collectedAt <= :endTime OR (s.collectedAt IS NULL AND o.orderTime <= :endTime)) " +
           "ORDER BY s.sampleSequenceId ASC")
    List<LabSample> findLabRunSheetSamples(
            @Param("department") Department department,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}
