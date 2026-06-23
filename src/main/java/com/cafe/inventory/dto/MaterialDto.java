package com.cafe.inventory.dto;

import com.cafe.inventory.entity.Material;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record MaterialDto(
        Long id,
        @NotBlank String materialCode,
        @NotBlank String materialName,
        @NotBlank String unit,
        @NotNull BigDecimal currentQty,
        @NotNull BigDecimal minimumQty,
        @NotNull BigDecimal maximumQty,
        @NotNull BigDecimal averageCost,
        Boolean activeFlag
) {
    public static MaterialDto from(Material m) {
        return new MaterialDto(
                m.getId(), m.getMaterialCode(), m.getMaterialName(), m.getUnit(),
                m.getCurrentQty(), m.getMinimumQty(), m.getMaximumQty(),
                m.getAverageCost(), m.getActiveFlag()
        );
    }
}
