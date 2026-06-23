package com.cafe.inventory.controller.api;

import com.cafe.inventory.dto.MaterialDto;
import com.cafe.inventory.entity.InventoryTransaction;
import com.cafe.inventory.repository.InventoryTransactionRepository;
import com.cafe.inventory.repository.MaterialRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Inventory")
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryApiController {

    private final InventoryTransactionRepository txnRepository;
    private final MaterialRepository materialRepository;

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
}
