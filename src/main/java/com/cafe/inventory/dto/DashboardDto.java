package com.cafe.inventory.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardDto(
        long totalProducts,
        long totalMaterials,
        BigDecimal inventoryValue,
        BigDecimal todaySales,
        BigDecimal todayConsumptionValue,
        long lowStockCount,
        List<MaterialDto> lowStockMaterials,
        List<TopProductView> topProducts
) {
    public record TopProductView(String productCode, String productName, BigDecimal quantity) {}
}
