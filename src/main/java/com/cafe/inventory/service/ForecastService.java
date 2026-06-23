package com.cafe.inventory.service;

import com.cafe.inventory.dto.ForecastDtos.*;
import com.cafe.inventory.entity.InventoryTransaction;
import com.cafe.inventory.entity.Material;
import com.cafe.inventory.repository.InventoryTransactionRepository;
import com.cafe.inventory.repository.MaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Inventory forecasting engine. Uses the SALE_CONSUMPTION ledger to estimate
 * average daily usage per material, projects days-to-stockout and a recommended
 * reorder quantity. This is the predictive core; AI narrative is layered on top.
 */
@Service
@RequiredArgsConstructor
public class ForecastService {

    private final MaterialRepository materialRepository;
    private final InventoryTransactionRepository txnRepository;
    private final AiAdvisorService aiAdvisorService;

    private static final int LEAD_TIME_DAYS = 7; // assumed supplier lead time for reorder suggestion

    @Transactional(readOnly = true)
    public ForecastResponse forecast(int lookbackDays, int horizonDays, boolean withAi) {
        LocalDateTime since = LocalDateTime.now().minusDays(lookbackDays);
        List<InventoryTransaction> consumption = txnRepository.findConsumptionSince(since);

        // total consumed quantity (positive) per material over the lookback window
        Map<Long, BigDecimal> consumedByMaterial = consumption.stream().collect(Collectors.groupingBy(
                InventoryTransaction::getMaterialId,
                Collectors.reducing(BigDecimal.ZERO, t -> t.getQuantity().abs(), BigDecimal::add)));

        List<ForecastItem> items = new ArrayList<>();
        for (Material m : materialRepository.findByActiveFlagTrue()) {
            BigDecimal consumed = consumedByMaterial.getOrDefault(m.getId(), BigDecimal.ZERO);
            BigDecimal avgDaily = consumed.divide(BigDecimal.valueOf(lookbackDays), 3, RoundingMode.HALF_UP);
            BigDecimal projected = avgDaily.multiply(BigDecimal.valueOf(horizonDays));

            Double daysToStockout = null;
            if (avgDaily.compareTo(BigDecimal.ZERO) > 0) {
                daysToStockout = m.getCurrentQty().divide(avgDaily, 1, RoundingMode.HALF_UP).doubleValue();
            }

            // reorder so that we cover lead-time demand + minimum safety stock
            BigDecimal target = avgDaily.multiply(BigDecimal.valueOf(LEAD_TIME_DAYS)).add(m.getMinimumQty());
            BigDecimal reorder = target.subtract(m.getCurrentQty());
            if (reorder.compareTo(BigDecimal.ZERO) < 0) reorder = BigDecimal.ZERO;

            String status = classify(m, avgDaily, daysToStockout);

            items.add(new ForecastItem(
                    m.getId(), m.getMaterialCode(), m.getMaterialName(), m.getUnit(),
                    m.getCurrentQty(), m.getMinimumQty(),
                    avgDaily, daysToStockout, projected,
                    reorder.setScale(3, RoundingMode.HALF_UP), status));
        }

        // sort: most urgent first (smallest days-to-stockout), idle items last
        items.sort(Comparator.comparing(i -> i.daysToStockout() == null ? Double.MAX_VALUE : i.daysToStockout()));

        String advice = withAi ? aiAdvisorService.buildAdvice(items, lookbackDays, horizonDays) : null;
        return new ForecastResponse(horizonDays, lookbackDays, items, advice, aiAdvisorService.isLlmEnabled());
    }

    private String classify(Material m, BigDecimal avgDaily, Double daysToStockout) {
        if (avgDaily.compareTo(BigDecimal.ZERO) == 0) {
            return m.getCurrentQty().compareTo(m.getMinimumQty()) < 0 ? "WARNING" : "IDLE";
        }
        if (daysToStockout != null && daysToStockout <= LEAD_TIME_DAYS) return "CRITICAL";
        if (m.getCurrentQty().compareTo(m.getMinimumQty()) < 0 || (daysToStockout != null && daysToStockout <= 14))
            return "WARNING";
        return "OK";
    }
}
