package com.cafe.inventory.controller.api;

import com.cafe.inventory.dto.ProductDto;
import com.cafe.inventory.service.ProductService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Tag(name = "Products")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductApiController {

    private final ProductService productService;

    @GetMapping
    public List<ProductDto> list() {
        return productService.findAll();
    }

    @GetMapping("/{id}")
    public ProductDto get(@PathVariable Long id) {
        return productService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ProductDto> create(@Valid @RequestBody ProductDto dto, Principal principal) {
        return ResponseEntity.ok(productService.create(dto, name(principal)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ProductDto update(@PathVariable Long id, @Valid @RequestBody ProductDto dto, Principal principal) {
        return productService.update(id, dto, name(principal));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private String name(Principal p) {
        return p == null ? "system" : p.getName();
    }
}
