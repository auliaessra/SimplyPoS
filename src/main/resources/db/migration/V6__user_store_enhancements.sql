-- ============================================================
-- Simply PoS 1.0 - User & Store Enhancements
-- ============================================================

-- Tambahan kolom untuk users
ALTER TABLE users ADD COLUMN IF NOT EXISTS phone VARCHAR(30);

ALTER TABLE users ADD COLUMN IF NOT EXISTS avatar_path VARCHAR(255);

ALTER TABLE users ADD COLUMN IF NOT EXISTS last_login_at TIMESTAMP;

ALTER TABLE users ADD COLUMN IF NOT EXISTS last_login_ip VARCHAR(45);

ALTER TABLE users ADD COLUMN IF NOT EXISTS updated_by VARCHAR(50);

-- Tambahan kolom untuk stores
ALTER TABLE stores
ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE stores ADD COLUMN IF NOT EXISTS email VARCHAR(190);

ALTER TABLE stores
ADD COLUMN IF NOT EXISTS manager_name VARCHAR(150);

-- Index
CREATE INDEX IF NOT EXISTS idx_stores_active ON stores (is_active);

CREATE INDEX IF NOT EXISTS idx_users_status ON users (status);