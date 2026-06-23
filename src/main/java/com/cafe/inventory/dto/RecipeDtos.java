package com.cafe.inventory.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public class RecipeDtos {

    public record RecipeLine(
            @NotNull Long materialId,
            String materialCode,
            String materialName,
            String unit,
            @NotNull BigDecimal standardQty
    ) {}

    public record RecipeView(
            Long recipeId,
            Long productId,
            String productCode,
            String productName,
            Integer versionNo,
            Boolean activeFlag,
            BigDecimal totalCost,
            List<RecipeLine> lines
    ) {}

    public record RecipeSaveRequest(
            @NotNull Long productId,
            List<RecipeLine> lines
    ) {}
}
