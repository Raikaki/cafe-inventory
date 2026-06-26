package com.cafe.inventory.repository;

import com.cafe.inventory.entity.MaterialBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MaterialBatchRepository extends JpaRepository<MaterialBatch, Long> {

    @Query("SELECT b FROM MaterialBatch b WHERE b.remainingQty > 0 " +
           "ORDER BY CASE WHEN b.expiryDate IS NULL THEN 1 ELSE 0 END, b.expiryDate ASC, b.id ASC")
    List<MaterialBatch> findActiveFefo();
}
