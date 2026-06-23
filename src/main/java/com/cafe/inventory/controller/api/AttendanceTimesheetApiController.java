package com.cafe.inventory.controller.api;

import com.cafe.inventory.entity.AttendanceRecord;
import com.cafe.inventory.service.AttendanceTimesheetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Tag(name = "Attendance Timesheet")
@RestController
@RequestMapping("/api/attendance/timesheet")
@RequiredArgsConstructor
public class AttendanceTimesheetApiController {

    private final AttendanceTimesheetService timesheetService;

    @Operation(summary = "Monthly timesheet records (employee x day)")
    @GetMapping
    public List<AttendanceRecord> month(@RequestParam int year, @RequestParam int month) {
        return timesheetService.getMonth(year, month);
    }

    @Operation(summary = "Import timesheet from Excel/CSV: Name | Date | CheckIn | CheckOut")
    @PostMapping(value = "/import", consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF')")
    public Map<String, Object> importFile(@RequestParam("file") MultipartFile file) {
        int count = timesheetService.importFile(file);
        return Map.of("imported", count);
    }

    @Operation(summary = "Roll up QR scans of the month into timesheet records")
    @PostMapping("/sync")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF')")
    public Map<String, Object> sync(@RequestParam int year, @RequestParam int month) {
        int count = timesheetService.syncFromQr(year, month);
        return Map.of("synced", count);
    }
}
