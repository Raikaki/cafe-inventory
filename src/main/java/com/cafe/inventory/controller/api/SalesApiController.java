package com.cafe.inventory.controller.api;

import com.cafe.inventory.dto.SalesDtos.*;
import com.cafe.inventory.service.SalesImportService;
import com.cafe.inventory.service.SalesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@Tag(name = "Sales")
@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SalesApiController {

    private final SalesService salesService;
    private final SalesImportService salesImportService;

    @Operation(summary = "Record sales manually and auto-deduct inventory")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF')")
    public ResponseEntity<SalesResult> record(@Valid @RequestBody SalesRequest request, Principal principal) {
        return ResponseEntity.ok(salesService.recordSales(request, name(principal)));
    }

    @Operation(summary = "Import sales from Excel (.xlsx) or CSV and auto-deduct inventory")
    @PostMapping(value = "/import", consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF')")
    public ResponseEntity<SalesResult> importFile(@RequestParam("file") MultipartFile file, Principal principal) {
        return ResponseEntity.ok(salesImportService.importFile(file, name(principal)));
    }

    private String name(Principal p) {
        return p == null ? "system" : p.getName();
    }
}
