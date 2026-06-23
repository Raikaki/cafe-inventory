-- =========================================================
-- V6: Make the transaction ledger the single source of truth.
-- For every material whose current_qty differs from the sum of its existing
-- inventory_transactions, insert ONE reconciling "opening balance" transaction
-- so that SUM(inventory_transactions.quantity) == materials.current_qty.
--
-- Dated far in the past (2000-01-01) so it always falls into the "opening
-- balance" of any real reporting period, never into period movements.
-- =========================================================

INSERT INTO inventory_transactions
    (txn_date, txn_type, reference_no, material_id, quantity, before_qty, after_qty, unit_cost, note, created_by, created_at)
SELECT
    '2000-01-01 00:00:00',
    'ADJUSTMENT',
    'OPENING',
    m.material_id,
    (m.current_qty - COALESCE(s.sum_qty, 0)) AS quantity,
    0                                         AS before_qty,
    (m.current_qty - COALESCE(s.sum_qty, 0)) AS after_qty,
    m.average_cost,
    'Opening balance reconciliation',
    'system',
    '2000-01-01 00:00:00'
FROM materials m
LEFT JOIN (
    SELECT material_id, SUM(quantity) AS sum_qty
    FROM inventory_transactions
    GROUP BY material_id
) s ON s.material_id = m.material_id
WHERE (m.current_qty - COALESCE(s.sum_qty, 0)) <> 0;
