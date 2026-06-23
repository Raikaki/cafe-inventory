package com.cafe.inventory.controller.api;

import com.cafe.inventory.dto.AttendanceDtos.*;
import com.cafe.inventory.entity.AttendanceLog;
import com.cafe.inventory.entity.AttendanceQr;
import com.cafe.inventory.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Attendance")
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceApiController {

    private final AttendanceService attendanceService;

    @Operation(summary = "Get today's QR token (generates once per day)")
    @GetMapping("/qr/today")
    public TokenResponse today() {
        AttendanceQr qr = attendanceService.getOrCreateTodayToken();
        return new TokenResponse(qr.getQrDate(), qr.getToken(), "/checkin?token=" + qr.getToken());
    }

    @Operation(summary = "Check-in by scanning the QR (public). Captures IP, time and GPS.")
    @PostMapping("/checkin")
    public CheckinResponse checkin(@Valid @RequestBody CheckinRequest req, HttpServletRequest request) {
        return attendanceService.checkin(req, clientIp(request), request.getHeader("User-Agent"));
    }

    @Operation(summary = "Attendance logs for a date")
    @GetMapping("/logs")
    public List<AttendanceLog> logs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return attendanceService.logs(date == null ? LocalDate.now() : date);
    }

    private String clientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim(); // first IP = original client (Render proxies)
        }
        return request.getRemoteAddr();
    }
}
