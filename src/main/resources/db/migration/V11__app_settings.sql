-- ============================================================
-- Simply PoS 1.0 - Application Settings (Key-Value)
-- ============================================================

CREATE TABLE IF NOT EXISTS app_settings (
    id BIGSERIAL PRIMARY KEY,
    setting_key VARCHAR(100) NOT NULL UNIQUE,
    setting_value TEXT,
    setting_type VARCHAR(30) NOT NULL DEFAULT 'STRING', -- STRING, BOOLEAN, INTEGER, FILE
    setting_group VARCHAR(50) NOT NULL DEFAULT 'GENERAL', -- GENERAL, LOGIN, SIDEBAR, BRANDING
    label VARCHAR(150) NOT NULL,
    description TEXT,
    sort_order INT NOT NULL DEFAULT 0,
    is_public BOOLEAN NOT NULL DEFAULT FALSE,
    updated_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_settings_key ON app_settings (setting_key);

CREATE INDEX idx_settings_group ON app_settings (setting_group);

-- ============ SEED DEFAULT VALUES ============

-- ============ LOGIN PAGE ============
INSERT INTO
    app_settings (
        setting_key,
        setting_value,
        setting_type,
        setting_group,
        label,
        description,
        sort_order,
        is_public,
        created_at,
        updated_at
    )
VALUES (
        'login.head.title',
        'Login – Simply PoS',
        'STRING',
        'LOGIN',
        'Title Head Login',
        'Judul tab browser di halaman login',
        10,
        TRUE,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        'login.logo.path',
        NULL,
        'FILE',
        'LOGIN',
        'Logo Login',
        'Logo yang tampil di halaman login (PNG/JPG/SVG, maks 500KB)',
        20,
        TRUE,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        'login.logo.text',
        'PG',
        'STRING',
        'LOGIN',
        'Logo Text (Fallback)',
        'Teks yang tampil jika logo tidak di-upload',
        30,
        TRUE,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        'login.title',
        'Simply PoS',
        'STRING',
        'LOGIN',
        'Judul Login',
        'Judul utama di halaman login',
        40,
        TRUE,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        'login.subtitle',
        'Business Management System',
        'STRING',
        'LOGIN',
        'Subtitle Login',
        'Sub-judul di bawah judul login',
        50,
        TRUE,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        'login.footer',
        '© 2026 Simply PoS v1.0',
        'STRING',
        'LOGIN',
        'Footer Login',
        'Teks footer di halaman login',
        60,
        TRUE,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        'login.background.color',
        '#0078D4',
        'STRING',
        'LOGIN',
        'Warna Background Login',
        'Warna background halaman login (hex)',
        70,
        TRUE,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    )
ON CONFLICT (setting_key) DO NOTHING;

-- ============ SIDEBAR ============
INSERT INTO
    app_settings (
        setting_key,
        setting_value,
        setting_type,
        setting_group,
        label,
        description,
        sort_order,
        is_public,
        created_at,
        updated_at
    )
VALUES (
        'sidebar.brand.logo.path',
        NULL,
        'FILE',
        'SIDEBAR',
        'Logo Sidebar',
        'Logo kecil di sidebar (PNG/SVG, maks 500KB)',
        10,
        TRUE,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        'sidebar.brand.text',
        'PG',
        'STRING',
        'SIDEBAR',
        'Brand Text (Fallback)',
        'Teks fallback jika logo sidebar tidak di-upload',
        20,
        TRUE,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        'sidebar.brand.name',
        'Simply PoS',
        'STRING',
        'SIDEBAR',
        'Nama Brand',
        'Nama brand yang tampil di sidebar',
        30,
        TRUE,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        'sidebar.show.brand.name',
        'true',
        'BOOLEAN',
        'SIDEBAR',
        'Tampilkan Nama Brand',
        'Tampilkan nama brand di sebelah logo',
        40,
        TRUE,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    )
ON CONFLICT (setting_key) DO NOTHING;

-- ============ GENERAL ============
INSERT INTO
    app_settings (
        setting_key,
        setting_value,
        setting_type,
        setting_group,
        label,
        description,
        sort_order,
        is_public,
        created_at,
        updated_at
    )
VALUES (
        'app.name',
        'Simply PoS',
        'STRING',
        'GENERAL',
        'Nama Aplikasi',
        'Nama aplikasi untuk tab browser',
        10,
        TRUE,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        'app.tagline',
        'Business Management System',
        'STRING',
        'GENERAL',
        'Tagline',
        'Tagline aplikasi',
        20,
        TRUE,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        'app.footer',
        '© 2026 Simply PoS v1.0',
        'STRING',
        'GENERAL',
        'Footer',
        'Footer aplikasi',
        30,
        TRUE,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    )
ON CONFLICT (setting_key) DO NOTHING;