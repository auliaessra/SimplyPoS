-- ============================================================
-- Simply PoS 1.0 - Add missing email column to users
-- ============================================================
-- Error yang diperbaiki: "missing column [email] in table [users]"
-- Kolom `email` sudah ada di entity User tapi belum ada di DB.
-- ============================================================

ALTER TABLE users ADD COLUMN IF NOT EXISTS email VARCHAR(190);

-- Index untuk pencarian berdasarkan email (opsional)
CREATE INDEX IF NOT EXISTS idx_users_email ON users (email);

-- ============================================================
-- Update email untuk user yang sudah ada (opsional)
-- ============================================================
UPDATE users
SET
    email = username || '@simplypos.local'
WHERE
    email IS NULL;

-- ============================================================
-- VERIFIKASI: Seharusnya tidak ada lagi kolom yang hilang
-- ============================================================
-- Entity User fields vs Database columns:
--   id             → users.id             ✓
--   store_id       → users.store_id       ✓
--   username       → users.username       ✓
--   password       → users.password       ✓
--   full_name      → users.full_name      ✓
--   email          → users.email          ✓ (ditambahkan V7)
--   phone          → users.phone          ✓ (ditambahkan V6)
--   role           → users.role           ✓
--   status         → users.status         ✓
--   avatar_path    → users.avatar_path    ✓ (ditambahkan V6)
--   last_login_at  → users.last_login_at  ✓ (ditambahkan V6)
--   last_login_ip  → users.last_login_ip  ✓ (ditambahkan V6)
--   updated_by     → users.updated_by     ✓ (ditambahkan V6)
--   created_at     → users.created_at     ✓
--   updated_at     → users.updated_at     ✓