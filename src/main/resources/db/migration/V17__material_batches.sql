-- =========================================================
-- V17: Material batches with expiry (lô + hạn sử dụng) for FEFO/expiry alerts.
-- =========================================================

CREATE TABLE material_batches (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    material_id   BIGINT        NOT NULL,
    batch_no      VARCHAR(50),
    supplier_id   BIGINT,
    received_date DATE,
    expiry_date   DATE,
    received_qty  DECIMAL(18,3) NOT NULL DEFAULT 0,
    remaining_qty DECIMAL(18,3) NOT NULL DEFAULT 0,
    note          VARCHAR(500),
    created_by    VARCHAR(100),
    created_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_batch_material FOREIGN KEY (material_id) REFERENCES materials (material_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_batch_material ON material_batches (material_id);
CREATE INDEX idx_batch_expiry   ON material_batches (expiry_date);
