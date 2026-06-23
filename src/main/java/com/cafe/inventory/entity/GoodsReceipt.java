package com.cafe.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "goods_receipts")
public class GoodsReceipt extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "receipt_id")
    private Long id;

    @Column(name = "receipt_no", nullable = false, unique = true, length = 30)
    private String receiptNo;

    @Column(name = "receipt_date", nullable = false)
    private LocalDate receiptDate;

    @Column(name = "supplier_id")
    private Long supplierId;

    @Column(name = "note", length = 500)
    private String note;

    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @OneToMany(mappedBy = "goodsReceipt", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<GoodsReceiptDetail> details = new ArrayList<>();

    public void addDetail(GoodsReceiptDetail detail) {
        detail.setGoodsReceipt(this);
        this.details.add(detail);
    }
}
