package com.cafe.inventory.repository;

import com.cafe.inventory.entity.PeriodLock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PeriodLockRepository extends JpaRepository<PeriodLock, Long> {
    Optional<PeriodLock> findByPeriodYearAndPeriodMonth(int year, int month);
    boolean existsByPeriodYearAndPeriodMonth(int year, int month);
    void deleteByPeriodYearAndPeriodMonth(int year, int month);
    List<PeriodLock> findAllByOrderByPeriodYearDescPeriodMonthDesc();
}
