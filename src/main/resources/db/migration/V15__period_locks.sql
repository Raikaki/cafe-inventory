-- =========================================================
-- V15: Period lock (khóa sổ kỳ). A locked (year, month) blocks creating or
-- editing dated documents within that period.
-- =========================================================

CREATE TABLE period_locks (
    id           BIGINT      NOT NULL AUTO_INCREMENT,
    period_year  INT         NOT NULL,
    period_month INT         NOT NULL,
    locked_by    VARCHAR(100),
    locked_at    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_period_lock UNIQUE (period_year, period_month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
