-- =========================================================
-- V8: QR attendance (chấm công)
-- A random daily token is generated; scanning records IP, time and GPS location.
-- =========================================================

CREATE TABLE attendance_qr (
    qr_id      BIGINT      NOT NULL AUTO_INCREMENT,
    qr_date    DATE        NOT NULL,
    token      VARCHAR(64) NOT NULL,
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (qr_id),
    CONSTRAINT uk_attendance_qr_date UNIQUE (qr_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE attendance_log (
    log_id        BIGINT        NOT NULL AUTO_INCREMENT,
    qr_date       DATE          NOT NULL,
    token         VARCHAR(64),
    employee_name VARCHAR(255)  NOT NULL,
    ip_address    VARCHAR(64),
    latitude      DECIMAL(10,7),
    longitude     DECIMAL(10,7),
    user_agent    VARCHAR(500),
    scan_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (log_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_attendance_log_date ON attendance_log (qr_date);
