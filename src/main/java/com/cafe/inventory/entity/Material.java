package com.cafe.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "materials")
public class Material extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "material_id")
    private Long id;

    @Column(name = "material_code", nullable = false, unique = true, length = 50)
    private String materialCode;

    @Column(name = "material_name", nullable = false)
    private String materialName;

    @Column(name = "unit", nullable = false, length = 20)
    private String unit;

    @Column(name = "current_qty", nullable = false, precision = 18, scale = 3)
    private BigDecimal currentQty = BigDecimal.ZERO;

    @Column(name = "minimum_qty", nullable = false, precision = 18, scale = 3)
    private BigDecimal minimumQty = BigDecimal.ZERO;

    @Column(name = "maximum_qty", nullable = false, precision = 18, scale = 3)
    private BigDecimal maximumQty = BigDecimal.ZERO;

    @Column(name = "average_cost", nullable = false, precision = 18, scale = 2)
    private BigDecimal averageCost = BigDecimal.ZERO;

    @Column(name = "active_flag", nullable = false)
    private Boolean activeFlag = true;
}
