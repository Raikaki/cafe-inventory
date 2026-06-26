-- =========================================================
-- V16: Unit of measure conversion. A material has a base unit (unit) and an
-- optional purchase unit; 1 purchase_unit = conversion_factor base units.
-- e.g. base = g, purchase = kg, factor = 1000.
-- =========================================================

ALTER TABLE materials
    ADD COLUMN purchase_unit     VARCHAR(20)   NULL          AFTER unit,
    ADD COLUMN conversion_factor DECIMAL(18,4) NOT NULL DEFAULT 1 AFTER purchase_unit;
