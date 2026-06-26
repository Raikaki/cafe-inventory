package com.cafe.inventory.repository;

import com.cafe.inventory.entity.StockCount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockCountRepository extends JpaRepository<StockCount, Long> {
    long countByCountNoStartingWith(String prefix);
    List<StockCount> findTop50ByOrderByCountDateDescIdDesc();
}
