package com.cafe.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "stock_count_details")
public class StockCountDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "count_id", nullable = false)
    private StockCount stockCount;

    @Column(name = "material_id", nullable = false)
    private Long materialId;

    @Column(name = "system_qty", precision = 18, scale = 3)
    private BigDecimal systemQty = BigDecimal.ZERO;

    @Column(name = "actual_qty", precision = 18, scale = 3)
    private BigDecimal actualQty = BigDecimal.ZERO;

    @Column(name = "diff_qty", precision = 18, scale = 3)
    private BigDecimal diffQty = BigDecimal.ZERO;
}
