-- ============================================================
-- Parfume Gallery 1.0 - Add barcode column to items
-- ============================================================

ALTER TABLE items ADD COLUMN IF NOT EXISTS barcode VARCHAR(100);

CREATE INDEX IF NOT EXISTS idx_items_barcode ON items(barcode)
WHERE barcode IS NOT NULL;