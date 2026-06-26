package com.cafe.inventory.controller.api;

import com.cafe.inventory.dto.BatchDtos.BatchRequest;
import com.cafe.inventory.dto.BatchDtos.BatchView;
import com.cafe.inventory.entity.MaterialBatch;
import com.cafe.inventory.service.BatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@Tag(name = "Batches")
@RestController
@RequestMapping("/api/batches")
@RequiredArgsConstructor
public class BatchApiController {

    private final BatchService batchService;

    @Operation(summary = "List batches in FEFO order with expiry status")
    @GetMapping
    public List<BatchView> list() {
        return batchService.list();
    }

    @Operation(summary = "Register a material batch with expiry")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF')")
    public ResponseEntity<MaterialBatch> create(@Valid @RequestBody BatchRequest req, Principal principal) {
        return ResponseEntity.ok(batchService.create(req, principal == null ? "system" : principal.getName()));
    }

    @Operation(summary = "Dispose/use part of a batch (reduces stock via adjustment)")
    @PostMapping("/{id}/dispose")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF')")
    public ResponseEntity<Void> dispose(@PathVariable Long id, @RequestParam BigDecimal qty,
                                        @RequestParam(required = false) String reason, Principal principal) {
        batchService.dispose(id, qty, reason, principal == null ? "system" : principal.getName());
        return ResponseEntity.noContent().build();
    }
}
