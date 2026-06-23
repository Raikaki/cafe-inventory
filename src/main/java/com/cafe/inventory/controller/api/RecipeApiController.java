package com.cafe.inventory.controller.api;

import com.cafe.inventory.dto.RecipeDtos.*;
import com.cafe.inventory.service.RecipeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Tag(name = "Recipes")
@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeApiController {

    private final RecipeService recipeService;

    @GetMapping("/product/{productId}")
    public RecipeView getByProduct(@PathVariable Long productId) {
        return recipeService.getByProductId(productId);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public RecipeView save(@Valid @RequestBody RecipeSaveRequest request, Principal principal) {
        return recipeService.save(request, principal == null ? "system" : principal.getName());
    }
}
