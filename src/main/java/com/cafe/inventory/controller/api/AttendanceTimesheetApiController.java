package com.cafe.inventory.controller.api;

import com.cafe.inventory.dto.AttendanceDtos.TimesheetRow;
import com.cafe.inventory.service.AttendanceTimesheetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Tag(name = "Attendance Timesheet")
@RestController
@RequestMapping("/api/attendance/timesheet")
@RequiredArgsConstructor
public class AttendanceTimesheetApiController {

    private final AttendanceTimesheetService timesheetService;

    @Operation(summary = "Timesheet for a date range, built from daily QR check-ins + imports")
    @GetMapping
    public List<TimesheetRow> range(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return timesheetService.getRange(from, to);
    }

    @Operation(summary = "Import timesheet from Excel/CSV: Name | Date | CheckIn | CheckOut")
    @PostMapping(value = "/import", consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF')")
    public Map<String, Object> importFile(@RequestParam("file") MultipartFile file) {
        int count = timesheetService.importFile(file);
        return Map.of("imported", count);
    }

    @Operation(summary = "Export the timesheet for a date range to Excel (.xlsx)")
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        byte[] data = timesheetService.exportExcel(from, to);
        String filename = "cham_cong_" + from + "_" + to + ".xlsx";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }

    @Operation(summary = "Roll up QR scans of the month into timesheet records")
    @PostMapping("/sync")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF')")
    public Map<String, Object> sync(@RequestParam int year, @RequestParam int month) {
        int count = timesheetService.syncFromQr(year, month);
        return Map.of("synced", count);
    }
}
