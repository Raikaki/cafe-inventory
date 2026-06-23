package com.cafe.inventory.service;

import com.cafe.inventory.dto.InventoryBalanceDtos.*;
import com.cafe.inventory.entity.InventoryTransaction;
import com.cafe.inventory.entity.Material;
import com.cafe.inventory.entity.TransactionType;
import com.cafe.inventory.repository.InventoryTransactionRepository;
import com.cafe.inventory.repository.MaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Inventory balance report per material over a date range:
 * Opening + Receipt - Consumption + Adjustment = Closing.
 * Opening/Closing are derived from the inventory_transactions ledger so they
 * stay accurate even when the material was seeded with an initial quantity.
 */
@Service
@RequiredArgsConstructor
public class InventoryBalanceService {

    private final MaterialRepository materialRepository;
    private final InventoryTransactionRepository txnRepository;

    @Transactional(readOnly = true)
    public BalanceReport report(LocalDate from, LocalDate to) {
        List<Material> materials = materialRepository.findAll();
        Map<Long, List<InventoryTransaction>> byMaterial = txnRepository.findAllByOrderByTxnDateAsc()
                .stream().collect(Collectors.groupingBy(InventoryTransaction::getMaterialId));

        List<BalanceRow> rows = new ArrayList<>();
        for (Material m : materials) {
            List<InventoryTransaction> txns = byMaterial.getOrDefault(m.getId(), List.of());

            BigDecimal opening = balanceAsOf(txns, from, true, m);   // balance just before 'from'
            BigDecimal closing = balanceAsOf(txns, to, false, m);    // balance at end of 'to'

            BigDecimal receipt = BigDecimal.ZERO, consumption = BigDecimal.ZERO, adjustment = BigDecimal.ZERO;
            for (InventoryTransaction t : txns) {
                LocalDate d = t.getTxnDate().toLocalDate();
                if (d.isBefore(from) || d.isAfter(to)) continue;
                if (t.getTxnType() == TransactionType.RECEIPT) {
                    receipt = receipt.add(t.getQuantity());
                } else if (t.getTxnType() == TransactionType.SALE_CONSUMPTION) {
                    consumption = consumption.add(t.getQuantity().abs());
                } else if (t.getTxnType() == TransactionType.ADJUSTMENT) {
                    adjustment = adjustment.add(t.getQuantity());
                }
            }

            // skip materials with no activity AND no stock to keep the report focused? keep all for completeness
            rows.add(new BalanceRow(m.getId(), m.getMaterialCode(), m.getMaterialName(), m.getUnit(),
                    opening, receipt, consumption, adjustment, closing));
        }
        rows.sort(Comparator.comparing(BalanceRow::materialCode));
        return new BalanceReport(from, to, rows);
    }

    /**
     * Balance of a material at a cut-off date.
     * @param strictlyBefore true = balance just before the date (opening); false = balance through end of the date (closing)
     */
    private BigDecimal balanceAsOf(List<InventoryTransaction> txns, LocalDate date, boolean strictlyBefore, Material m) {
        InventoryTransaction last = null;
        for (InventoryTransaction t : txns) { // txns are ascending by date
            LocalDate d = t.getTxnDate().toLocalDate();
            boolean inScope = strictlyBefore ? d.isBefore(date) : !d.isAfter(date);
            if (inScope) last = t; else break;
        }
        if (last != null) return last.getAfterQty();
        // no transaction within scope
        if (!txns.isEmpty()) return txns.get(0).getBeforeQty(); // balance before first ever movement = initial
        return m.getCurrentQty(); // never moved -> seeded current qty
    }
}
