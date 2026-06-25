package com.cafe.inventory.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class AttendanceDtos {

    public record TokenResponse(LocalDate date, String token, String checkinPath) {}

    /** One timesheet cell: an employee's check-in/out on a day. */
    public record TimesheetRow(
            String employeeName,
            LocalDate workDate,
            LocalTime checkIn,
            LocalTime checkOut,
            String source            // QR or IMPORT
    ) {}

    public record LocationSetting(
            BigDecimal latitude,
            BigDecimal longitude,
            Integer radiusMeters,
            Boolean activeFlag
    ) {}

    public record CheckinRequest(
            @NotBlank String token,
            @NotBlank String employeeName,
            BigDecimal latitude,
            BigDecimal longitude
    ) {}

    public record CheckinResponse(
            String employeeName,
            String checkType,        // VAO or RA
            String scanTime,
            String ipAddress,
            BigDecimal latitude,
            BigDecimal longitude
    ) {}
}
