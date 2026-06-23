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
@Table(name = "inventory_transactions")
public class InventoryTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "txn_id")
    private Long id;

    @Column(name = "txn_date", nullable = false)
    private LocalDateTime txnDate = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "txn_type", nullable = false, length = 20)
    private TransactionType txnType;

    @Column(name = "reference_no", length = 40)
    private String referenceNo;

    @Column(name = "material_id", nullable = false)
    private Long materialId;

    @Column(name = "quantity", nullable = false, precision = 18, scale = 3)
    private BigDecimal quantity;

    @Column(name = "before_qty", nullable = false, precision = 18, scale = 3)
    private BigDecimal beforeQty;

    @Column(name = "after_qty", nullable = false, precision = 18, scale = 3)
    private BigDecimal afterQty;

    @Column(name = "unit_cost", nullable = false, precision = 18, scale = 2)
    private BigDecimal unitCost = BigDecimal.ZERO;

    @Column(name = "note", length = 500)
    private String note;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
