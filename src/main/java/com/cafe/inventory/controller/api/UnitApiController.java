package com.cafe.inventory.controller.api;

import com.cafe.inventory.entity.Unit;
import com.cafe.inventory.exception.BusinessException;
import com.cafe.inventory.exception.ResourceNotFoundException;
import com.cafe.inventory.repository.UnitRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Units")
@RestController
@RequestMapping("/api/units")
@RequiredArgsConstructor
public class UnitApiController {

    private final UnitRepository unitRepository;

    @GetMapping
    public List<Unit> list() {
        return unitRepository.findAll();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public Unit create(@RequestBody Unit body) {
        if (body.getCode() == null || body.getCode().isBlank())
            throw new BusinessException("Mã đơn vị không được trống");
        if (unitRepository.existsByCode(body.getCode().trim()))
            throw new BusinessException("Đơn vị đã tồn tại: " + body.getCode());
        Unit u = new Unit();
        u.setCode(body.getCode().trim());
        u.setName(body.getName());
        u.setActiveFlag(true);
        return unitRepository.save(u);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public Unit update(@PathVariable Long id, @RequestBody Unit body) {
        Unit u = unitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found: " + id));
        u.setCode(body.getCode());
        u.setName(body.getName());
        if (body.getActiveFlag() != null) u.setActiveFlag(body.getActiveFlag());
        return unitRepository.save(u);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        unitRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
