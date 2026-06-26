package com.cafe.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public class VoucherDtos {

    public record VoucherRequest(
            @NotBlank String voucherType,
            @NotNull LocalDate voucherDate,
            String creatorName,
            String creatorAddress,
            String partnerName,
            String partnerAddress,
            String content,
            BigDecimal quantity,
            String unit,
            @NotNull BigDecimal amount,
            String approverName,
            String note,
            String attachmentUrl,
            String debitAccount,
            String creditAccount
    ) {}
}
