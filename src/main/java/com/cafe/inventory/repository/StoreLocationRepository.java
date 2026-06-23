package com.cafe.inventory.repository;

import com.cafe.inventory.entity.StoreLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreLocationRepository extends JpaRepository<StoreLocation, Long> {
    Optional<StoreLocation> findFirstByOrderByIdAsc();
}
