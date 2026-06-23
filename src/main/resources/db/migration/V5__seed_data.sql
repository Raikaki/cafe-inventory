-- =========================================================
-- V5: Seed data (materials, products, recipes, supplier)
-- The ADMIN user is created by the application DataInitializer
-- so the password is hashed with the app BCrypt encoder.
-- =========================================================

-- ---------- Supplier ----------
INSERT INTO suppliers (supplier_code, supplier_name, address, phone, email, created_by)
VALUES ('SUP001', 'Default Supplier', 'Ho Chi Minh City', '0900000000', 'supplier@cafe.local', 'system');

-- ---------- Materials ----------
INSERT INTO materials (material_code, material_name, unit, current_qty, minimum_qty, maximum_qty, average_cost, created_by) VALUES
('MAT001', 'Coffee Powder',   'g',   10000, 2000, 50000, 0.30, 'system'),
('MAT002', 'Coffee Bean',     'g',   8000,  2000, 50000, 0.40, 'system'),
('MAT003', 'Condensed Milk',  'ml',  6000,  1000, 30000, 0.05, 'system'),
('MAT004', 'Fresh Milk',      'ml',  5000,  1000, 30000, 0.04, 'system'),
('MAT005', 'Sugar',           'g',   9000,  1000, 30000, 0.02, 'system'),
('MAT006', 'Peach Tea',       'g',   3000,  500,  20000, 0.50, 'system'),
('MAT007', 'Bubble',          'g',   2000,  500,  20000, 0.10, 'system'),
('MAT008', 'Ice',             'g',   50000, 5000, 200000,0.001,'system'),
('MAT009', 'Plastic Cup',     'pcs', 500,   100,  5000,  800.00,'system'),
('MAT010', 'Straw',           'pcs', 500,   100,  5000,  100.00,'system'),
('MAT011', 'Lid',             'pcs', 500,   100,  5000,  150.00,'system'),
('MAT012', 'Matcha Powder',   'g',   2000,  500,  20000, 0.80, 'system');

-- ---------- Products ----------
INSERT INTO products (product_code, product_name, sale_price, created_by) VALUES
('CF001', 'Coffee Black',  20000, 'system'),
('CF002', 'Coffee Milk',   25000, 'system'),
('CF003', 'Bac Xiu',       29000, 'system'),
('TEA001','Peach Tea',     30000, 'system'),
('TEA002','Milk Tea',      32000, 'system'),
('MAT100','Matcha Latte',  35000, 'system');

-- ---------- Recipes (one active recipe per product) ----------
INSERT INTO recipes (product_id, version_no, active_flag, created_by)
SELECT product_id, 1, 1, 'system' FROM products;

-- ---------- Recipe details ----------
-- Coffee Black: Coffee Powder 18g, Sugar 10g, Ice 120g, Cup 1, Straw 1, Lid 1
INSERT INTO recipe_details (recipe_id, material_id, standard_qty)
SELECT r.recipe_id, m.material_id, x.qty
FROM (SELECT 'CF001' pc, 'MAT001' mc, 18 qty UNION ALL
      SELECT 'CF001','MAT005',10 UNION ALL
      SELECT 'CF001','MAT008',120 UNION ALL
      SELECT 'CF001','MAT009',1 UNION ALL
      SELECT 'CF001','MAT010',1 UNION ALL
      SELECT 'CF001','MAT011',1) x
JOIN products p ON p.product_code = x.pc
JOIN recipes  r ON r.product_id = p.product_id AND r.active_flag = 1
JOIN materials m ON m.material_code = x.mc;

-- Coffee Milk: Coffee Powder 20g, Condensed Milk 30ml, Ice 150g, Cup 1, Straw 1, Lid 1
INSERT INTO recipe_details (recipe_id, material_id, standard_qty)
SELECT r.recipe_id, m.material_id, x.qty
FROM (SELECT 'CF002' pc, 'MAT001' mc, 20 qty UNION ALL
      SELECT 'CF002','MAT003',30 UNION ALL
      SELECT 'CF002','MAT008',150 UNION ALL
      SELECT 'CF002','MAT009',1 UNION ALL
      SELECT 'CF002','MAT010',1 UNION ALL
      SELECT 'CF002','MAT011',1) x
JOIN products p ON p.product_code = x.pc
JOIN recipes  r ON r.product_id = p.product_id AND r.active_flag = 1
JOIN materials m ON m.material_code = x.mc;

-- Bac Xiu: Coffee Powder 12g, Condensed Milk 25ml, Fresh Milk 40ml, Ice 150g, Cup 1, Straw 1, Lid 1
INSERT INTO recipe_details (recipe_id, material_id, standard_qty)
SELECT r.recipe_id, m.material_id, x.qty
FROM (SELECT 'CF003' pc, 'MAT001' mc, 12 qty UNION ALL
      SELECT 'CF003','MAT003',25 UNION ALL
      SELECT 'CF003','MAT004',40 UNION ALL
      SELECT 'CF003','MAT008',150 UNION ALL
      SELECT 'CF003','MAT009',1 UNION ALL
      SELECT 'CF003','MAT010',1 UNION ALL
      SELECT 'CF003','MAT011',1) x
JOIN products p ON p.product_code = x.pc
JOIN recipes  r ON r.product_id = p.product_id AND r.active_flag = 1
JOIN materials m ON m.material_code = x.mc;

-- Peach Tea: Peach Tea 15g, Sugar 12g, Ice 150g, Cup 1, Straw 1, Lid 1
INSERT INTO recipe_details (recipe_id, material_id, standard_qty)
SELECT r.recipe_id, m.material_id, x.qty
FROM (SELECT 'TEA001' pc, 'MAT006' mc, 15 qty UNION ALL
      SELECT 'TEA001','MAT005',12 UNION ALL
      SELECT 'TEA001','MAT008',150 UNION ALL
      SELECT 'TEA001','MAT009',1 UNION ALL
      SELECT 'TEA001','MAT010',1 UNION ALL
      SELECT 'TEA001','MAT011',1) x
JOIN products p ON p.product_code = x.pc
JOIN recipes  r ON r.product_id = p.product_id AND r.active_flag = 1
JOIN materials m ON m.material_code = x.mc;

-- Milk Tea: Peach Tea base 10g, Fresh Milk 50ml, Bubble 30g, Sugar 12g, Ice 120g, Cup 1, Straw 1, Lid 1
INSERT INTO recipe_details (recipe_id, material_id, standard_qty)
SELECT r.recipe_id, m.material_id, x.qty
FROM (SELECT 'TEA002' pc, 'MAT006' mc, 10 qty UNION ALL
      SELECT 'TEA002','MAT004',50 UNION ALL
      SELECT 'TEA002','MAT007',30 UNION ALL
      SELECT 'TEA002','MAT005',12 UNION ALL
      SELECT 'TEA002','MAT008',120 UNION ALL
      SELECT 'TEA002','MAT009',1 UNION ALL
      SELECT 'TEA002','MAT010',1 UNION ALL
      SELECT 'TEA002','MAT011',1) x
JOIN products p ON p.product_code = x.pc
JOIN recipes  r ON r.product_id = p.product_id AND r.active_flag = 1
JOIN materials m ON m.material_code = x.mc;

-- Matcha Latte: Matcha 18g, Fresh Milk 80ml, Sugar 10g, Ice 120g, Cup 1, Straw 1, Lid 1
INSERT INTO recipe_details (recipe_id, material_id, standard_qty)
SELECT r.recipe_id, m.material_id, x.qty
FROM (SELECT 'MAT100' pc, 'MAT012' mc, 18 qty UNION ALL
      SELECT 'MAT100','MAT004',80 UNION ALL
      SELECT 'MAT100','MAT005',10 UNION ALL
      SELECT 'MAT100','MAT008',120 UNION ALL
      SELECT 'MAT100','MAT009',1 UNION ALL
      SELECT 'MAT100','MAT010',1 UNION ALL
      SELECT 'MAT100','MAT011',1) x
JOIN products p ON p.product_code = x.pc
JOIN recipes  r ON r.product_id = p.product_id AND r.active_flag = 1
JOIN materials m ON m.material_code = x.mc;
