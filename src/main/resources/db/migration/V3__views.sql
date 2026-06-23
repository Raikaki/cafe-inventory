-- =========================================================
-- V3: Reporting views
-- =========================================================

-- Materials currently below minimum quantity
CREATE OR REPLACE VIEW vw_low_stock AS
SELECT
    m.material_id,
    m.material_code,
    m.material_name,
    m.unit,
    m.current_qty,
    m.minimum_qty,
    (m.minimum_qty - m.current_qty) AS shortage_qty
FROM materials m
WHERE m.active_flag = 1
  AND m.current_qty < m.minimum_qty;

-- Inventory transactions with material name (inventory card)
CREATE OR REPLACE VIEW vw_inventory_transaction AS
SELECT
    t.txn_id,
    t.txn_date,
    t.txn_type,
    t.reference_no,
    t.material_id,
    m.material_code,
    m.material_name,
    m.unit,
    t.quantity,
    t.before_qty,
    t.after_qty,
    t.unit_cost,
    t.note,
    t.created_by
FROM inventory_transactions t
JOIN materials m ON m.material_id = t.material_id;

-- Product cost = SUM(standard_qty * material.average_cost) over the active recipe
CREATE OR REPLACE VIEW vw_product_cost AS
SELECT
    p.product_id,
    p.product_code,
    p.product_name,
    p.sale_price,
    COALESCE(SUM(rd.standard_qty * m.average_cost), 0) AS total_cost
FROM products p
LEFT JOIN recipes r        ON r.product_id = p.product_id AND r.active_flag = 1
LEFT JOIN recipe_details rd ON rd.recipe_id = r.recipe_id
LEFT JOIN materials m       ON m.material_id = rd.material_id
GROUP BY p.product_id, p.product_code, p.product_name, p.sale_price;

-- Inventory balance summary per material (current snapshot)
CREATE OR REPLACE VIEW vw_inventory_balance AS
SELECT
    m.material_id,
    m.material_code,
    m.material_name,
    m.unit,
    m.current_qty,
    m.average_cost,
    (m.current_qty * m.average_cost) AS stock_value
FROM materials m
WHERE m.active_flag = 1;
