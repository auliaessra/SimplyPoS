-- ============================================================
-- Fix NULL values di item_store_prices
-- ============================================================

-- Set default 0 untuk null values
UPDATE item_store_prices
SET
    stock_qty_ml = 0
WHERE
    stock_qty_ml IS NULL;

UPDATE item_store_prices
SET
    min_stock_ml = 0
WHERE
    min_stock_ml IS NULL;

UPDATE item_store_prices SET buy_price = 0 WHERE buy_price IS NULL;

UPDATE item_store_prices SET sell_price = 0 WHERE sell_price IS NULL;

-- Set NOT NULL constraint
ALTER TABLE item_store_prices
ALTER COLUMN stock_qty_ml
SET NOT NULL,
ALTER COLUMN min_stock_ml
SET NOT NULL,
ALTER COLUMN buy_price
SET NOT NULL,
ALTER COLUMN sell_price
SET NOT NULL;

-- Set default value
ALTER TABLE item_store_prices
ALTER COLUMN stock_qty_ml
SET DEFAULT 0,
ALTER COLUMN min_stock_ml
SET DEFAULT 0,
ALTER COLUMN buy_price
SET DEFAULT 0,
ALTER COLUMN sell_price
SET DEFAULT 0;