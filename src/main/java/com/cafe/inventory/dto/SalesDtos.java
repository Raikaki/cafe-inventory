package com.cafe.inventory.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class SalesDtos {

    public record SaleLine(
            @NotNull String productCode,
            @NotNull BigDecimal quantity
    ) {}

    public record SalesRequest(
            @NotNull LocalDate saleDate,
            @NotNull List<SaleLine> lines
    ) {}

    public record ConsumedMaterial(
            Long materialId,
            String materialCode,
            String materialName,
            String unit,
            BigDecimal consumedQty,
            BigDecimal beforeQty,
            BigDecimal afterQty
    ) {}

    public record SalesResult(
            String batchNo,
            LocalDate saleDate,
            int salesLines,
            List<ConsumedMaterial> consumption,
            List<String> warnings
    ) {}
}
