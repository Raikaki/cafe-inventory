package com.cafe.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "recipes")
public class Recipe extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recipe_id")
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "version_no", nullable = false)
    private Integer versionNo = 1;

    @Column(name = "active_flag", nullable = false)
    private Boolean activeFlag = true;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<RecipeDetail> details = new ArrayList<>();

    public void addDetail(RecipeDetail detail) {
        detail.setRecipe(this);
        this.details.add(detail);
    }
}
