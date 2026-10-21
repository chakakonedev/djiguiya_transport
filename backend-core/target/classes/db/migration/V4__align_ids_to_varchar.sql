-- Align UUID / month_key columns with Hibernate String mapping (VARCHAR).
-- One MODIFY per statement for H2 MySQL-mode compatibility.
ALTER TABLE users MODIFY id VARCHAR(36) NOT NULL;

ALTER TABLE staff MODIFY id VARCHAR(36) NOT NULL;
ALTER TABLE staff MODIFY user_id VARCHAR(36) NULL;

ALTER TABLE vehicles MODIFY id VARCHAR(36) NOT NULL;

ALTER TABLE primary_assignments MODIFY id VARCHAR(36) NOT NULL;
ALTER TABLE primary_assignments MODIFY vehicle_id VARCHAR(36) NOT NULL;
ALTER TABLE primary_assignments MODIFY driver_id VARCHAR(36) NOT NULL;

ALTER TABLE vehicle_documents MODIFY id VARCHAR(36) NOT NULL;
ALTER TABLE vehicle_documents MODIFY vehicle_id VARCHAR(36) NOT NULL;

ALTER TABLE team_links MODIFY id VARCHAR(36) NOT NULL;
ALTER TABLE team_links MODIFY manager_id VARCHAR(36) NOT NULL;
ALTER TABLE team_links MODIFY driver_id VARCHAR(36) NOT NULL;

ALTER TABLE trips MODIFY id VARCHAR(36) NOT NULL;
ALTER TABLE trips MODIFY vehicle_id VARCHAR(36) NOT NULL;
ALTER TABLE trips MODIFY driver_id VARCHAR(36) NOT NULL;
ALTER TABLE trips MODIFY month_key VARCHAR(7) NOT NULL;

ALTER TABLE transactions MODIFY id VARCHAR(36) NOT NULL;
ALTER TABLE transactions MODIFY vehicle_id VARCHAR(36) NULL;
ALTER TABLE transactions MODIFY driver_id VARCHAR(36) NULL;
ALTER TABLE transactions MODIFY trip_id VARCHAR(36) NULL;
ALTER TABLE transactions MODIFY created_by VARCHAR(36) NULL;
ALTER TABLE transactions MODIFY month_key VARCHAR(7) NOT NULL;
ALTER TABLE transactions MODIFY payroll_month VARCHAR(7) NULL;

ALTER TABLE profit_share_rules MODIFY id VARCHAR(36) NOT NULL;
ALTER TABLE profit_share_rules MODIFY beneficiary_staff_id VARCHAR(36) NULL;

ALTER TABLE refresh_tokens MODIFY id VARCHAR(36) NOT NULL;
ALTER TABLE refresh_tokens MODIFY user_id VARCHAR(36) NOT NULL;
