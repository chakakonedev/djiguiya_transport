-- Sole director/admin identity: Chaka Koné (password: Djiguiya!Owner2026 — change in production)
-- Upsert so missing seed row is recreated instead of a silent no-op UPDATE
-- BCrypt hash for "Djiguiya!Owner2026"
INSERT INTO users (id, email, password_hash, full_name, role_code, active)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'chakakone93@yahoo.fr',
    '$2b$10$dMTTwkcKzxUCkzYu/dyLXu5xaWfTUH3/umpDfecAtW7dYEvTxu6V2',
    'Chaka Koné',
    'owner',
    1
)
ON DUPLICATE KEY UPDATE
    email = 'chakakone93@yahoo.fr',
    password_hash = '$2b$10$dMTTwkcKzxUCkzYu/dyLXu5xaWfTUH3/umpDfecAtW7dYEvTxu6V2',
    full_name = 'Chaka Koné',
    role_code = 'owner',
    active = 1;

INSERT INTO staff (id, full_name, staff_type, status, user_id)
VALUES (
    '00000000-0000-0000-0000-000000000101',
    'Chaka Koné',
    'owner',
    'active',
    '00000000-0000-0000-0000-000000000001'
)
ON DUPLICATE KEY UPDATE
    full_name = 'Chaka Koné',
    staff_type = 'owner',
    status = 'active',
    user_id = '00000000-0000-0000-0000-000000000001';
