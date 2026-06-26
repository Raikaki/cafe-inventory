package com.cafe.inventory.repository;

import com.cafe.inventory.entity.GoodsReceiptDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface GoodsReceiptDetailRepository extends JpaRepository<GoodsReceiptDetail, Long> {

    @Query("SELECT d.materialId AS materialId, d.goodsReceipt.supplierId AS supplierId, " +
           "d.unitPrice AS unitPrice, d.goodsReceipt.receiptDate AS receiptDate " +
           "FROM GoodsReceiptDetail d WHERE d.goodsReceipt.supplierId IS NOT NULL")
    List<HistPrice> historicalPrices();

    interface HistPrice {
        Long getMaterialId();
        Long getSupplierId();
        BigDecimal getUnitPrice();
        LocalDate getReceiptDate();
    }
}
