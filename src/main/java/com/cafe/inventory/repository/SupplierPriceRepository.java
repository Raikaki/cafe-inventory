package com.cafe.inventory.repository;

import com.cafe.inventory.entity.SupplierPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SupplierPriceRepository extends JpaRepository<SupplierPrice, Long> {
    Optional<SupplierPrice> findByMaterialIdAndSupplierId(Long materialId, Long supplierId);
}
