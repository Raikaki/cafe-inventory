package com.cafe.inventory.service;

import com.cafe.inventory.entity.InventoryTransaction;
import com.cafe.inventory.entity.Material;
import com.cafe.inventory.entity.StockPeriodSummary;
import com.cafe.inventory.entity.TransactionType;
import com.cafe.inventory.repository.InventoryTransactionRepository;
import com.cafe.inventory.repository.MaterialRepository;
import com.cafe.inventory.repository.StockPeriodSummaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Period-close: aggregates the transaction ledger into the materialized
 * stock_period_summary table for a given month (qty + value). Reports then read
 * directly from the stored table.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockSummaryService {

    private final MaterialRepository materialRepository;
    private final InventoryTransactionRepository txnRepository;
    private final StockPeriodSummaryRepository summaryRepository;

    @Transactional(readOnly = true)
    public List<StockPeriodSummary> get(int year, int month) {
        return summaryRepository.findByPeriodYearAndPeriodMonthOrderByMaterialCode(year, month);
    }

    /**
     * Close the month: recompute from the ledger, derive the PERIODIC WEIGHTED
     * AVERAGE cost, update each material's average cost, and store the summary.
     *
     *   avgCost = (openingValue + receiptAmountInMonth) / (openingQty + receiptQtyInMonth)
     *   openingValue = previous month's closing value (or openingQty × current avg if none)
     */
    @Transactional
    public List<StockPeriodSummary> aggregate(int year, int month, String user) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate from = ym.atDay(1);
        LocalDate to = ym.atEndOfMonth();

        YearMonth prevYm = ym.minusMonths(1);
        Map<Long, StockPeriodSummary> prev = summaryRepository
                .findByPeriodYearAndPeriodMonthOrderByMaterialCode(prevYm.getYear(), prevYm.getMonthValue())
                .stream().collect(Collectors.toMap(StockPeriodSummary::getMaterialId, s -> s, (a, b) -> a));

        Map<Long, List<InventoryTransaction>> byMaterial = txnRepository.findAllByOrderByTxnDateAsc()
                .stream().collect(Collectors.groupingBy(InventoryTransaction::getMaterialId));

        summaryRepository.deleteByPeriodYearAndPeriodMonth(year, month);

        List<StockPeriodSummary> rows = new ArrayList<>();
        for (Material m : materialRepository.findAll()) {
            List<InventoryTransaction> txns = byMaterial.getOrDefault(m.getId(), List.of());

            BigDecimal opening = BigDecimal.ZERO, closing = BigDecimal.ZERO;
            BigDecimal receipt = BigDecimal.ZERO, consumption = BigDecimal.ZERO, adjustment = BigDecimal.ZERO;
            BigDecimal receiptAmount = BigDecimal.ZERO; // tổng tiền nhập trong tháng

            for (InventoryTransaction t : txns) {
                LocalDate d = t.getTxnDate().toLocalDate();
                BigDecimal q = t.getQuantity();
                if (d.isBefore(from)) opening = opening.add(q);
                if (!d.isAfter(to)) closing = closing.add(q);
                if (!d.isBefore(from) && !d.isAfter(to)) {
                    switch (t.getTxnType()) {
                        case RECEIPT -> {
                            receipt = receipt.add(q);
                            BigDecimal uc = t.getUnitCost() == null ? BigDecimal.ZERO : t.getUnitCost();
                            receiptAmount = receiptAmount.add(q.multiply(uc));
                        }
                        case SALE_CONSUMPTION -> consumption = consumption.add(q.abs());
                        case ADJUSTMENT -> adjustment = adjustment.add(q);
                        default -> { }
                    }
                }
            }

            // opening value: prefer previous month's closing value
            BigDecimal prevAvg = m.getAverageCost() == null ? BigDecimal.ZERO : m.getAverageCost();
            StockPeriodSummary p = prev.get(m.getId());
            BigDecimal openingValue = (p != null) ? p.getClosingValue() : opening.multiply(prevAvg);

            // periodic weighted average cost
            BigDecimal denom = opening.add(receipt);
            BigDecimal avgCost = denom.compareTo(BigDecimal.ZERO) > 0
                    ? openingValue.add(receiptAmount).divide(denom, 2, RoundingMode.HALF_UP)
                    : prevAvg;

            BigDecimal closingValue = closing.multiply(avgCost).setScale(2, RoundingMode.HALF_UP);

            // update the material's average cost at close
            m.setAverageCost(avgCost);
            materialRepository.save(m);

            StockPeriodSummary s = new StockPeriodSummary();
            s.setPeriodYear(year);
            s.setPeriodMonth(month);
            s.setMaterialId(m.getId());
            s.setMaterialCode(m.getMaterialCode());
            s.setMaterialName(m.getMaterialName());
            s.setUnit(m.getUnit());
            s.setOpeningQty(opening);
            s.setReceiptQty(receipt);
            s.setConsumptionQty(consumption);
            s.setAdjustmentQty(adjustment);
            s.setClosingQty(closing);
            s.setUnitCost(avgCost);
            s.setClosingValue(closingValue);
            s.setCreatedBy(user);
            rows.add(s);
        }
        rows.sort(Comparator.comparing(StockPeriodSummary::getMaterialCode,
                Comparator.nullsLast(Comparator.naturalOrder())));
        List<StockPeriodSummary> saved = summaryRepository.saveAll(rows);
        log.info("Stock summary aggregated for {}-{} by {}: {} rows", year, month, user, saved.size());
        return saved;
    }
}
