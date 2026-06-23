package com.cafe.inventory.repository;

import com.cafe.inventory.entity.GoodsReceipt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoodsReceiptRepository extends JpaRepository<GoodsReceipt, Long> {
    long countByReceiptNoStartingWith(String prefix);
}
