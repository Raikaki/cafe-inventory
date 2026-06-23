package com.cafe.inventory.service;

import com.cafe.inventory.dto.RecipeDtos.*;
import com.cafe.inventory.entity.*;
import com.cafe.inventory.exception.ResourceNotFoundException;
import com.cafe.inventory.repository.MaterialRepository;
import com.cafe.inventory.repository.ProductRepository;
import com.cafe.inventory.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final ProductRepository productRepository;
    private final MaterialRepository materialRepository;

    @Transactional(readOnly = true)
    public RecipeView getByProductId(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
        Recipe recipe = recipeRepository.findFirstByProductIdAndActiveFlagTrue(productId).orElse(null);

        Map<Long, Material> materials = materialRepository.findAll().stream()
                .collect(Collectors.toMap(Material::getId, m -> m));

        List<RecipeLine> lines = new ArrayList<>();
        BigDecimal totalCost = BigDecimal.ZERO;
        if (recipe != null) {
            for (RecipeDetail d : recipe.getDetails()) {
                Material m = materials.get(d.getMaterialId());
                BigDecimal cost = (m == null ? BigDecimal.ZERO : m.getAverageCost()).multiply(d.getStandardQty());
                totalCost = totalCost.add(cost);
                lines.add(new RecipeLine(
                        d.getMaterialId(),
                        m == null ? null : m.getMaterialCode(),
                        m == null ? null : m.getMaterialName(),
                        m == null ? null : m.getUnit(),
                        d.getStandardQty()
                ));
            }
        }
        return new RecipeView(
                recipe == null ? null : recipe.getId(),
                product.getId(),
                product.getProductCode(),
                product.getProductName(),
                recipe == null ? null : recipe.getVersionNo(),
                recipe != null,
                totalCost,
                lines
        );
    }

    @Transactional
    public RecipeView save(RecipeSaveRequest request, String user) {
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + request.productId()));

        Recipe recipe = recipeRepository.findFirstByProductIdAndActiveFlagTrue(product.getId())
                .orElseGet(() -> {
                    Recipe r = new Recipe();
                    r.setProductId(product.getId());
                    r.setVersionNo(1);
                    r.setActiveFlag(true);
                    r.setCreatedBy(user);
                    return r;
                });
        recipe.setUpdatedBy(user);
        recipe.getDetails().clear();

        if (request.lines() != null) {
            for (RecipeLine line : request.lines()) {
                if (line.materialId() == null || line.standardQty() == null) continue;
                RecipeDetail d = new RecipeDetail();
                d.setMaterialId(line.materialId());
                d.setStandardQty(line.standardQty());
                recipe.addDetail(d);
            }
        }
        recipeRepository.save(recipe);
        return getByProductId(product.getId());
    }
}
