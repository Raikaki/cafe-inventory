package com.cafe.inventory.service;

import com.cafe.inventory.dto.MaterialDto;
import com.cafe.inventory.entity.Material;
import com.cafe.inventory.exception.BusinessException;
import com.cafe.inventory.exception.ResourceNotFoundException;
import com.cafe.inventory.repository.MaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MaterialService {

    private final MaterialRepository materialRepository;

    public List<MaterialDto> findAll() {
        return materialRepository.findAll().stream().map(MaterialDto::from).toList();
    }

    public List<Material> findAllActive() {
        return materialRepository.findByActiveFlagTrue();
    }

    public MaterialDto findById(Long id) {
        return MaterialDto.from(get(id));
    }

    @Transactional
    public MaterialDto create(MaterialDto dto, String user) {
        if (materialRepository.existsByMaterialCode(dto.materialCode())) {
            throw new BusinessException("Material code already exists: " + dto.materialCode());
        }
        Material m = new Material();
        apply(m, dto);
        m.setCreatedBy(user);
        return MaterialDto.from(materialRepository.save(m));
    }

    @Transactional
    public MaterialDto update(Long id, MaterialDto dto, String user) {
        Material m = get(id);
        apply(m, dto);
        m.setUpdatedBy(user);
        return MaterialDto.from(materialRepository.save(m));
    }

    @Transactional
    public void delete(Long id) {
        Material m = get(id);
        m.setActiveFlag(false);
        materialRepository.save(m);
    }

    private void apply(Material m, MaterialDto dto) {
        m.setMaterialCode(dto.materialCode());
        m.setMaterialName(dto.materialName());
        m.setUnit(dto.unit());
        m.setCurrentQty(dto.currentQty());
        m.setMinimumQty(dto.minimumQty());
        m.setMaximumQty(dto.maximumQty());
        m.setAverageCost(dto.averageCost());
        if (dto.activeFlag() != null) {
            m.setActiveFlag(dto.activeFlag());
        }
    }

    private Material get(Long id) {
        return materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material not found: " + id));
    }
}
