package com.cafe.inventory.service;

import com.cafe.inventory.dto.InventoryBalanceDtos.BalanceReport;
import com.cafe.inventory.dto.InventoryBalanceDtos.BalanceRow;
import com.cafe.inventory.entity.InventoryTransaction;
import com.cafe.inventory.entity.Material;
import com.cafe.inventory.entity.TransactionType;
import com.cafe.inventory.repository.InventoryTransactionRepository;
import com.cafe.inventory.repository.MaterialRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InventoryBalanceServiceTest {

    @Mock MaterialRepository materialRepository;
    @Mock InventoryTransactionRepository txnRepository;
    @InjectMocks InventoryBalanceService service;

    private InventoryTransaction txn(TransactionType type, LocalDateTime when, String qty) {
        InventoryTransaction t = new InventoryTransaction();
        t.setMaterialId(1L);
        t.setTxnType(type);
        t.setTxnDate(when);
        t.setQuantity(new BigDecimal(qty));
        return t;
    }

    @Test
    void report_carriesOpeningAndComputesClosingFromLedger() {
        Material m = new Material();
        m.setId(1L);
        m.setMaterialCode("MAT001");
        m.setMaterialName("Coffee Powder");
        m.setUnit("g");
        when(materialRepository.findAll()).thenReturn(List.of(m));

        // opening 500 (before June) + receipt 200 in June, no sales
        when(txnRepository.findAllByOrderByTxnDateAsc()).thenReturn(List.of(
                txn(TransactionType.ADJUSTMENT, LocalDateTime.of(2000, 1, 1, 0, 0), "500"),
                txn(TransactionType.RECEIPT, LocalDateTime.of(2026, 6, 10, 8, 0), "200")
        ));

        BalanceReport report = service.report(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30));

        assertThat(report.rows()).hasSize(1);
        BalanceRow r = report.rows().get(0);
        assertThat(r.openingQty()).isEqualByComparingTo("500");
        assertThat(r.receiptQty()).isEqualByComparingTo("200");
        assertThat(r.consumptionQty()).isEqualByComparingTo("0");
        assertThat(r.closingQty()).isEqualByComparingTo("700"); // 500 + 200
    }
}
