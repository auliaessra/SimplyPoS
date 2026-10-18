-- ============================================================
-- Simply PoS 1.0 - Sales & AR (Phase 4)
-- ============================================================

-- ============== SALES ORDERS (POS) ==============
CREATE TABLE IF NOT EXISTS sales_orders (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL REFERENCES stores(id),
    customer_id BIGINT REFERENCES customers(id),
    order_no VARCHAR(50) NOT NULL UNIQUE,
    order_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    payment_method VARCHAR(30) NOT NULL DEFAULT 'CASH', -- CASH, QRIS, CREDIT
    status VARCHAR(30) NOT NULL DEFAULT 'COMPLETED',   -- COMPLETED, VOID
    subtotal NUMERIC(15,2) NOT NULL DEFAULT 0,
    discount_amount NUMERIC(15,2) NOT NULL DEFAULT 0,
    tax_amount NUMERIC(15,2) NOT NULL DEFAULT 0,
    total_amount NUMERIC(15,2) NOT NULL DEFAULT 0,
    paid_amount NUMERIC(15,2) NOT NULL DEFAULT 0,
    change_amount NUMERIC(15,2) NOT NULL DEFAULT 0,
    notes TEXT,
    created_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_so_store ON sales_orders(store_id);
CREATE INDEX idx_so_customer ON sales_orders(customer_id);
CREATE INDEX idx_so_date ON sales_orders(order_date);

CREATE TABLE IF NOT EXISTS sales_order_items (
    id BIGSERIAL PRIMARY KEY,
    sales_order_id BIGINT NOT NULL REFERENCES sales_orders(id) ON DELETE CASCADE,
    item_id BIGINT NOT NULL REFERENCES items(id),
    qty_ml NUMERIC(15,3) NOT NULL,
    unit_price NUMERIC(15,2) NOT NULL,
    discount_percent NUMERIC(5,2) DEFAULT 0,
    total_price NUMERIC(15,2) NOT NULL,
    unit_cost NUMERIC(15,2) NOT NULL DEFAULT 0,   -- HPP saat transaksi
    total_cost NUMERIC(15,2) NOT NULL DEFAULT 0   -- total HPP
);
CREATE INDEX idx_soi_order ON sales_order_items(sales_order_id);

-- ============== CUSTOMER PAYMENTS (AR COLLECTION) ==============
CREATE TABLE IF NOT EXISTS customer_payments (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL REFERENCES stores(id),
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    sales_order_id BIGINT REFERENCES sales_orders(id),
    payment_no VARCHAR(50) NOT NULL,
    payment_date DATE NOT NULL,
    amount NUMERIC(15,2) NOT NULL,
    payment_method VARCHAR(30) NOT NULL DEFAULT 'CASH',
    bank_account VARCHAR(100),
    notes TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'POSTED', -- POSTED, VOID
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_cp_store_no UNIQUE (store_id, payment_no)
);
CREATE INDEX idx_cp_store ON customer_payments(store_id);
CREATE INDEX idx_cp_customer ON customer_payments(customer_id);

-- ============== CUSTOMER LEDGERS (AR) ==============
CREATE TABLE IF NOT EXISTS customer_ledgers (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL REFERENCES stores(id),
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    document_type VARCHAR(30) NOT NULL, -- SALE, RECEIPT, ADJUSTMENT
    document_no VARCHAR(50) NOT NULL,
    document_id BIGINT,
    transaction_date DATE NOT NULL,
    amount NUMERIC(15,2) NOT NULL,     -- + = AR increase, - = AR reduce
    balance NUMERIC(15,2) NOT NULL DEFAULT 0,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_cl_store ON customer_ledgers(store_id);
CREATE INDEX idx_cl_customer ON customer_ledgers(customer_id);

-- ============== Sequences ==============
INSERT INTO sequences (name, prefix, current_value) VALUES
('SO',  'SO',  0),
('CPAY','CPAY',0)
ON CONFLICT (name) DO NOTHING;