package com.cafe.inventory.controller.api;

import com.cafe.inventory.dto.PriceComparisonDtos.MaterialComparison;
import com.cafe.inventory.dto.PriceComparisonDtos.QuoteRequest;
import com.cafe.inventory.entity.SupplierPrice;
import com.cafe.inventory.service.PriceComparisonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Tag(name = "Price Comparison")
@RestController
@RequestMapping("/api/price-comparison")
@RequiredArgsConstructor
public class PriceComparisonApiController {

    private final PriceComparisonService service;

    @Operation(summary = "Compare supplier prices per material and recommend the cheapest")
    @GetMapping
    public List<MaterialComparison> compare() {
        return service.compare();
    }

    @Operation(summary = "Add or update a supplier price quote for a material")
    @PostMapping("/quote")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF')")
    public ResponseEntity<SupplierPrice> saveQuote(@Valid @RequestBody QuoteRequest req, Principal principal) {
        return ResponseEntity.ok(service.saveQuote(req, principal == null ? "system" : principal.getName()));
    }

    @DeleteMapping("/quote/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Void> deleteQuote(@PathVariable Long id) {
        service.deleteQuote(id);
        return ResponseEntity.noContent().build();
    }
}
