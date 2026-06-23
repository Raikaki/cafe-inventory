package com.cafe.inventory.controller.api;

import com.cafe.inventory.dto.InventoryDtos.AdjustRequest;
import com.cafe.inventory.dto.MaterialDto;
import com.cafe.inventory.entity.InventoryTransaction;
import com.cafe.inventory.repository.InventoryTransactionRepository;
import com.cafe.inventory.repository.MaterialRepository;
import com.cafe.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Tag(name = "Inventory")
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryApiController {

    private final InventoryTransactionRepository txnRepository;
    private final MaterialRepository materialRepository;
    private final InventoryService inventoryService;

    @GetMapping("/transactions")
    public List<InventoryTransaction> recent() {
        return txnRepository.findTop50ByOrderByTxnDateDesc();
    }

    @GetMapping("/transactions/material/{materialId}")
    public List<InventoryTransaction> byMaterial(@PathVariable Long materialId) {
        return txnRepository.findByMaterialIdOrderByTxnDateDesc(materialId);
    }

    @GetMapping("/low-stock")
    public List<MaterialDto> lowStock() {
        return materialRepository.findLowStock().stream().map(MaterialDto::from).toList();
    }

    @Operation(summary = "Adjust stock via a transaction (positive = increase, negative = decrease)")
    @PostMapping("/adjust")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<InventoryTransaction> adjust(@Valid @RequestBody AdjustRequest req, Principal principal) {
        String user = principal == null ? "system" : principal.getName();
        String reason = req.reason() == null || req.reason().isBlank() ? "Điều chỉnh tồn" : req.reason();
        return ResponseEntity.ok(
                inventoryService.adjust(req.materialId(), req.quantity(), "ADJ", user, reason));
    }
}
