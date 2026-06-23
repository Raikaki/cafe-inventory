package com.cafe.inventory.service;

import com.cafe.inventory.dto.MaterialDto;
import com.cafe.inventory.entity.Material;
import com.cafe.inventory.exception.BusinessException;
import com.cafe.inventory.exception.ResourceNotFoundException;
import com.cafe.inventory.repository.MaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final InventoryService inventoryService;

    public List<MaterialDto> findAll() {
        return materialRepository.findAll().stream().map(MaterialDto::from).toList();
    }

    public List<Material> findAllActive() {
        return materialRepository.findByActiveFlagTrue();
    }

    public MaterialDto findById(Long id) {
        return MaterialDto.from(get(id));
    }

    /**
     * Create a material. Any initial stock is recorded as an "opening" inventory
     * transaction (not written directly to current_qty) so the ledger stays the
     * single source of truth.
     */
    @Transactional
    public MaterialDto create(MaterialDto dto, String user) {
        if (materialRepository.existsByMaterialCode(dto.materialCode())) {
            throw new BusinessException("Material code already exists: " + dto.materialCode());
        }
        Material m = new Material();
        m.setMaterialCode(dto.materialCode());
        m.setMaterialName(dto.materialName());
        m.setUnit(dto.unit());
        m.setMinimumQty(nz(dto.minimumQty()));
        m.setMaximumQty(nz(dto.maximumQty()));
        m.setAverageCost(nz(dto.averageCost()));
        m.setCurrentQty(BigDecimal.ZERO);
        m.setActiveFlag(dto.activeFlag() == null ? true : dto.activeFlag());
        m.setCreatedBy(user);
        Material saved = materialRepository.save(m);

        BigDecimal opening = nz(dto.currentQty());
        if (opening.compareTo(BigDecimal.ZERO) != 0) {
            inventoryService.adjust(saved.getId(), opening, "OPENING", user, "Tồn đầu kỳ");
        }
        return MaterialDto.from(get(saved.getId()));
    }

    /**
     * Update material attributes. Stock fields (current_qty) are intentionally
     * NOT editable here — stock only changes through inventory transactions
     * (goods receipt, sales consumption, adjustment).
     */
    @Transactional
    public MaterialDto update(Long id, MaterialDto dto, String user) {
        Material m = get(id);
        m.setMaterialCode(dto.materialCode());
        m.setMaterialName(dto.materialName());
        m.setUnit(dto.unit());
        m.setMinimumQty(nz(dto.minimumQty()));
        m.setMaximumQty(nz(dto.maximumQty()));
        m.setAverageCost(nz(dto.averageCost()));
        if (dto.activeFlag() != null) {
            m.setActiveFlag(dto.activeFlag());
        }
        m.setUpdatedBy(user);
        return MaterialDto.from(materialRepository.save(m));
    }

    @Transactional
    public void delete(Long id) {
        Material m = get(id);
        m.setActiveFlag(false);
        materialRepository.save(m);
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private Material get(Long id) {
        return materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material not found: " + id));
    }
}
