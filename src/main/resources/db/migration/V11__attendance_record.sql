-- =========================================================
-- V11: Monthly timesheet records (one row per employee per day).
-- Source = IMPORT (uploaded Excel/CSV) or QR (rolled up from QR scans).
-- =========================================================

CREATE TABLE attendance_record (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    employee_name VARCHAR(255) NOT NULL,
    work_date     DATE         NOT NULL,
    check_in      TIME         NULL,
    check_out     TIME         NULL,
    source        VARCHAR(20)  NOT NULL DEFAULT 'IMPORT',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_att_rec UNIQUE (employee_name, work_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_att_rec_date ON attendance_record (work_date);
