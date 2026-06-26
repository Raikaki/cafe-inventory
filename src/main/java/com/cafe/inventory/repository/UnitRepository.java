package com.cafe.inventory.repository;

import com.cafe.inventory.entity.Unit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnitRepository extends JpaRepository<Unit, Long> {
    boolean existsByCode(String code);
}
