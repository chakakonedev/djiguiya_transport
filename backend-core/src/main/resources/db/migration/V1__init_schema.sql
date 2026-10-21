-- Djiguiya Transport — initial schema (Phase 1 core)

CREATE TABLE users (
    id              VARCHAR(36)     NOT NULL PRIMARY KEY,
    email           VARCHAR(180) NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(180) NOT NULL,
    role_code       VARCHAR(40)  NOT NULL,
    active          TINYINT(1)   NOT NULL DEFAULT 1,
    created_at      TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE staff (
    id              VARCHAR(36)     NOT NULL PRIMARY KEY,
    full_name       VARCHAR(180) NOT NULL,
    staff_type      VARCHAR(40)  NOT NULL,
    phone           VARCHAR(40)  NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'active',
    monthly_salary  BIGINT       NULL,
    pay_day         INT          NULL,
    user_id         VARCHAR(36)     NULL,
    notes           TEXT         NULL,
    created_at      TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_staff_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE vehicles (
    id              VARCHAR(36)     NOT NULL PRIMARY KEY,
    name            VARCHAR(120) NOT NULL,
    plate           VARCHAR(60)  NULL,
    brand           VARCHAR(80)  NULL,
    model           VARCHAR(80)  NULL,
    year_built      INT          NULL,
    purchase_price  BIGINT       NULL,
    purchase_date   DATE         NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'active',
    notes           TEXT         NULL,
    created_at      TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE primary_assignments (
    id              VARCHAR(36)     NOT NULL PRIMARY KEY,
    vehicle_id      VARCHAR(36)     NOT NULL,
    driver_id       VARCHAR(36)     NOT NULL,
    start_date      DATE         NOT NULL,
    end_date        DATE         NULL,
    reason          VARCHAR(255) NULL,
    CONSTRAINT fk_pa_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles (id),
    CONSTRAINT fk_pa_driver FOREIGN KEY (driver_id) REFERENCES staff (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE vehicle_documents (
    id                      VARCHAR(36)     NOT NULL PRIMARY KEY,
    vehicle_id              VARCHAR(36)     NOT NULL,
    doc_type                VARCHAR(40)  NOT NULL,
    expires_on              DATE         NOT NULL,
    last_renewed_on         DATE         NULL,
    renewal_period_months   INT          NOT NULL DEFAULT 12,
    last_amount             BIGINT       NULL,
    notes                   TEXT         NULL,
    updated_at              TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    UNIQUE KEY uk_vehicle_doc (vehicle_id, doc_type),
    CONSTRAINT fk_vd_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE team_links (
    id              VARCHAR(36)     NOT NULL PRIMARY KEY,
    manager_id      VARCHAR(36)     NOT NULL,
    driver_id       VARCHAR(36)     NOT NULL,
    start_date      DATE         NULL,
    end_date        DATE         NULL,
    CONSTRAINT fk_tl_manager FOREIGN KEY (manager_id) REFERENCES staff (id),
    CONSTRAINT fk_tl_driver FOREIGN KEY (driver_id) REFERENCES staff (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE trips (
    id              VARCHAR(36)     NOT NULL PRIMARY KEY,
    vehicle_id      VARCHAR(36)     NOT NULL,
    departure_date  DATE         NOT NULL,
    return_date     DATE         NOT NULL,
    month_key       VARCHAR(7)      NOT NULL,
    driver_id       VARCHAR(36)     NOT NULL,
    is_replacement  TINYINT(1)   NOT NULL DEFAULT 0,
    replacement_reason VARCHAR(255) NULL,
    notes           TEXT         NULL,
    created_at      TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_at      TIMESTAMP(6) NULL,
    KEY idx_trips_month (month_key),
    KEY idx_trips_vehicle_month (vehicle_id, month_key),
    KEY idx_trips_driver_month (driver_id, month_key),
    CONSTRAINT fk_trips_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles (id),
    CONSTRAINT fk_trips_driver FOREIGN KEY (driver_id) REFERENCES staff (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE transactions (
    id                  VARCHAR(36)     NOT NULL PRIMARY KEY,
    type                VARCHAR(20)  NOT NULL,
    txn_date            DATE         NOT NULL,
    month_key           VARCHAR(7)      NOT NULL,
    amount              BIGINT       NOT NULL,
    vehicle_id          VARCHAR(36)     NULL,
    driver_id           VARCHAR(36)     NULL,
    trip_id             VARCHAR(36)     NULL,
    category_code       VARCHAR(40)  NULL,
    subcategory_code    VARCHAR(40)  NULL,
    label               VARCHAR(255) NOT NULL,
    payroll_month       VARCHAR(7)      NULL,
    payroll_kind        VARCHAR(20)  NULL,
    notes               TEXT         NULL,
    created_by          VARCHAR(36)     NULL,
    created_at          TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_at          TIMESTAMP(6) NULL,
    KEY idx_txn_month (month_key),
    KEY idx_txn_type_month (type, month_key),
    KEY idx_txn_vehicle_month (vehicle_id, month_key),
    KEY idx_txn_driver_month (driver_id, month_key),
    CONSTRAINT fk_txn_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles (id),
    CONSTRAINT fk_txn_driver FOREIGN KEY (driver_id) REFERENCES staff (id),
    CONSTRAINT fk_txn_trip FOREIGN KEY (trip_id) REFERENCES trips (id),
    CONSTRAINT fk_txn_user FOREIGN KEY (created_by) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE profit_share_rules (
    id                          VARCHAR(36)     NOT NULL PRIMARY KEY,
    beneficiary_staff_id        VARCHAR(36)     NULL,
    rule_kind                   VARCHAR(40)  NOT NULL,
    rate_percent                DECIMAL(8,4) NULL,
    salary_months_equivalent    DECIMAL(8,4) NULL,
    enabled                     TINYINT(1)   NOT NULL DEFAULT 1,
    effective_from              DATE         NOT NULL,
    effective_to                DATE         NULL,
    notes                       TEXT         NULL,
    CONSTRAINT fk_psr_staff FOREIGN KEY (beneficiary_staff_id) REFERENCES staff (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE refresh_tokens (
    id              VARCHAR(36)     NOT NULL PRIMARY KEY,
    user_id         VARCHAR(36)     NOT NULL,
    token_hash      VARCHAR(128) NOT NULL,
    expires_at      TIMESTAMP(6) NOT NULL,
    revoked         TINYINT(1)   NOT NULL DEFAULT 0,
    created_at      TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    UNIQUE KEY uk_refresh_hash (token_hash),
    CONSTRAINT fk_rt_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
