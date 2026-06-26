package com.cafe.inventory.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public class PriceComparisonDtos {

    public record SupplierPriceOption(
            Long quoteId,   // non-null only for manual QUOTE rows (for editing/deleting)
            Long supplierId,
            String supplierName,
            BigDecimal price,
            String source   // QUOTE (báo giá) | HISTORY (lịch sử nhập)
    ) {}

    public record MaterialComparison(
            Long materialId,
            String materialCode,
            String materialName,
            String unit,
            BigDecimal currentQty,
            List<SupplierPriceOption> options,
            Long cheapestSupplierId,
            String cheapestSupplierName,
            BigDecimal cheapestPrice
    ) {}

    public record QuoteRequest(
            @NotNull Long materialId,
            @NotNull Long supplierId,
            @NotNull BigDecimal price,
            String note
    ) {}
}
