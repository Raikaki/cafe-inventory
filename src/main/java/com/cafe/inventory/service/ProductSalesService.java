package com.cafe.inventory.service;

import com.cafe.inventory.dto.ProductSalesDtos.*;
import com.cafe.inventory.entity.Product;
import com.cafe.inventory.repository.ProductRepository;
import com.cafe.inventory.repository.SaleRepository;
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
public class ProductSalesService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public ProductSalesReport report(LocalDate from, LocalDate to) {
        Map<Long, Product> products = productRepository.findAll().stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        BigDecimal totalQty = BigDecimal.ZERO;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        List<ProductSalesRow> rows = new java.util.ArrayList<>();

        for (SaleRepository.TopProduct tp : saleRepository.sumByProductBetween(from, to)) {
            Product p = products.get(tp.getProductId());
            BigDecimal qty = tp.getQty() == null ? BigDecimal.ZERO : tp.getQty();
            BigDecimal price = p == null ? BigDecimal.ZERO : p.getSalePrice();
            BigDecimal revenue = qty.multiply(price);
            totalQty = totalQty.add(qty);
            totalRevenue = totalRevenue.add(revenue);
            rows.add(new ProductSalesRow(
                    p == null ? "?" : p.getProductCode(),
                    p == null ? "(đã xoá)" : p.getProductName(),
                    price, qty, revenue));
        }
        return new ProductSalesReport(from, to, totalQty, totalRevenue, rows);
    }
}
