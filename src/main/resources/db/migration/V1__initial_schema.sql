-- ============================================================
-- Simply PoS 1.0 - Skema Awal (Phase 1)
-- ============================================================

-- Tabel Stores (cabang)
CREATE TABLE IF NOT EXISTS stores (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    address TEXT,
    phone VARCHAR(30),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabel Users (pengguna sistem)
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT REFERENCES stores(id) ON DELETE SET NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    role VARCHAR(30) NOT NULL, -- SUPER_ADMIN, ADMIN, CASHIER, STAFF
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, INACTIVE
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index untuk performa
CREATE INDEX idx_users_store_id ON users(store_id);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_role ON users(role);

-- ============================================================
-- Data Awal (seeder)
-- ============================================================

-- Insert Store default
INSERT INTO stores (code, name, address, phone)
VALUES ('SP001', 'Simply PoS Pusat', 'Jl. Simply PoS No. 1, Jakarta', '08123456789')
ON CONFLICT (code) DO NOTHING;

-- Insert User Super Admin (password: admin123, di-hash dengan BCrypt)
-- Hash untuk 'admin123' dengan BCrypt strength 10
INSERT INTO users (store_id, username, password, full_name, role, status)
VALUES (
    NULL, -- Super Admin tidak terikat store
    'superadmin',
    '$2a$10$Jt0Yx5PFcHP6h3TbS8KqUuS2VW5xL7yR1kA2bC3dE4fG5hI6jK7lM', -- admin123
    'Super Administrator',
    'SUPER_ADMIN',
    'ACTIVE'
) ON CONFLICT (username) DO NOTHING;

-- Insert User Admin untuk store PG001
INSERT INTO users (store_id, username, password, full_name, role, status)
VALUES (
    (SELECT id FROM stores WHERE code = 'PG001'),
    'admin',
    '$2a$10$Jt0Yx5PFcHP6h3TbS8KqUuS2VW5xL7yR1kA2bC3dE4fG5hI6jK7lM', -- admin123
    'Store Administrator',
    'ADMIN',
    'ACTIVE'
) ON CONFLICT (username) DO NOTHING;

-- Insert User Cashier
INSERT INTO users (store_id, username, password, full_name, role, status)
VALUES (
    (SELECT id FROM stores WHERE code = 'PG001'),
    'cashier',
    '$2a$10$Jt0Yx5PFcHP6h3TbS8KqUuS2VW5xL7yR1kA2bC3dE4fG5hI6jK7lM', -- admin123
    'Kasir Toko',
    'CASHIER',
    'ACTIVE'
) ON CONFLICT (username) DO NOTHING;