package com.cafe.inventory.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class StockCountDtos {

    public record CountItem(
            @NotNull Long materialId,
            @NotNull BigDecimal actualQty
    ) {}

    public record CountRequest(
            @NotNull LocalDate countDate,
            String note,
            @NotNull List<CountItem> items
    ) {}

    public record CountResultRow(
            String materialCode,
            String materialName,
            String unit,
            BigDecimal systemQty,
            BigDecimal actualQty,
            BigDecimal diffQty
    ) {}

    public record CountResponse(
            String countNo,
            LocalDate countDate,
            int adjustedCount,
            List<CountResultRow> rows
    ) {}
}
