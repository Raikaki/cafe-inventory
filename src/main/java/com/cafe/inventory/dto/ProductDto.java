package com.cafe.inventory.dto;

import com.cafe.inventory.entity.Product;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProductDto(
        Long id,
        @NotBlank String productCode,
        @NotBlank String productName,
        @NotNull BigDecimal salePrice,
        Boolean activeFlag
) {
    public static ProductDto from(Product p) {
        return new ProductDto(p.getId(), p.getProductCode(), p.getProductName(),
                p.getSalePrice(), p.getActiveFlag());
    }
}
