-- =========================================================
-- V12: Accounting vouchers (chứng từ kế toán) register.
-- Captures the mandatory fields required by Luật Kế toán 2015 (Điều 16):
-- số/ngày chứng từ, người lập & người nhận, nội dung, số lượng/đơn giá/số tiền,
-- số tiền bằng chữ, người duyệt.
-- =========================================================

CREATE TABLE vouchers (
    voucher_id       BIGINT        NOT NULL AUTO_INCREMENT,
    voucher_type     VARCHAR(30)   NOT NULL,            -- PHIEU_THU / PHIEU_CHI / PHIEU_NHAP_KHO / PHIEU_XUAT_KHO / HOA_DON / KHAC
    voucher_no       VARCHAR(40)   NOT NULL,
    voucher_date     DATE          NOT NULL,
    creator_name     VARCHAR(255),                      -- người/đơn vị lập
    creator_address  VARCHAR(500),
    partner_name     VARCHAR(255),                      -- người/đơn vị nộp/nhận
    partner_address  VARCHAR(500),
    content          VARCHAR(1000),                     -- nội dung nghiệp vụ kinh tế
    quantity         DECIMAL(18,3),
    unit             VARCHAR(50),
    amount           DECIMAL(18,2) NOT NULL DEFAULT 0,  -- số tiền
    amount_in_words  VARCHAR(500),                      -- số tiền bằng chữ
    approver_name    VARCHAR(255),                      -- người duyệt
    note             VARCHAR(500),
    attachment_url   VARCHAR(500),
    created_by       VARCHAR(100),
    created_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by       VARCHAR(100),
    updated_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (voucher_id),
    CONSTRAINT uk_voucher_no UNIQUE (voucher_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_voucher_date ON vouchers (voucher_date);
CREATE INDEX idx_voucher_type ON vouchers (voucher_type);
