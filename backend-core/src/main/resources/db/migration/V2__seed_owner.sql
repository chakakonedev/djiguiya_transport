-- Default owner account (password: password) — superseded by V3 for production identity
-- BCrypt hash for "password"
INSERT INTO users (id, email, password_hash, full_name, role_code, active)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'owner@djiguiya.ml',
    '$2b$10$rzwZpDaQJv0vBIIZqHmYnOg8jqqo8il8wlMA1GaXMRiE.RcPsk/Fa',
    'Propriétaire',
    'owner',
    1
);

INSERT INTO staff (id, full_name, staff_type, status, user_id)
VALUES (
    '00000000-0000-0000-0000-000000000101',
    'Propriétaire',
    'owner',
    'active',
    '00000000-0000-0000-0000-000000000001'
);

-- Company growth reserve default 20%
INSERT INTO profit_share_rules (id, beneficiary_staff_id, rule_kind, rate_percent, enabled, effective_from, notes)
VALUES (
    '00000000-0000-0000-0000-000000000201',
    NULL,
    'company_growth_reserve_pct',
    20.0000,
    1,
    '2025-08-01',
    'Part évolution entreprise (défaut CDC)'
);
