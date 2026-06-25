package com.cafe.inventory.service;

import com.cafe.inventory.dto.GoodsReceiptDtos.ReceiptLine;
import com.cafe.inventory.dto.GoodsReceiptDtos.ReceiptRequest;
import com.cafe.inventory.dto.GoodsReceiptDtos.ReceiptResponse;
import com.cafe.inventory.entity.GoodsReceipt;
import com.cafe.inventory.repository.GoodsReceiptRepository;
import com.cafe.inventory.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GoodsReceiptServiceTest {

    @Mock GoodsReceiptRepository goodsReceiptRepository;
    @Mock InventoryService inventoryService;
    @Mock VoucherService voucherService;
    @Mock SupplierRepository supplierRepository;
    @InjectMocks GoodsReceiptService service;

    @BeforeEach
    void setUp() {
        when(goodsReceiptRepository.countByReceiptNoStartingWith(anyString())).thenReturn(0L);
        when(goodsReceiptRepository.save(any(GoodsReceipt.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void create_derivesUnitPriceFromAmount_andSumsTotal() {
        // qty 10, amount 1000 -> unit price 100
        ReceiptRequest req = new ReceiptRequest(LocalDate.now(), null, "test",
                List.of(new ReceiptLine(1L, new BigDecimal("10"), new BigDecimal("1000"))));

        ReceiptResponse res = service.create(req, "user");

        ArgumentCaptor<GoodsReceipt> cap = ArgumentCaptor.forClass(GoodsReceipt.class);
        verify(goodsReceiptRepository).save(cap.capture());
        GoodsReceipt gr = cap.getValue();

        assertThat(gr.getDetails()).hasSize(1);
        assertThat(gr.getDetails().get(0).getUnitPrice()).isEqualByComparingTo("100");
        assertThat(gr.getDetails().get(0).getAmount()).isEqualByComparingTo("1000");
        assertThat(res.totalAmount()).isEqualByComparingTo("1000");
        assertThat(res.receiptNo()).startsWith("GR");

        // inventory increased for the material; cost computed at close, not here
        verify(inventoryService).receive(eq(1L), any(), any(), anyString(), eq("user"));
    }
}
