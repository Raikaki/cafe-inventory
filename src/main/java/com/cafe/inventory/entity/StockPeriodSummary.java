package com.cafe.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "stock_period_summary")
public class StockPeriodSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "summary_id")
    private Long id;

    @Column(name = "period_year", nullable = false)
    private Integer periodYear;

    @Column(name = "period_month", nullable = false)
    private Integer periodMonth;

    @Column(name = "material_id", nullable = false)
    private Long materialId;

    @Column(name = "material_code")
    private String materialCode;

    @Column(name = "material_name")
    private String materialName;

    @Column(name = "unit")
    private String unit;

    @Column(name = "opening_qty", precision = 18, scale = 3)
    private BigDecimal openingQty = BigDecimal.ZERO;

    @Column(name = "receipt_qty", precision = 18, scale = 3)
    private BigDecimal receiptQty = BigDecimal.ZERO;

    @Column(name = "consumption_qty", precision = 18, scale = 3)
    private BigDecimal consumptionQty = BigDecimal.ZERO;

    @Column(name = "adjustment_qty", precision = 18, scale = 3)
    private BigDecimal adjustmentQty = BigDecimal.ZERO;

    @Column(name = "closing_qty", precision = 18, scale = 3)
    private BigDecimal closingQty = BigDecimal.ZERO;

    @Column(name = "unit_cost", precision = 18, scale = 2)
    private BigDecimal unitCost = BigDecimal.ZERO;

    @Column(name = "closing_value", precision = 18, scale = 2)
    private BigDecimal closingValue = BigDecimal.ZERO;

    @Column(name = "created_by")
    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
