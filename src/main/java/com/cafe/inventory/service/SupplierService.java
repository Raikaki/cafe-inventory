package com.cafe.inventory.service;

import com.cafe.inventory.dto.SupplierDto;
import com.cafe.inventory.entity.Supplier;
import com.cafe.inventory.exception.BusinessException;
import com.cafe.inventory.exception.ResourceNotFoundException;
import com.cafe.inventory.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public List<SupplierDto> findAll() {
        return supplierRepository.findAll().stream().map(SupplierDto::from).toList();
    }

    public SupplierDto findById(Long id) {
        return SupplierDto.from(get(id));
    }

    @Transactional
    public SupplierDto create(SupplierDto dto, String user) {
        if (supplierRepository.existsBySupplierCode(dto.supplierCode())) {
            throw new BusinessException("Mã nhà cung cấp đã tồn tại: " + dto.supplierCode());
        }
        Supplier s = new Supplier();
        apply(s, dto);
        s.setCreatedBy(user);
        return SupplierDto.from(supplierRepository.save(s));
    }

    @Transactional
    public SupplierDto update(Long id, SupplierDto dto, String user) {
        Supplier s = get(id);
        apply(s, dto);
        s.setUpdatedBy(user);
        return SupplierDto.from(supplierRepository.save(s));
    }

    @Transactional
    public void delete(Long id) {
        Supplier s = get(id);
        s.setActiveFlag(false);
        supplierRepository.save(s);
    }

    private void apply(Supplier s, SupplierDto dto) {
        s.setSupplierCode(dto.supplierCode());
        s.setSupplierName(dto.supplierName());
        s.setAddress(dto.address());
        s.setPhone(dto.phone());
        s.setEmail(dto.email());
        if (dto.activeFlag() != null) {
            s.setActiveFlag(dto.activeFlag());
        }
    }

    private Supplier get(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + id));
    }
}
