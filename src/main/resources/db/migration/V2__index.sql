-- =========================================================
-- V2: Indexes
-- =========================================================

CREATE INDEX idx_materials_active   ON materials (active_flag);
CREATE INDEX idx_products_active    ON products (active_flag);

CREATE INDEX idx_recipes_product    ON recipes (product_id, active_flag);
CREATE INDEX idx_rd_recipe          ON recipe_details (recipe_id);
CREATE INDEX idx_rd_material        ON recipe_details (material_id);

CREATE INDEX idx_gr_date            ON goods_receipts (receipt_date);
CREATE INDEX idx_grd_receipt        ON goods_receipt_details (receipt_id);
CREATE INDEX idx_grd_material       ON goods_receipt_details (material_id);

CREATE INDEX idx_sales_date         ON sales (sale_date);
CREATE INDEX idx_sales_product      ON sales (product_id);

CREATE INDEX idx_txn_date           ON inventory_transactions (txn_date);
CREATE INDEX idx_txn_material       ON inventory_transactions (material_id);
CREATE INDEX idx_txn_type           ON inventory_transactions (txn_type);
