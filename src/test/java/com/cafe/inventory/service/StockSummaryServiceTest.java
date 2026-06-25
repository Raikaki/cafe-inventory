package com.cafe.inventory.service;

import com.cafe.inventory.entity.InventoryTransaction;
import com.cafe.inventory.entity.Material;
import com.cafe.inventory.entity.StockPeriodSummary;
import com.cafe.inventory.entity.TransactionType;
import com.cafe.inventory.repository.InventoryTransactionRepository;
import com.cafe.inventory.repository.MaterialRepository;
import com.cafe.inventory.repository.StockPeriodSummaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StockSummaryServiceTest {

    @Mock MaterialRepository materialRepository;
    @Mock InventoryTransactionRepository txnRepository;
    @Mock StockPeriodSummaryRepository summaryRepository;
    @InjectMocks StockSummaryService service;

    Material material;

    @BeforeEach
    void setUp() {
        material = new Material();
        material.setId(1L);
        material.setMaterialCode("MAT001");
        material.setMaterialName("Coffee Powder");
        material.setUnit("g");
        material.setAverageCost(new BigDecimal("10")); // previous average

        when(materialRepository.findAll()).thenReturn(List.of(material));
        when(materialRepository.save(any(Material.class))).thenAnswer(i -> i.getArgument(0));
        when(summaryRepository.findByPeriodYearAndPeriodMonthOrderByMaterialCode(anyInt(), anyInt()))
                .thenReturn(List.of()); // no previous month
        when(summaryRepository.saveAll(any())).thenAnswer(i -> i.getArgument(0));
    }

    private InventoryTransaction txn(TransactionType type, LocalDateTime when, String qty, String unitCost) {
        InventoryTransaction t = new InventoryTransaction();
        t.setMaterialId(1L);
        t.setTxnType(type);
        t.setTxnDate(when);
        t.setQuantity(new BigDecimal(qty));
        t.setUnitCost(new BigDecimal(unitCost));
        return t;
    }

    @Test
    void aggregate_computesPeriodicWeightedAverage() {
        // opening 100 (dated before the month) + receipt 50 @ 20 in June
        InventoryTransaction opening = txn(TransactionType.ADJUSTMENT, LocalDateTime.of(2000, 1, 1, 0, 0), "100", "10");
        InventoryTransaction receipt = txn(TransactionType.RECEIPT, LocalDateTime.of(2026, 6, 10, 8, 0), "50", "20");
        when(txnRepository.findAllByOrderByTxnDateAsc()).thenReturn(List.of(opening, receipt));

        List<StockPeriodSummary> rows = service.aggregate(2026, 6, "user");

        assertThat(rows).hasSize(1);
        StockPeriodSummary s = rows.get(0);
        assertThat(s.getOpeningQty()).isEqualByComparingTo("100");
        assertThat(s.getReceiptQty()).isEqualByComparingTo("50");
        assertThat(s.getClosingQty()).isEqualByComparingTo("150");

        // avg = (openingValue 100*10 + receiptAmount 50*20) / (100+50) = 2000/150 = 13.33
        assertThat(s.getUnitCost()).isEqualByComparingTo("13.33");
        assertThat(s.getClosingValue()).isEqualByComparingTo("1999.50");

        // material average cost is updated at close
        assertThat(material.getAverageCost()).isEqualByComparingTo("13.33");
    }
}
