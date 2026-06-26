-- =========================================================
-- V18: Unit of measure catalog (danh mục đơn vị tính).
-- =========================================================

CREATE TABLE units (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    code        VARCHAR(20) NOT NULL,
    name        VARCHAR(100),
    active_flag TINYINT     NOT NULL DEFAULT 1,
    created_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_unit_code UNIQUE (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO units (code, name) VALUES
('g', 'Gram'), ('kg', 'Kilogram'), ('ml', 'Mililít'), ('l', 'Lít'),
('cái', 'Cái'), ('chai', 'Chai'), ('thùng', 'Thùng'), ('gói', 'Gói'), ('hộp', 'Hộp'), ('bao', 'Bao');
