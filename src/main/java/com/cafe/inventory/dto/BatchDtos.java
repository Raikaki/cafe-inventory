package com.cafe.inventory.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BatchDtos {

    public record BatchRequest(
            @NotNull Long materialId,
            String batchNo,
            Long supplierId,
            LocalDate receivedDate,
            LocalDate expiryDate,
            @NotNull BigDecimal receivedQty,
            String note
    ) {}

    public record BatchView(
            Long id,
            Long materialId,
            String materialCode,
            String materialName,
            String unit,
            String batchNo,
            String supplierName,
            LocalDate receivedDate,
            LocalDate expiryDate,
            BigDecimal receivedQty,
            BigDecimal remainingQty,
            Long daysToExpiry,   // null if no expiry; negative = expired
            String status        // EXPIRED | EXPIRING | OK | NO_EXPIRY
    ) {}
}
