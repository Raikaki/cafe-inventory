package com.cafe.inventory.service;

import com.cafe.inventory.entity.InventoryTransaction;
import com.cafe.inventory.entity.Material;
import com.cafe.inventory.entity.TransactionType;
import com.cafe.inventory.exception.ResourceNotFoundException;
import com.cafe.inventory.repository.InventoryTransactionRepository;
import com.cafe.inventory.repository.MaterialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Central inventory engine: every quantity movement goes through here so that
 * the material balance and the inventory_transactions ledger stay consistent.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final MaterialRepository materialRepository;
    private final InventoryTransactionRepository txnRepository;

    /** Increase stock (goods receipt) and recompute weighted average cost. */
    @Transactional
    public InventoryTransaction receive(Long materialId, BigDecimal qty, BigDecimal unitCost,
                                        String referenceNo, String user) {
        Material m = getMaterial(materialId);
        BigDecimal before = m.getCurrentQty();
        BigDecimal after = before.add(qty);

        // weighted average: (before*avg + qty*unitCost) / after
        if (after.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal totalValue = before.multiply(m.getAverageCost())
                    .add(qty.multiply(unitCost));
            m.setAverageCost(totalValue.divide(after, 2, RoundingMode.HALF_UP));
        }
        m.setCurrentQty(after);
        materialRepository.save(m);

        return writeTxn(TransactionType.RECEIPT, materialId, qty, before, after,
                unitCost, referenceNo, user, null);
    }

    /** Decrease stock (sales consumption). Allows going negative but warns. */
    @Transactional
    public InventoryTransaction consume(Long materialId, BigDecimal qty,
                                        String referenceNo, String user, String note) {
        Material m = getMaterial(materialId);
        BigDecimal before = m.getCurrentQty();
        BigDecimal after = before.subtract(qty);
        m.setCurrentQty(after);
        materialRepository.save(m);

        return writeTxn(TransactionType.SALE_CONSUMPTION, materialId, qty.negate(),
                before, after, m.getAverageCost(), referenceNo, user, note);
    }

    /** Manual adjustment (increase or decrease). signedQty: + increase, - decrease. */
    @Transactional
    public InventoryTransaction adjust(Long materialId, BigDecimal signedQty,
                                       String referenceNo, String user, String reason) {
        Material m = getMaterial(materialId);
        BigDecimal before = m.getCurrentQty();
        BigDecimal after = before.add(signedQty);
        m.setCurrentQty(after);
        materialRepository.save(m);

        return writeTxn(TransactionType.ADJUSTMENT, materialId, signedQty, before, after,
                m.getAverageCost(), referenceNo, user, reason);
    }

    private InventoryTransaction writeTxn(TransactionType type, Long materialId, BigDecimal qty,
                                          BigDecimal before, BigDecimal after, BigDecimal unitCost,
                                          String refNo, String user, String note) {
        InventoryTransaction txn = new InventoryTransaction();
        txn.setTxnDate(LocalDateTime.now());
        txn.setTxnType(type);
        txn.setReferenceNo(refNo);
        txn.setMaterialId(materialId);
        txn.setQuantity(qty);
        txn.setBeforeQty(before);
        txn.setAfterQty(after);
        txn.setUnitCost(unitCost == null ? BigDecimal.ZERO : unitCost);
        txn.setNote(note);
        txn.setCreatedBy(user);
        return txnRepository.save(txn);
    }

    private Material getMaterial(Long materialId) {
        return materialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Material not found: " + materialId));
    }
}
