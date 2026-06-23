package com.cafe.inventory.repository;

import com.cafe.inventory.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MaterialRepository extends JpaRepository<Material, Long> {
    Optional<Material> findByMaterialCode(String materialCode);
    boolean existsByMaterialCode(String materialCode);
    List<Material> findByActiveFlagTrue();

    @Query("SELECT m FROM Material m WHERE m.activeFlag = true AND m.currentQty < m.minimumQty")
    List<Material> findLowStock();
}
