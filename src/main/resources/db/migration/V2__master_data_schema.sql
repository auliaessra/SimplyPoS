-- ============================================================
-- Simply PoS 1.0 - Master Data (Phase 2)
-- ============================================================

-- ============== CHART OF ACCOUNTS (GLOBAL) ==============
CREATE TABLE IF NOT EXISTS chart_of_accounts (
    id BIGSERIAL PRIMARY KEY,
    account_code VARCHAR(30) NOT NULL UNIQUE,
    account_name VARCHAR(100) NOT NULL,
    account_type VARCHAR(30) NOT NULL, -- ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE
    parent_id BIGINT REFERENCES chart_of_accounts(id) ON DELETE SET NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_coa_code ON chart_of_accounts(account_code);
CREATE INDEX idx_coa_type ON chart_of_accounts(account_type);

-- ============== ITEMS (GLOBAL, UNIT LOCKED TO 'ML') ==============
CREATE TABLE IF NOT EXISTS items (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    unit VARCHAR(10) NOT NULL DEFAULT 'ML',
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_items_unit CHECK (unit = 'ML')
);

CREATE INDEX idx_items_code ON items(code);
CREATE INDEX idx_items_name ON items(name);

-- ============== ITEM STORE PRICES (PER STORE) ==============
CREATE TABLE IF NOT EXISTS item_store_prices (
    id BIGSERIAL PRIMARY KEY,
    item_id BIGINT NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    store_id BIGINT NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    buy_price NUMERIC(15,2) NOT NULL DEFAULT 0,
    sell_price NUMERIC(15,2) NOT NULL DEFAULT 0,
    stock_qty_ml NUMERIC(15,3) NOT NULL DEFAULT 0,
    min_stock_ml NUMERIC(15,3) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_item_store UNIQUE (item_id, store_id),
    CONSTRAINT chk_stock_non_negative CHECK (stock_qty_ml >= 0)
);

CREATE INDEX idx_isp_item ON item_store_prices(item_id);
CREATE INDEX idx_isp_store ON item_store_prices(store_id);

-- ============== VENDORS (PER STORE) ==============
CREATE TABLE IF NOT EXISTS vendors (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    code VARCHAR(30) NOT NULL,
    name VARCHAR(150) NOT NULL,
    phone VARCHAR(30),
    email VARCHAR(190),
    address TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_vendor_store_code UNIQUE (store_id, code)
);

CREATE INDEX idx_vendors_store ON vendors(store_id);

-- ============== CUSTOMERS (PER STORE) ==============
CREATE TABLE IF NOT EXISTS customers (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    code VARCHAR(30) NOT NULL,
    name VARCHAR(150) NOT NULL,
    phone VARCHAR(30),
    email VARCHAR(190),
    address TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_customer_store_code UNIQUE (store_id, code)
);

CREATE INDEX idx_customers_store ON customers(store_id);

-- ============================================================
-- SEEDER: CoA Standar (Double-Entry)
-- ============================================================
INSERT INTO chart_of_accounts (account_code, account_name, account_type) VALUES
-- ASSETS
('1101', 'Kas', 'ASSET'),
('1102', 'Bank', 'ASSET'),
('1201', 'Piutang Usaha (AR)', 'ASSET'),
('1301', 'Persediaan Barang Dagang', 'ASSET'),
-- LIABILITIES
('2101', 'Hutang Usaha (AP)', 'LIABILITY'),
('2102', 'Hutang Pajak', 'LIABILITY'),
-- EQUITY
('3101', 'Modal Pemilik', 'EQUITY'),
('3102', 'Laba Ditahan', 'EQUITY'),
-- REVENUE
('4101', 'Penjualan', 'REVENUE'),
('4102', 'Diskon Penjualan', 'REVENUE'),
-- EXPENSE
('5101', 'Harga Pokok Penjualan (HPP)', 'EXPENSE'),
('5102', 'Beban Gaji', 'EXPENSE'),
('5103', 'Beban Listrik & Air', 'EXPENSE'),
('5104', 'Beban Sewa', 'EXPENSE')
ON CONFLICT (account_code) DO NOTHING;

-- Seeder Item contoh
INSERT INTO items (code, name, unit, description) VALUES
('ITM-001', 'Parfum Mawar Premium', 'ML', 'Parfum wangi mawar kualitas premium'),
('ITM-002', 'Parfum Melati Elegance', 'ML', 'Parfum wangi melati elegan'),
('ITM-003', 'Parfum Sandalwood Classic', 'ML', 'Parfum kayu cendana klasik')
ON CONFLICT (code) DO NOTHING;