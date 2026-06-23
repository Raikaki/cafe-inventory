package com.cafe.inventory.service;

import com.cafe.inventory.entity.InventoryTransaction;
import com.cafe.inventory.entity.Material;
import com.cafe.inventory.entity.TransactionType;
import com.cafe.inventory.repository.InventoryTransactionRepository;
import com.cafe.inventory.repository.MaterialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock MaterialRepository materialRepository;
    @Mock InventoryTransactionRepository txnRepository;
    @InjectMocks InventoryService inventoryService;

    Material material;

    @BeforeEach
    void setUp() {
        material = new Material();
        material.setId(1L);
        material.setMaterialCode("MAT001");
        material.setCurrentQty(new BigDecimal("100.000"));
        material.setAverageCost(new BigDecimal("10.00"));
        when(materialRepository.findById(1L)).thenReturn(Optional.of(material));
        when(materialRepository.save(any(Material.class))).thenAnswer(i -> i.getArgument(0));
        when(txnRepository.save(any(InventoryTransaction.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void receive_increasesQtyAndRecomputesWeightedAverageCost() {
        // 100 @ 10 + 100 @ 20  => 200 @ 15
        InventoryTransaction txn = inventoryService.receive(
                1L, new BigDecimal("100"), new BigDecimal("20"), "GR1", "tester");

        assertThat(material.getCurrentQty()).isEqualByComparingTo("200.000");
        assertThat(material.getAverageCost()).isEqualByComparingTo("15.00");
        assertThat(txn.getTxnType()).isEqualTo(TransactionType.RECEIPT);
        assertThat(txn.getBeforeQty()).isEqualByComparingTo("100");
        assertThat(txn.getAfterQty()).isEqualByComparingTo("200");
    }

    @Test
    void consume_decreasesQtyAndRecordsNegativeMovement() {
        InventoryTransaction txn = inventoryService.consume(
                1L, new BigDecimal("30"), "SL1", "tester", "Sales");

        assertThat(material.getCurrentQty()).isEqualByComparingTo("70.000");
        assertThat(txn.getTxnType()).isEqualTo(TransactionType.SALE_CONSUMPTION);
        assertThat(txn.getQuantity()).isEqualByComparingTo("-30");
        assertThat(txn.getAfterQty()).isEqualByComparingTo("70");
    }
}
