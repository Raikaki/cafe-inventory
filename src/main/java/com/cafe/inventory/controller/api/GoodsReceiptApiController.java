package com.cafe.inventory.controller.api;

import com.cafe.inventory.dto.GoodsReceiptDtos.*;
import com.cafe.inventory.service.GoodsReceiptService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Tag(name = "Goods Receipt")
@RestController
@RequestMapping("/api/goods-receipts")
@RequiredArgsConstructor
public class GoodsReceiptApiController {

    private final GoodsReceiptService goodsReceiptService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF')")
    public ResponseEntity<ReceiptResponse> create(@Valid @RequestBody ReceiptRequest request, Principal principal) {
        return ResponseEntity.ok(goodsReceiptService.create(request, principal == null ? "system" : principal.getName()));
    }
}
