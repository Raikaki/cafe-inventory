-- =========================================================
-- V13: Supplier price quotes (báo giá nhà cung cấp) per material.
-- Used together with historical goods-receipt prices to compare suppliers
-- and recommend the cheapest one for each material.
-- =========================================================

CREATE TABLE supplier_prices (
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    material_id  BIGINT        NOT NULL,
    supplier_id  BIGINT        NOT NULL,
    price        DECIMAL(18,2) NOT NULL DEFAULT 0,
    note         VARCHAR(500),
    created_by   VARCHAR(100),
    created_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_supplier_price UNIQUE (material_id, supplier_id),
    CONSTRAINT fk_sp_material FOREIGN KEY (material_id) REFERENCES materials (material_id),
    CONSTRAINT fk_sp_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers (supplier_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_sp_material ON supplier_prices (material_id);
