-- ============================================================
-- Simply PoS 1.0 - Inventory & General Ledger (Phase 5)
-- ============================================================

-- ============== ITEM JOURNALS (Stock Adjustment) ==============
CREATE TABLE IF NOT EXISTS item_journals (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL REFERENCES stores(id),
    document_no VARCHAR(50) NOT NULL,
    journal_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT', -- DRAFT, POSTED, CANCELLED
    reason VARCHAR(50) NOT NULL DEFAULT 'OPNAME', -- OPNAME, DAMAGE, LOSS, FOUND, OTHER
    notes TEXT,
    posted_at TIMESTAMP,
    created_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_ij_store_no UNIQUE (store_id, document_no)
);
CREATE INDEX idx_ij_store ON item_journals(store_id);
CREATE INDEX idx_ij_status ON item_journals(status);
CREATE INDEX idx_ij_date ON item_journals(journal_date);

CREATE TABLE IF NOT EXISTS item_journal_items (
    id BIGSERIAL PRIMARY KEY,
    item_journal_id BIGINT NOT NULL REFERENCES item_journals(id) ON DELETE CASCADE,
    item_id BIGINT NOT NULL REFERENCES items(id),
    qty_ml_adjustment NUMERIC(15,3) NOT NULL, -- + add, - reduce
    unit_cost NUMERIC(15,2) NOT NULL DEFAULT 0,
    total_cost NUMERIC(15,2) NOT NULL DEFAULT 0,
    notes TEXT
);
CREATE INDEX idx_iji_journal ON item_journal_items(item_journal_id);

-- ============== SEQUENCE ==============
INSERT INTO sequences (name, prefix, current_value) VALUES
('IJ', 'IJ', 0)
ON CONFLICT (name) DO NOTHING;

-- ============== CoA Tambahan untuk Penyesuaian Stok ==============
INSERT INTO chart_of_accounts (account_code, account_name, account_type) VALUES
('5901', 'Beban Penyesuaian Stok', 'EXPENSE'),
('5902', 'Pendapatan Penyesuaian Stok', 'REVENUE')
ON CONFLICT (account_code) DO NOTHING;