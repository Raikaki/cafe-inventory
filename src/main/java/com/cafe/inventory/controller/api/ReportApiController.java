package com.cafe.inventory.controller.api;

import com.cafe.inventory.dto.InventoryBalanceDtos.BalanceReport;
import com.cafe.inventory.dto.ProductSalesDtos.ProductSalesReport;
import com.cafe.inventory.service.InventoryBalanceService;
import com.cafe.inventory.service.ProductSalesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Tag(name = "Reports")
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportApiController {

    private final InventoryBalanceService inventoryBalanceService;
    private final ProductSalesService productSalesService;

    @Operation(summary = "Inventory balance per material over a date range (opening/receipt/consumption/adjustment/closing)")
    @GetMapping("/inventory-balance")
    public BalanceReport inventoryBalance(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return inventoryBalanceService.report(from, to);
    }

    @Operation(summary = "Products sold over a date range (quantity + revenue)")
    @GetMapping("/product-sales")
    public ProductSalesReport productSales(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return productSalesService.report(from, to);
    }
}
