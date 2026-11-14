package com.lcms.repository;

import com.lcms.model.FeeAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeeAdjustmentRepository extends JpaRepository<FeeAdjustment, Long> {
    List<FeeAdjustment> findByOrderIdOrderByCreatedAtDesc(Long orderId);
    List<FeeAdjustment> findTop100ByOrderByCreatedAtDesc();
    Optional<FeeAdjustment> findByVoucherNumber(String voucherNumber);
}
