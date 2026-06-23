-- =========================================================
-- V10: Store (cafe) location for attendance geofencing.
-- When active_flag = 1, check-in is only allowed within radius_meters of the
-- configured coordinates. When 0 (default), check-in is allowed anywhere.
-- =========================================================

CREATE TABLE store_location (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    latitude      DECIMAL(10,7),
    longitude     DECIMAL(10,7),
    radius_meters INT           NOT NULL DEFAULT 200,
    active_flag   TINYINT       NOT NULL DEFAULT 0,
    updated_by    VARCHAR(100),
    updated_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- single configuration row (inactive by default -> check-in allowed anywhere)
INSERT INTO store_location (latitude, longitude, radius_meters, active_flag)
VALUES (NULL, NULL, 200, 0);
