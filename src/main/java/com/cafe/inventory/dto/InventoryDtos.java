package com.cafe.inventory.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class InventoryDtos {

    /** Signed quantity: positive = increase, negative = decrease. */
    public record AdjustRequest(
            @NotNull Long materialId,
            @NotNull BigDecimal quantity,
            String reason
    ) {}
}
