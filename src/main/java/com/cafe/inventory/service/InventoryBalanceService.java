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
 * Inventory balance report per material over a date range, computed PURELY from
 * the transaction ledger (event-sourced):
 *   opening  = SUM(quantity) for txns before 'from'
 *   receipt  = SUM(+qty)      RECEIPT          within [from, to]
 *   consume  = SUM(|qty|)     SALE_CONSUMPTION within [from, to]
 *   adjust   = SUM(qty)       ADJUSTMENT       within [from, to]
 *   closing  = SUM(quantity) for txns up to 'to'  (= opening + receipt - consume + adjust)
 *
 * This keeps stock fully consistent with "current_qty == SUM(transactions)".
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

            BigDecimal opening = BigDecimal.ZERO, closing = BigDecimal.ZERO;
            BigDecimal receipt = BigDecimal.ZERO, consumption = BigDecimal.ZERO, adjustment = BigDecimal.ZERO;

            for (InventoryTransaction t : txns) {
                LocalDate d = t.getTxnDate().toLocalDate();
                BigDecimal q = t.getQuantity();

                if (d.isBefore(from)) opening = opening.add(q);              // cumulative before period
                if (!d.isAfter(to)) closing = closing.add(q);               // cumulative up to end

                if (!d.isBefore(from) && !d.isAfter(to)) {                   // within [from, to]
                    if (t.getTxnType() == TransactionType.RECEIPT) {
                        receipt = receipt.add(q);
                    } else if (t.getTxnType() == TransactionType.SALE_CONSUMPTION) {
                        consumption = consumption.add(q.abs());
                    } else if (t.getTxnType() == TransactionType.ADJUSTMENT) {
                        adjustment = adjustment.add(q);
                    }
                }
            }

            rows.add(new BalanceRow(m.getId(), m.getMaterialCode(), m.getMaterialName(), m.getUnit(),
                    opening, receipt, consumption, adjustment, closing));
        }
        rows.sort(Comparator.comparing(BalanceRow::materialCode));
        return new BalanceReport(from, to, rows);
    }
}
