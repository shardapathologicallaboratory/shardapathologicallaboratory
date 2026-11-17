package com.lcms.repository;

import com.lcms.model.LabOrder;
import com.lcms.model.enums.OrderStatus;
import com.lcms.model.enums.PatientSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LabOrderRepository extends JpaRepository<LabOrder, Long> {
    Optional<LabOrder> findByOrderNumber(String orderNumber);
    Optional<LabOrder> findByVerificationToken(String verificationToken);
    List<LabOrder> findByPatientIdOrderByOrderDateDescOrderTimeDesc(Long patientId);
    List<LabOrder> findByOrderDateOrderByDailyTokenNumberAsc(LocalDate date);
    List<LabOrder> findByStatusInOrderByOrderDateDescOrderTimeDesc(List<OrderStatus> statuses);
    List<LabOrder> findTop100ByOrderByOrderDateDescOrderTimeDesc();
    
    long countByOrderDate(LocalDate date);
    long countByOrderDateAndSource(LocalDate date, PatientSource source);
    long countByStatus(OrderStatus status);

    @Query("SELECT MAX(o.dailyTokenNumber) FROM LabOrder o WHERE o.orderDate = :date")
    Integer findMaxTokenForDate(@Param("date") LocalDate date);

    @Query("SELECT COALESCE(MAX(o.id), 0) FROM LabOrder o")
    Long findMaxOrderId();

    @Query("SELECT DISTINCT o FROM LabOrder o " +
           "JOIN o.patient p " +
           "JOIN o.doctor d " +
           "LEFT JOIN o.items i " +
           "WHERE (:query IS NULL OR :query = '' OR " +
           "       LOWER(p.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "       p.phone LIKE CONCAT('%', :query, '%') OR " +
           "       p.uhid LIKE CONCAT('%', :query, '%') OR " +
           "       o.orderNumber LIKE CONCAT('%', :query, '%')) " +
           "AND (:startDate IS NULL OR o.orderDate >= :startDate) " +
           "AND (:endDate IS NULL OR o.orderDate <= :endDate) " +
           "AND (:source IS NULL OR o.source = :source) " +
           "AND (:doctorId IS NULL OR d.id = :doctorId) " +
           "AND (:status IS NULL OR o.status = :status) " +
           "ORDER BY o.orderDate DESC, o.orderTime DESC")
    List<LabOrder> multiVectorSearch(
            @Param("query") String query,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("source") PatientSource source,
            @Param("doctorId") Long doctorId,
            @Param("status") OrderStatus status
    );
}
