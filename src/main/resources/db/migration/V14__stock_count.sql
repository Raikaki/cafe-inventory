-- =========================================================
-- V14: Physical stock count (kiểm kê) -> generates adjustments.
-- =========================================================

CREATE TABLE stock_counts (
    count_id   BIGINT       NOT NULL AUTO_INCREMENT,
    count_no   VARCHAR(30)  NOT NULL,
    count_date DATE         NOT NULL,
    note       VARCHAR(500),
    created_by VARCHAR(100),
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (count_id),
    CONSTRAINT uk_count_no UNIQUE (count_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE stock_count_details (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    count_id    BIGINT        NOT NULL,
    material_id BIGINT        NOT NULL,
    system_qty  DECIMAL(18,3) NOT NULL DEFAULT 0,
    actual_qty  DECIMAL(18,3) NOT NULL DEFAULT 0,
    diff_qty    DECIMAL(18,3) NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_scd_count    FOREIGN KEY (count_id)    REFERENCES stock_counts (count_id) ON DELETE CASCADE,
    CONSTRAINT fk_scd_material FOREIGN KEY (material_id) REFERENCES materials (material_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
