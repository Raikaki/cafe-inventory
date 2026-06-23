package com.cafe.inventory.repository;

import com.cafe.inventory.entity.InventoryTransaction;
import com.cafe.inventory.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {
    List<InventoryTransaction> findByMaterialIdOrderByTxnDateDesc(Long materialId);
    List<InventoryTransaction> findTop50ByOrderByTxnDateDesc();
    List<InventoryTransaction> findByTxnType(TransactionType txnType);

    @Query("SELECT t FROM InventoryTransaction t " +
           "WHERE t.txnType = com.cafe.inventory.entity.TransactionType.SALE_CONSUMPTION " +
           "AND t.txnDate >= :since")
    List<InventoryTransaction> findConsumptionSince(@Param("since") LocalDateTime since);
}
