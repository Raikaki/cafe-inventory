package com.cafe.inventory.controller.api;

import com.cafe.inventory.dto.SupplierDto;
import com.cafe.inventory.service.SupplierService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Tag(name = "Suppliers")
@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierApiController {

    private final SupplierService supplierService;

    @GetMapping
    public List<SupplierDto> list() {
        return supplierService.findAll();
    }

    @GetMapping("/{id}")
    public SupplierDto get(@PathVariable Long id) {
        return supplierService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<SupplierDto> create(@Valid @RequestBody SupplierDto dto, Principal principal) {
        return ResponseEntity.ok(supplierService.create(dto, name(principal)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public SupplierDto update(@PathVariable Long id, @Valid @RequestBody SupplierDto dto, Principal principal) {
        return supplierService.update(id, dto, name(principal));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        supplierService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private String name(Principal p) {
        return p == null ? "system" : p.getName();
    }
}
