ALTER TABLE orders
ADD COLUMN stock_deducted BOOLEAN NOT NULL DEFAULT FALSE;

-- Backfill: orders that are CONFIRMED or beyond had stock deducted
UPDATE orders
SET stock_deducted = TRUE
WHERE status IN ('CONFIRMED', 'SHIPPED', 'DELIVERED');

-- Backfill: PENDING orders created before this migration also had stock deducted
-- (the old checkout flow deducted stock immediately)
UPDATE orders
SET stock_deducted = TRUE
WHERE status = 'PENDING';