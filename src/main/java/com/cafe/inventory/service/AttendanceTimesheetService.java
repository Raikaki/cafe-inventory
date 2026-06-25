package com.cafe.inventory.service;

import com.cafe.inventory.dto.AttendanceDtos.TimesheetRow;
import com.cafe.inventory.entity.AttendanceLog;
import com.cafe.inventory.entity.AttendanceRecord;
import com.cafe.inventory.exception.BusinessException;
import com.cafe.inventory.repository.AttendanceLogRepository;
import com.cafe.inventory.repository.AttendanceRecordRepository;
import com.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Monthly timesheet: import attendance from Excel/CSV, roll up QR scans, and
 * read records for a month (rendered as an employee × day matrix on the UI).
 *
 * Import columns (with or without header): Tên nhân viên | Ngày | Giờ vào | Giờ ra
 *   Ngày: yyyy-MM-dd or dd/MM/yyyy ; Giờ: HH:mm or HH:mm:ss (giờ ra optional)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceTimesheetService {

    private final AttendanceRecordRepository recordRepository;
    private final AttendanceLogRepository logRepository;

    private static final DateTimeFormatter[] DATE_FMTS = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("d/M/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
    };

    @Transactional(readOnly = true)
    public List<AttendanceRecord> getMonth(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        return recordRepository.findByWorkDateBetweenOrderByEmployeeNameAscWorkDateAsc(ym.atDay(1), ym.atEndOfMonth());
    }

    /**
     * Build the timesheet for a date range directly from the daily QR check-ins
     * (min = giờ vào, max = giờ ra), supplemented by any imported records.
     * QR data wins on conflict.
     */
    @Transactional(readOnly = true)
    public List<TimesheetRow> getRange(LocalDate from, LocalDate to) {
        Map<String, TimesheetRow> map = new LinkedHashMap<>();

        // imported records first (base)
        for (AttendanceRecord r : recordRepository.findByWorkDateBetweenOrderByEmployeeNameAscWorkDateAsc(from, to)) {
            String key = r.getEmployeeName().trim().toLowerCase() + "|" + r.getWorkDate();
            map.put(key, new TimesheetRow(r.getEmployeeName(), r.getWorkDate(), r.getCheckIn(), r.getCheckOut(), "IMPORT"));
        }

        // daily QR scans override (this is the live source of truth)
        Map<String, List<AttendanceLog>> grouped = logRepository.findByQrDateBetween(from, to).stream()
                .collect(Collectors.groupingBy(l -> l.getEmployeeName().trim().toLowerCase() + "|" + l.getQrDate()));
        for (List<AttendanceLog> g : grouped.values()) {
            g.sort(Comparator.comparing(AttendanceLog::getScanTime));
            AttendanceLog first = g.get(0);
            AttendanceLog last = g.get(g.size() - 1);
            String key = first.getEmployeeName().trim().toLowerCase() + "|" + first.getQrDate();
            map.put(key, new TimesheetRow(
                    first.getEmployeeName(), first.getQrDate(),
                    first.getScanTime().toLocalTime(),
                    g.size() >= 2 ? last.getScanTime().toLocalTime() : null,
                    "QR"));
        }

        List<TimesheetRow> rows = new ArrayList<>(map.values());
        rows.sort(Comparator.comparing((TimesheetRow r) -> r.employeeName().toLowerCase())
                .thenComparing(TimesheetRow::workDate));
        return rows;
    }

    @Transactional
    public int importFile(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new BusinessException("File trống");
        String name = Optional.ofNullable(file.getOriginalFilename()).orElse("").toLowerCase();
        List<String[]> rows;
        try (InputStream in = file.getInputStream()) {
            if (name.endsWith(".xlsx")) rows = parseExcel(in);
            else if (name.endsWith(".csv")) rows = parseCsv(in);
            else throw new BusinessException("Chỉ hỗ trợ .xlsx hoặc .csv (ảnh không đọc được tự động)");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("Không đọc được file: " + e.getMessage());
        }

        int count = 0;
        for (String[] r : rows) {
            String empName = r[0].trim();
            if (empName.isEmpty()) continue;
            LocalDate date = parseDate(r[1].trim());
            if (date == null) continue;
            LocalTime in = parseTime(r.length > 2 ? r[2].trim() : "");
            LocalTime out = parseTime(r.length > 3 ? r[3].trim() : "");
            upsert(empName, date, in, out, "IMPORT");
            count++;
        }
        if (count == 0) throw new BusinessException("Không có dòng dữ liệu hợp lệ");
        log.info("Timesheet import: {} rows", count);
        return count;
    }

    /** Roll up QR scans for the month into timesheet records (min = giờ vào, max = giờ ra). */
    @Transactional
    public int syncFromQr(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        List<AttendanceLog> logs = logRepository.findByQrDateBetween(ym.atDay(1), ym.atEndOfMonth());

        Map<String, List<AttendanceLog>> grouped = logs.stream()
                .collect(Collectors.groupingBy(l -> l.getEmployeeName().trim().toLowerCase() + "|" + l.getQrDate()));

        int count = 0;
        for (List<AttendanceLog> g : grouped.values()) {
            g.sort(Comparator.comparing(AttendanceLog::getScanTime));
            AttendanceLog first = g.get(0);
            AttendanceLog last = g.get(g.size() - 1);
            LocalTime in = first.getScanTime().toLocalTime();
            LocalTime out = g.size() >= 2 ? last.getScanTime().toLocalTime() : null;
            upsert(first.getEmployeeName().trim(), first.getQrDate(), in, out, "QR");
            count++;
        }
        log.info("Timesheet sync from QR for {}-{}: {} day-records", year, month, count);
        return count;
    }

    private void upsert(String name, LocalDate date, LocalTime in, LocalTime out, String source) {
        AttendanceRecord rec = recordRepository.findByEmployeeNameAndWorkDate(name, date)
                .orElseGet(AttendanceRecord::new);
        rec.setEmployeeName(name);
        rec.setWorkDate(date);
        if (in != null) rec.setCheckIn(in);
        if (out != null) rec.setCheckOut(out);
        rec.setSource(source);
        recordRepository.save(rec);
    }

    // ---------- parsing ----------
    private List<String[]> parseExcel(InputStream in) throws Exception {
        List<String[]> result = new ArrayList<>();
        try (Workbook wb = new XSSFWorkbook(in)) {
            Sheet sheet = wb.getSheetAt(0);
            DataFormatter fmt = new DataFormatter();
            boolean first = true;
            for (Row row : sheet) {
                if (row == null) continue;
                String c0 = fmt.formatCellValue(row.getCell(0)).trim();
                String c1 = fmt.formatCellValue(row.getCell(1)).trim();
                String c2 = fmt.formatCellValue(row.getCell(2)).trim();
                String c3 = fmt.formatCellValue(row.getCell(3)).trim();
                if (first && isHeader(c0, c1)) { first = false; continue; }
                first = false;
                if (c0.isEmpty() && c1.isEmpty()) continue;
                result.add(new String[]{c0, c1, c2, c3});
            }
        }
        return result;
    }

    private List<String[]> parseCsv(InputStream in) throws Exception {
        List<String[]> result = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new InputStreamReader(in))) {
            String[] line;
            boolean first = true;
            while ((line = reader.readNext()) != null) {
                if (line.length < 2) continue;
                String c0 = line[0].trim();
                if (first && isHeader(c0, line.length > 1 ? line[1] : "")) { first = false; continue; }
                first = false;
                if (c0.isEmpty()) continue;
                result.add(new String[]{c0,
                        line.length > 1 ? line[1].trim() : "",
                        line.length > 2 ? line[2].trim() : "",
                        line.length > 3 ? line[3].trim() : ""});
            }
        }
        return result;
    }

    private boolean isHeader(String c0, String c1) {
        String a = (c0 + " " + c1).toLowerCase();
        return a.contains("tên") || a.contains("ten") || a.contains("name")
                || a.contains("ngày") || a.contains("ngay") || a.contains("date");
    }

    private LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) return null;
        for (DateTimeFormatter f : DATE_FMTS) {
            try { return LocalDate.parse(s, f); } catch (Exception ignored) { }
        }
        return null;
    }

    private LocalTime parseTime(String s) {
        if (s == null || s.isBlank()) return null;
        String t = s.trim();
        try {
            String[] p = t.split(":");
            int h = Integer.parseInt(p[0].trim());
            int m = p.length > 1 ? Integer.parseInt(p[1].trim()) : 0;
            int sec = p.length > 2 ? Integer.parseInt(p[2].trim().split("\\.")[0]) : 0;
            if (h < 0 || h > 23 || m < 0 || m > 59) return null;
            return LocalTime.of(h, m, Math.min(sec, 59));
        } catch (Exception e) {
            return null;
        }
    }
}
