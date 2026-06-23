package com.cafe.inventory.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class GoodsReceiptDtos {

    public record ReceiptLine(
            @NotNull Long materialId,
            @NotNull BigDecimal quantity,
            @NotNull BigDecimal unitPrice
    ) {}

    public record ReceiptRequest(
            @NotNull LocalDate receiptDate,
            Long supplierId,
            String note,
            @NotNull List<ReceiptLine> lines
    ) {}

    public record ReceiptResponse(
            Long receiptId,
            String receiptNo,
            LocalDate receiptDate,
            BigDecimal totalAmount
    ) {}
}
