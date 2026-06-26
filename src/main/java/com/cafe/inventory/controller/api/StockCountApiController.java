package com.cafe.inventory.controller.api;

import com.cafe.inventory.dto.StockCountDtos.CountRequest;
import com.cafe.inventory.dto.StockCountDtos.CountResponse;
import com.cafe.inventory.service.StockCountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Tag(name = "Stock Count")
@RestController
@RequestMapping("/api/stock-counts")
@RequiredArgsConstructor
public class StockCountApiController {

    private final StockCountService stockCountService;

    @Operation(summary = "Submit a physical stock count; posts adjustments for differences")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF')")
    public ResponseEntity<CountResponse> create(@Valid @RequestBody CountRequest request, Principal principal) {
        return ResponseEntity.ok(stockCountService.create(request, principal == null ? "system" : principal.getName()));
    }
}
