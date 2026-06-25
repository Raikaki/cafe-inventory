package com.cafe.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ProductSalesDtos {

    public record ProductSalesRow(
            String productCode,
            String productName,
            BigDecimal salePrice,
            BigDecimal quantitySold,
            BigDecimal revenue
    ) {}

    public record ProductSalesReport(
            LocalDate fromDate,
            LocalDate toDate,
            BigDecimal totalQuantity,
            BigDecimal totalRevenue,
            List<ProductSalesRow> rows
    ) {}
}
