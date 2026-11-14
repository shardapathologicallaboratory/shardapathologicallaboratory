package com.lcms.repository;

import com.lcms.model.RecheckFlag;
import com.lcms.model.enums.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecheckFlagRepository extends JpaRepository<RecheckFlag, Long> {
    List<RecheckFlag> findByOrderIdOrderByCreatedAtDesc(Long orderId);
    List<RecheckFlag> findByTargetDepartmentAndResolvedFalseOrderByCreatedAtDesc(Department targetDepartment);
    long countByTargetDepartmentAndResolvedFalse(Department targetDepartment);
}
