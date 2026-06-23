-- =========================================================
-- V7: Materialized monthly stock summary ("period close").
-- The aggregate function computes opening/in/out/adjustment/closing + value
-- from the transaction ledger and stores one row per material per month here.
-- Reports then read directly from this table.
-- =========================================================

CREATE TABLE stock_period_summary (
    summary_id      BIGINT        NOT NULL AUTO_INCREMENT,
    period_year     INT           NOT NULL,
    period_month    INT           NOT NULL,
    material_id     BIGINT        NOT NULL,
    material_code   VARCHAR(50),
    material_name   VARCHAR(255),
    unit            VARCHAR(20),
    opening_qty     DECIMAL(18,3) NOT NULL DEFAULT 0,
    receipt_qty     DECIMAL(18,3) NOT NULL DEFAULT 0,
    consumption_qty DECIMAL(18,3) NOT NULL DEFAULT 0,
    adjustment_qty  DECIMAL(18,3) NOT NULL DEFAULT 0,
    closing_qty     DECIMAL(18,3) NOT NULL DEFAULT 0,
    unit_cost       DECIMAL(18,2) NOT NULL DEFAULT 0,
    closing_value   DECIMAL(18,2) NOT NULL DEFAULT 0,
    created_by      VARCHAR(100),
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (summary_id),
    CONSTRAINT uk_sps_period_material UNIQUE (period_year, period_month, material_id),
    CONSTRAINT fk_sps_material FOREIGN KEY (material_id) REFERENCES materials (material_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_sps_period ON stock_period_summary (period_year, period_month);
