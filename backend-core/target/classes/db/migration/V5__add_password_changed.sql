-- Tracks whether the user replaced the bootstrap default password.
ALTER TABLE users
    ADD COLUMN password_changed TINYINT(1) NOT NULL DEFAULT 0;
