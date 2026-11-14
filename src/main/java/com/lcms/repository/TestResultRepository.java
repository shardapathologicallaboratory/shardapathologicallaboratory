package com.lcms.repository;

import com.lcms.model.TestResult;
import com.lcms.model.enums.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestResultRepository extends JpaRepository<TestResult, Long> {
    List<TestResult> findByOrderItemIdOrderBySortOrderAsc(Long orderItemId);
    List<TestResult> findByOrderIdOrderByDepartmentAscSortOrderAsc(Long orderId);
    List<TestResult> findByOrderIdAndDepartmentOrderBySortOrderAsc(Long orderId, Department department);

    @Query("SELECT r FROM TestResult r WHERE r.patient.id = :patientId " +
           "AND LOWER(r.parameterName) = LOWER(:parameterName) " +
           "AND r.status = 'VERIFIED' " +
           "AND r.id <> :currentResultId " +
           "ORDER BY r.verifiedAt DESC")
    List<TestResult> findPastVerifiedResults(
            @Param("patientId") Long patientId,
            @Param("parameterName") String parameterName,
            @Param("currentResultId") Long currentResultId
    );
}
