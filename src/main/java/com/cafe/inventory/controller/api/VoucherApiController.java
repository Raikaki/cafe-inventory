package com.cafe.inventory.controller.api;

import com.cafe.inventory.dto.VoucherDtos.VoucherRequest;
import com.cafe.inventory.entity.Voucher;
import com.cafe.inventory.service.VoucherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@Tag(name = "Vouchers")
@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
public class VoucherApiController {

    private final VoucherService voucherService;

    @Operation(summary = "Query accounting vouchers by date range and optional type")
    @GetMapping
    public List<Voucher> query(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String type) {
        return voucherService.query(from, to, type);
    }

    @GetMapping("/{id}")
    public Voucher get(@PathVariable Long id) {
        return voucherService.get(id);
    }

    @Operation(summary = "Create an accounting voucher (auto number + amount in words)")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF')")
    public ResponseEntity<Voucher> create(@Valid @RequestBody VoucherRequest req, Principal principal) {
        return ResponseEntity.ok(voucherService.create(req, principal == null ? "system" : principal.getName()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        voucherService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
