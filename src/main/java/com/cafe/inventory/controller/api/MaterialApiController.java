package com.cafe.inventory.controller.api;

import com.cafe.inventory.dto.MaterialDto;
import com.cafe.inventory.service.MaterialService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Tag(name = "Materials")
@RestController
@RequestMapping("/api/materials")
@RequiredArgsConstructor
public class MaterialApiController {

    private final MaterialService materialService;

    @GetMapping
    public List<MaterialDto> list() {
        return materialService.findAll();
    }

    @GetMapping("/{id}")
    public MaterialDto get(@PathVariable Long id) {
        return materialService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF')")
    public ResponseEntity<MaterialDto> create(@Valid @RequestBody MaterialDto dto, Principal principal) {
        return ResponseEntity.ok(materialService.create(dto, name(principal)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF')")
    public MaterialDto update(@PathVariable Long id, @Valid @RequestBody MaterialDto dto, Principal principal) {
        return materialService.update(id, dto, name(principal));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        materialService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private String name(Principal p) {
        return p == null ? "system" : p.getName();
    }
}
