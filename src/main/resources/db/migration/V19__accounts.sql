-- =========================================================
-- V19: Chart of accounts (hệ thống tài khoản kế toán) + debit/credit on vouchers.
-- =========================================================

CREATE TABLE accounts (
    id           BIGINT      NOT NULL AUTO_INCREMENT,
    account_code VARCHAR(20) NOT NULL,
    account_name VARCHAR(255),
    active_flag  TINYINT     NOT NULL DEFAULT 1,
    created_at   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_account_code UNIQUE (account_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO accounts (account_code, account_name) VALUES
('111', 'Tiền mặt'),
('112', 'Tiền gửi ngân hàng'),
('131', 'Phải thu của khách hàng'),
('152', 'Nguyên liệu, vật liệu'),
('153', 'Công cụ, dụng cụ'),
('156', 'Hàng hóa'),
('211', 'Tài sản cố định'),
('331', 'Phải trả cho người bán'),
('333', 'Thuế và các khoản phải nộp Nhà nước'),
('334', 'Phải trả người lao động'),
('411', 'Vốn đầu tư của chủ sở hữu'),
('421', 'Lợi nhuận sau thuế chưa phân phối'),
('511', 'Doanh thu bán hàng và cung cấp dịch vụ'),
('632', 'Giá vốn hàng bán'),
('642', 'Chi phí quản lý kinh doanh'),
('911', 'Xác định kết quả kinh doanh');

ALTER TABLE vouchers
    ADD COLUMN debit_account  VARCHAR(20) NULL AFTER amount_in_words,
    ADD COLUMN credit_account VARCHAR(20) NULL AFTER debit_account;
