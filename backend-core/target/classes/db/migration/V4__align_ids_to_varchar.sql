-- Align UUID / month_key columns with Hibernate String mapping (VARCHAR), like CCEJ.
ALTER TABLE users
    MODIFY id VARCHAR(36) NOT NULL;

ALTER TABLE staff
    MODIFY id VARCHAR(36) NOT NULL,
    MODIFY user_id VARCHAR(36) NULL;

ALTER TABLE vehicles
    MODIFY id VARCHAR(36) NOT NULL;

ALTER TABLE primary_assignments
    MODIFY id VARCHAR(36) NOT NULL,
    MODIFY vehicle_id VARCHAR(36) NOT NULL,
    MODIFY driver_id VARCHAR(36) NOT NULL;

ALTER TABLE vehicle_documents
    MODIFY id VARCHAR(36) NOT NULL,
    MODIFY vehicle_id VARCHAR(36) NOT NULL;

ALTER TABLE team_links
    MODIFY id VARCHAR(36) NOT NULL,
    MODIFY manager_id VARCHAR(36) NOT NULL,
    MODIFY driver_id VARCHAR(36) NOT NULL;

ALTER TABLE trips
    MODIFY id VARCHAR(36) NOT NULL,
    MODIFY vehicle_id VARCHAR(36) NOT NULL,
    MODIFY driver_id VARCHAR(36) NOT NULL,
    MODIFY month_key VARCHAR(7) NOT NULL;

ALTER TABLE transactions
    MODIFY id VARCHAR(36) NOT NULL,
    MODIFY vehicle_id VARCHAR(36) NULL,
    MODIFY driver_id VARCHAR(36) NULL,
    MODIFY trip_id VARCHAR(36) NULL,
    MODIFY created_by VARCHAR(36) NULL,
    MODIFY month_key VARCHAR(7) NOT NULL,
    MODIFY payroll_month VARCHAR(7) NULL;

ALTER TABLE profit_share_rules
    MODIFY id VARCHAR(36) NOT NULL,
    MODIFY beneficiary_staff_id VARCHAR(36) NULL;

ALTER TABLE refresh_tokens
    MODIFY id VARCHAR(36) NOT NULL,
    MODIFY user_id VARCHAR(36) NOT NULL;
