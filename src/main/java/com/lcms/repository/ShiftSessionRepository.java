package com.lcms.repository;

import com.lcms.model.ShiftSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShiftSessionRepository extends JpaRepository<ShiftSession, Long> {
    Optional<ShiftSession> findTopByUserIdAndStatusOrderByStartTimeDesc(Long userId, String status);
    Optional<ShiftSession> findTopByStatusOrderByStartTimeDesc(String status);
    List<ShiftSession> findAllByOrderByStartTimeDesc();

    @Query("SELECT COUNT(s) FROM ShiftSession s WHERE s.startTime >= CURRENT_DATE")
    long countTodayShifts();
}
