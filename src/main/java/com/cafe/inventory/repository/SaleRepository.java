package com.cafe.inventory.repository;

import com.cafe.inventory.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    List<Sale> findBySaleDate(LocalDate saleDate);

    @Query("SELECT COALESCE(SUM(s.quantity), 0) FROM Sale s WHERE s.saleDate = :date")
    java.math.BigDecimal sumQuantityByDate(@Param("date") LocalDate date);

    @Query("SELECT s.productId AS productId, SUM(s.quantity) AS qty " +
           "FROM Sale s GROUP BY s.productId ORDER BY SUM(s.quantity) DESC")
    List<TopProduct> findTopProducts();

    interface TopProduct {
        Long getProductId();
        java.math.BigDecimal getQty();
    }
}
