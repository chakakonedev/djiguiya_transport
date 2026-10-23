-- Backfill Bus 1 identity from year-1 business facts (plate, purchase, first trip note).
UPDATE vehicles
SET plate = 'AA-496-ER',
    purchase_price = 25000000,
    purchase_date = '2025-07-01',
    notes = 'Year-1 primary bus (source=year1_import); purchased Jul 2025 for 25M FCFA; first trip 2025-07-26',
    updated_at = CURRENT_TIMESTAMP(6)
WHERE id = '00000000-0000-0000-0000-000000000301';
