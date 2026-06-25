package com.cafe.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "vouchers")
public class Voucher extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "voucher_id")
    private Long id;

    @Column(name = "voucher_type", nullable = false, length = 30)
    private String voucherType;

    @Column(name = "voucher_no", nullable = false, unique = true, length = 40)
    private String voucherNo;

    @Column(name = "voucher_date", nullable = false)
    private LocalDate voucherDate;

    @Column(name = "creator_name")
    private String creatorName;

    @Column(name = "creator_address", length = 500)
    private String creatorAddress;

    @Column(name = "partner_name")
    private String partnerName;

    @Column(name = "partner_address", length = 500)
    private String partnerAddress;

    @Column(name = "content", length = 1000)
    private String content;

    @Column(name = "quantity", precision = 18, scale = 3)
    private BigDecimal quantity;

    @Column(name = "unit", length = 50)
    private String unit;

    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "amount_in_words", length = 500)
    private String amountInWords;

    @Column(name = "approver_name")
    private String approverName;

    @Column(name = "note", length = 500)
    private String note;

    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl;
}
