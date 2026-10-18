-- ============================================================
-- Simply PoS 1.0 - Purchasing & AP (Phase 3)
-- ============================================================

-- ============== PURCHASE ORDERS (PO) ==============
CREATE TABLE IF NOT EXISTS purchase_orders (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL REFERENCES stores(id),
    vendor_id BIGINT NOT NULL REFERENCES vendors(id),
    po_number VARCHAR(50) NOT NULL UNIQUE,
    order_date DATE NOT NULL,
    expected_date DATE,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT', -- DRAFT, CONFIRMED, RECEIVED, CANCELLED
    notes TEXT,
    total_amount NUMERIC(15,2) NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_po_store ON purchase_orders(store_id);
CREATE INDEX idx_po_vendor ON purchase_orders(vendor_id);
CREATE INDEX idx_po_status ON purchase_orders(status);

CREATE TABLE IF NOT EXISTS purchase_order_items (
    id BIGSERIAL PRIMARY KEY,
    purchase_order_id BIGINT NOT NULL REFERENCES purchase_orders(id) ON DELETE CASCADE,
    item_id BIGINT NOT NULL REFERENCES items(id),
    qty_ml NUMERIC(15,3) NOT NULL,
    unit_cost NUMERIC(15,2) NOT NULL,
    total_cost NUMERIC(15,2) NOT NULL
);
CREATE INDEX idx_poi_po ON purchase_order_items(purchase_order_id);

-- ============== PURCHASE INVOICES (PI) ==============
CREATE TABLE IF NOT EXISTS purchase_invoices (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL REFERENCES stores(id),
    vendor_id BIGINT NOT NULL REFERENCES vendors(id),
    purchase_order_id BIGINT REFERENCES purchase_orders(id),
    invoice_no VARCHAR(50) NOT NULL,
    vendor_invoice_no VARCHAR(50),
    invoice_date DATE NOT NULL,
    due_date DATE,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT', -- DRAFT, POSTED, PARTIAL_PAID, PAID, CANCELLED
    notes TEXT,
    subtotal NUMERIC(15,2) NOT NULL DEFAULT 0,
    tax_amount NUMERIC(15,2) NOT NULL DEFAULT 0,
    total_amount NUMERIC(15,2) NOT NULL DEFAULT 0,
    paid_amount NUMERIC(15,2) NOT NULL DEFAULT 0,
    posted_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_pi_store_no UNIQUE (store_id, invoice_no)
);
CREATE INDEX idx_pi_store ON purchase_invoices(store_id);
CREATE INDEX idx_pi_vendor ON purchase_invoices(vendor_id);
CREATE INDEX idx_pi_status ON purchase_invoices(status);

CREATE TABLE IF NOT EXISTS purchase_invoice_items (
    id BIGSERIAL PRIMARY KEY,
    purchase_invoice_id BIGINT NOT NULL REFERENCES purchase_invoices(id) ON DELETE CASCADE,
    item_id BIGINT NOT NULL REFERENCES items(id),
    qty_ml NUMERIC(15,3) NOT NULL,
    unit_cost NUMERIC(15,2) NOT NULL,
    total_cost NUMERIC(15,2) NOT NULL
);
CREATE INDEX idx_pii_pi ON purchase_invoice_items(purchase_invoice_id);

-- ============== VENDOR PAYMENTS ==============
CREATE TABLE IF NOT EXISTS vendor_payments (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL REFERENCES stores(id),
    vendor_id BIGINT NOT NULL REFERENCES vendors(id),
    purchase_invoice_id BIGINT REFERENCES purchase_invoices(id),
    payment_no VARCHAR(50) NOT NULL,
    payment_date DATE NOT NULL,
    amount NUMERIC(15,2) NOT NULL,
    payment_method VARCHAR(30) NOT NULL DEFAULT 'CASH', -- CASH, TRANSFER, GIRO
    bank_account VARCHAR(100),
    notes TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'POSTED', -- POSTED, VOID
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_vp_store_no UNIQUE (store_id, payment_no)
);
CREATE INDEX idx_vp_store ON vendor_payments(store_id);
CREATE INDEX idx_vp_vendor ON vendor_payments(vendor_id);

-- ============== VENDOR LEDGERS (AP) ==============
CREATE TABLE IF NOT EXISTS vendor_ledgers (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL REFERENCES stores(id),
    vendor_id BIGINT NOT NULL REFERENCES vendors(id),
    document_type VARCHAR(30) NOT NULL, -- INVOICE, PAYMENT, ADJUSTMENT
    document_no VARCHAR(50) NOT NULL,
    document_id BIGINT,
    transaction_date DATE NOT NULL,
    amount NUMERIC(15,2) NOT NULL, -- + = AP increase, - = AP reduction
    balance NUMERIC(15,2) NOT NULL DEFAULT 0,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_vl_store ON vendor_ledgers(store_id);
CREATE INDEX idx_vl_vendor ON vendor_ledgers(vendor_id);
CREATE INDEX idx_vl_date ON vendor_ledgers(transaction_date);

-- ============== ITEM LEDGERS (Inventory) ==============
CREATE TABLE IF NOT EXISTS item_ledgers (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL REFERENCES stores(id),
    item_id BIGINT NOT NULL REFERENCES items(id),
    document_type VARCHAR(30) NOT NULL, -- PURCHASE, SALE, ADJUSTMENT
    document_no VARCHAR(50) NOT NULL,
    document_id BIGINT,
    transaction_date DATE NOT NULL,
    qty_ml NUMERIC(15,3) NOT NULL, -- + = stock in, - = stock out
    unit_cost NUMERIC(15,2) NOT NULL,
    total_cost NUMERIC(15,2) NOT NULL,
    balance_qty_ml NUMERIC(15,3) NOT NULL DEFAULT 0,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_il_store ON item_ledgers(store_id);
CREATE INDEX idx_il_item ON item_ledgers(item_id);
CREATE INDEX idx_il_date ON item_ledgers(transaction_date);

-- ============== GENERAL LEDGER ==============
CREATE TABLE IF NOT EXISTS general_ledgers (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL REFERENCES stores(id),
    coa_id BIGINT NOT NULL REFERENCES chart_of_accounts(id),
    document_type VARCHAR(30) NOT NULL,
    document_no VARCHAR(50) NOT NULL,
    document_id BIGINT,
    transaction_date DATE NOT NULL,
    debit NUMERIC(15,2) NOT NULL DEFAULT 0,
    credit NUMERIC(15,2) NOT NULL DEFAULT 0,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_gl_store ON general_ledgers(store_id);
CREATE INDEX idx_gl_coa ON general_ledgers(coa_id);
CREATE INDEX idx_gl_date ON general_ledgers(transaction_date);
CREATE INDEX idx_gl_doc ON general_ledgers(document_type, document_no);

-- ============== Tambah kolom posted di CoA ==============
-- (kolom sudah ada, ini hanya dokumentasi)

-- ============== Sequence Number Generator ==============
CREATE TABLE IF NOT EXISTS sequences (
    name VARCHAR(50) PRIMARY KEY,
    prefix VARCHAR(20) NOT NULL,
    current_value BIGINT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO sequences (name, prefix, current_value) VALUES
('PO',  'PO',  0),
('PI',  'PI',  0),
('VPAY','VPAY',0)
ON CONFLICT (name) DO NOTHING;