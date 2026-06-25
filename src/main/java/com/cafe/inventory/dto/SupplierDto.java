package com.cafe.inventory.dto;

import com.cafe.inventory.entity.Supplier;
import jakarta.validation.constraints.NotBlank;

public record SupplierDto(
        Long id,
        @NotBlank String supplierCode,
        @NotBlank String supplierName,
        String address,
        String phone,
        String email,
        Boolean activeFlag
) {
    public static SupplierDto from(Supplier s) {
        return new SupplierDto(s.getId(), s.getSupplierCode(), s.getSupplierName(),
                s.getAddress(), s.getPhone(), s.getEmail(), s.getActiveFlag());
    }
}
