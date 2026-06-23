package com.cafe.inventory.repository;

import com.cafe.inventory.entity.StockPeriodSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockPeriodSummaryRepository extends JpaRepository<StockPeriodSummary, Long> {

    List<StockPeriodSummary> findByPeriodYearAndPeriodMonthOrderByMaterialCode(int year, int month);

    void deleteByPeriodYearAndPeriodMonth(int year, int month);
}
