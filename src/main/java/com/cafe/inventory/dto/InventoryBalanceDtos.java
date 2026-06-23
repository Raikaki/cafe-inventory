package com.cafe.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class InventoryBalanceDtos {

    public record BalanceRow(
            Long materialId,
            String materialCode,
            String materialName,
            String unit,
            BigDecimal openingQty,
            BigDecimal receiptQty,
            BigDecimal consumptionQty,
            BigDecimal adjustmentQty,
            BigDecimal closingQty
    ) {}

    public record BalanceReport(
            LocalDate fromDate,
            LocalDate toDate,
            List<BalanceRow> rows
    ) {}
}
