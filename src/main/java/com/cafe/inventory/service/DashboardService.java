package com.cafe.inventory.service;

import com.cafe.inventory.dto.DashboardDto;
import com.cafe.inventory.dto.DashboardDto.TopProductView;
import com.cafe.inventory.dto.MaterialDto;
import com.cafe.inventory.entity.Material;
import com.cafe.inventory.entity.Product;
import com.cafe.inventory.entity.Sale;
import com.cafe.inventory.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProductRepository productRepository;
    private final MaterialRepository materialRepository;
    private final SaleRepository saleRepository;

    @Transactional(readOnly = true)
    public DashboardDto build() {
        List<Material> materials = materialRepository.findAll();

        long totalProducts = productRepository.findByActiveFlagTrue().size();
        long totalMaterials = materials.stream().filter(m -> Boolean.TRUE.equals(m.getActiveFlag())).count();

        BigDecimal inventoryValue = materials.stream()
                .map(m -> m.getCurrentQty().multiply(m.getAverageCost()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDate today = LocalDate.now();
        List<Sale> todaySalesList = saleRepository.findBySaleDate(today);

        Map<Long, Product> productMap = productRepository.findAll().stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        BigDecimal todaySalesAmount = todaySalesList.stream()
                .map(s -> {
                    Product p = productMap.get(s.getProductId());
                    return p == null ? BigDecimal.ZERO : p.getSalePrice().multiply(s.getQuantity());
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Material> lowStock = materialRepository.findLowStock();

        List<TopProductView> topProducts = saleRepository.findTopProducts().stream()
                .limit(5)
                .map(tp -> {
                    Product p = productMap.get(tp.getProductId());
                    return new TopProductView(
                            p == null ? "?" : p.getProductCode(),
                            p == null ? "?" : p.getProductName(),
                            tp.getQty());
                })
                .toList();

        return new DashboardDto(
                totalProducts,
                totalMaterials,
                inventoryValue,
                todaySalesAmount,
                BigDecimal.ZERO,
                lowStock.size(),
                lowStock.stream().map(MaterialDto::from).toList(),
                topProducts
        );
    }
}
