package com.cafe.inventory.repository;

import com.cafe.inventory.entity.InventoryTransaction;
import com.cafe.inventory.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {
    List<InventoryTransaction> findByMaterialIdOrderByTxnDateDesc(Long materialId);
    List<InventoryTransaction> findTop50ByOrderByTxnDateDesc();
    List<InventoryTransaction> findByTxnType(TransactionType txnType);
}
