-- ============================================================
-- Simply PoS 1.0 - Master Units & Unit Conversions
-- ============================================================

-- ============ UNITS MASTER ============
CREATE TABLE IF NOT EXISTS units (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(30) NOT NULL DEFAULT 'VOLUME', -- VOLUME, WEIGHT, COUNT
    is_base BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    description TEXT,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_units_category ON units (category);

CREATE INDEX idx_units_active ON units (is_active);

-- ============ UNIT CONVERSIONS ============
CREATE TABLE IF NOT EXISTS unit_conversions (
    id BIGSERIAL PRIMARY KEY,
    from_unit_id BIGINT NOT NULL REFERENCES units (id) ON DELETE CASCADE,
    to_unit_id BIGINT NOT NULL REFERENCES units (id) ON DELETE CASCADE,
    factor NUMERIC(20, 8) NOT NULL, -- 1 from_unit = factor * to_unit
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_conv_from_to UNIQUE (from_unit_id, to_unit_id),
    CONSTRAINT chk_conv_different UNIQUE (from_unit_id, to_unit_id),
    CONSTRAINT chk_factor_positive CHECK (factor > 0)
);

CREATE INDEX idx_conv_from ON unit_conversions (from_unit_id);

CREATE INDEX idx_conv_to ON unit_conversions (to_unit_id);

-- ============ ADD unit_id TO ITEMS ============
ALTER TABLE items
ADD COLUMN IF NOT EXISTS unit_id BIGINT REFERENCES units (id);

CREATE INDEX IF NOT EXISTS idx_items_unit ON items (unit_id);

-- ============ DROP OLD CHECK CONSTRAINT (jika ada) ============
-- Hapus constraint lama yang mengunci unit = 'ML'
ALTER TABLE items DROP CONSTRAINT IF EXISTS chk_items_unit;

-- ============ SEED: BASE UNIT (ML) ============
INSERT INTO
    units (
        code,
        name,
        category,
        is_base,
        is_active,
        sort_order,
        created_at,
        updated_at
    )
VALUES (
        'ML',
        'Mililiter',
        'VOLUME',
        TRUE,
        TRUE,
        10,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        'LTR',
        'Liter',
        'VOLUME',
        FALSE,
        TRUE,
        20,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        'CL',
        'Centiliter',
        'VOLUME',
        FALSE,
        TRUE,
        25,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        'GAL',
        'Galon',
        'VOLUME',
        FALSE,
        TRUE,
        30,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        'BTL',
        'Botol',
        'COUNT',
        FALSE,
        TRUE,
        40,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        'PCS',
        'Pieces',
        'COUNT',
        FALSE,
        TRUE,
        50,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        'BOX',
        'Box',
        'COUNT',
        FALSE,
        TRUE,
        60,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        'PACK',
        'Pack',
        'COUNT',
        FALSE,
        TRUE,
        70,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        'KG',
        'Kilogram',
        'WEIGHT',
        FALSE,
        TRUE,
        80,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        'GR',
        'Gram',
        'WEIGHT',
        FALSE,
        TRUE,
        90,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    )
ON CONFLICT (code) DO NOTHING;

-- ============ SEED: CONTOH KONVERSI ============
-- 1 Liter = 1000 ML
INSERT INTO
    unit_conversions (
        from_unit_id,
        to_unit_id,
        factor,
        is_active,
        notes,
        created_at,
        updated_at
    )
SELECT u1.id, u2.id, 1000.0, TRUE, '1 Liter = 1000 ML',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
FROM units u1, units u2
WHERE
    u1.code = 'LTR'
    AND u2.code = 'ML'
ON CONFLICT (from_unit_id, to_unit_id) DO NOTHING;

-- 1 Centiliter = 10 ML
INSERT INTO
    unit_conversions (
        from_unit_id,
        to_unit_id,
        factor,
        is_active,
        notes,
        created_at,
        updated_at
    )
SELECT u1.id, u2.id, 10.0, TRUE, '1 Centiliter = 10 ML',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
FROM units u1, units u2
WHERE
    u1.code = 'CL'
    AND u2.code = 'ML'
ON CONFLICT (from_unit_id, to_unit_id) DO NOTHING;

-- 1 Galon = 3785 ML (US gallon)
INSERT INTO
    unit_conversions (
        from_unit_id,
        to_unit_id,
        factor,
        is_active,
        notes,
        created_at,
        updated_at
    )
SELECT u1.id, u2.id, 3785.0, TRUE, '1 Galon = 3785 ML',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
FROM units u1, units u2
WHERE
    u1.code = 'GAL'
    AND u2.code = 'ML'
ON CONFLICT (from_unit_id, to_unit_id) DO NOTHING;

-- 1 Botol = 100 ML (default; user bisa ubah)
INSERT INTO
    unit_conversions (
        from_unit_id,
        to_unit_id,
        factor,
        is_active,
        notes,
        created_at,
        updated_at
    )
SELECT u1.id, u2.id, 100.0, TRUE, '1 Botol = 100 ML (dapat disesuaikan)',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
FROM units u1, units u2
WHERE
    u1.code = 'BTL'
    AND u2.code = 'ML'
ON CONFLICT (from_unit_id, to_unit_id) DO NOTHING;

-- 1 KG = 1000 GR
INSERT INTO
    unit_conversions (
        from_unit_id,
        to_unit_id,
        factor,
        is_active,
        notes,
        created_at,
        updated_at
    )
SELECT u1.id, u2.id, 1000.0, TRUE, '1 Kilogram = 1000 Gram',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
FROM units u1, units u2
WHERE
    u1.code = 'KG'
    AND u2.code = 'GR'
ON CONFLICT (from_unit_id, to_unit_id) DO NOTHING;

-- ============ UPDATE: Set semua item existing ke ML ============
UPDATE items
SET
    unit_id = (
        SELECT id
        FROM units
        WHERE
            code = 'ML'
    )
WHERE
    unit_id IS NULL;

-- ============ SET DEFAULT ============
-- ALTER TABLE items
-- ALTER COLUMN unit_id
-- SET DEFAULT (
--     SELECT id
--     FROM units
--     WHERE
--         code = 'ML'
-- );

-- ============ DROP KOLOM unit LAMA (opsional) ============
-- Uncomment jika ingin migrasi penuh dan drop kolom lama
-- ALTER TABLE items DROP COLUMN IF EXISTS unit;