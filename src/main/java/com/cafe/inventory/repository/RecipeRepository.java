package com.cafe.inventory.repository;

import com.cafe.inventory.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    Optional<Recipe> findFirstByProductIdAndActiveFlagTrue(Long productId);
}
