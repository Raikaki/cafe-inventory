-- =========================================================
-- V9: Add check_type (VAO = check-in, RA = check-out) to attendance_log.
-- First scan of the day = VAO, second = RA.
-- =========================================================

ALTER TABLE attendance_log
    ADD COLUMN check_type VARCHAR(10) NOT NULL DEFAULT 'VAO' AFTER employee_name;
