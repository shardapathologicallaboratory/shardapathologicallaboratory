package com.lcms.repository;

import com.lcms.model.TestItem;
import com.lcms.model.enums.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestItemRepository extends JpaRepository<TestItem, Long> {
    Optional<TestItem> findByCode(String code);
    List<TestItem> findByActiveTrueOrderByDepartmentAscNameAsc();
    List<TestItem> findByDepartmentAndActiveTrue(Department department);

    @Query("SELECT t FROM TestItem t WHERE t.active = true AND (" +
           "LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(t.code) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<TestItem> searchActiveTests(@Param("query") String query);
}
