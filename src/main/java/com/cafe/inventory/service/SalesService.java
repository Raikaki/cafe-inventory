package com.cafe.inventory.service;

import com.cafe.inventory.dto.SalesDtos.*;
import com.cafe.inventory.entity.*;
import com.cafe.inventory.exception.BusinessException;
import com.cafe.inventory.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Year;
import java.util.*;

/**
 * Records sales and performs AUTOMATIC MATERIAL CONSUMPTION:
 * for every product sold, the active recipe is exploded and each material's
 * stock is decreased by standardQty * quantitySold.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SalesService {

    private final ProductRepository productRepository;
    private final RecipeRepository recipeRepository;
    private final MaterialRepository materialRepository;
    private final SaleRepository saleRepository;
    private final InventoryService inventoryService;

    @Transactional
    public SalesResult recordSales(SalesRequest request, String user) {
        if (request.lines() == null || request.lines().isEmpty()) {
            throw new BusinessException("Sales must contain at least one line");
        }

        String batchNo = nextBatchNo();
        List<String> warnings = new ArrayList<>();
        // aggregate required material qty across all sold products
        Map<Long, BigDecimal> required = new LinkedHashMap<>();

        for (SaleLine line : request.lines()) {
            Product product = productRepository.findByProductCode(line.productCode())
                    .orElseThrow(() -> new BusinessException("Product code not found: " + line.productCode()));

            Sale sale = new Sale();
            sale.setSaleDate(request.saleDate());
            sale.setProductId(product.getId());
            sale.setQuantity(line.quantity());
            sale.setBatchNo(batchNo);
            sale.setCreatedBy(user);
            saleRepository.save(sale);

            Recipe recipe = recipeRepository.findFirstByProductIdAndActiveFlagTrue(product.getId()).orElse(null);
            if (recipe == null || recipe.getDetails().isEmpty()) {
                warnings.add("No active recipe for product " + product.getProductCode() + " - skipped consumption");
                continue;
            }
            for (RecipeDetail d : recipe.getDetails()) {
                BigDecimal need = d.getStandardQty().multiply(line.quantity());
                required.merge(d.getMaterialId(), need, BigDecimal::add);
            }
        }

        // apply consumption
        List<ConsumedMaterial> consumption = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> e : required.entrySet()) {
            Material m = materialRepository.findById(e.getKey()).orElse(null);
            if (m == null) continue;
            BigDecimal before = m.getCurrentQty();
            inventoryService.consume(m.getId(), e.getValue(), batchNo, user, "Sales consumption");
            BigDecimal after = before.subtract(e.getValue());
            if (after.compareTo(BigDecimal.ZERO) < 0) {
                warnings.add("Material " + m.getMaterialCode() + " went below zero (" + after + ")");
            }
            consumption.add(new ConsumedMaterial(
                    m.getId(), m.getMaterialCode(), m.getMaterialName(), m.getUnit(),
                    e.getValue(), before, after));
        }

        log.info("Sales batch {} recorded by {}: {} lines, {} materials consumed",
                batchNo, user, request.lines().size(), consumption.size());
        return new SalesResult(batchNo, request.saleDate(), request.lines().size(), consumption, warnings);
    }

    private String nextBatchNo() {
        String prefix = "SL" + Year.now().getValue();
        long seq = saleRepository.count() + 1;
        return String.format("%s%06d", prefix, seq);
    }
}
