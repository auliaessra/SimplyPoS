-- ============================================================
-- Simply PoS 1.0 - Favicon Setting
-- ============================================================

INSERT INTO
    app_settings (
        setting_key,
        setting_value,
        setting_type,
        setting_group,
        label,
        description,
        sort_order,
        is_public
    )
VALUES (
        'app.favicon.path',
        NULL,
        'FILE',
        'GENERAL',
        'Favicon',
        'Ikon yang tampil di tab browser (ICO/PNG, 32x32 atau 64x64, maks 100KB)',
        5,
        TRUE
    )
ON CONFLICT (setting_key) DO NOTHING;