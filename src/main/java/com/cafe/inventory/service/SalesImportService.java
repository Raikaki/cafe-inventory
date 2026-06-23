package com.cafe.inventory.service;

import com.cafe.inventory.dto.SalesDtos.*;
import com.cafe.inventory.exception.BusinessException;
import com.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Imports sales from Excel (.xlsx) or CSV.
 * Expected columns (with or without header): Sale Date | Product Code | Quantity
 * Date format: yyyy-MM-dd
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SalesImportService {

    private final SalesService salesService;

    public SalesResult importFile(MultipartFile file, String user) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Uploaded file is empty");
        }
        String name = Optional.ofNullable(file.getOriginalFilename()).orElse("").toLowerCase();
        List<Row3> rows;
        try (InputStream in = file.getInputStream()) {
            if (name.endsWith(".xlsx")) {
                rows = parseExcel(in);
            } else if (name.endsWith(".csv")) {
                rows = parseCsv(in);
            } else {
                throw new BusinessException("Unsupported file type. Use .xlsx or .csv");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("Failed to read file: " + e.getMessage());
        }

        if (rows.isEmpty()) {
            throw new BusinessException("No data rows found in file");
        }

        // group by sale date
        Map<LocalDate, List<SaleLine>> byDate = new LinkedHashMap<>();
        for (Row3 r : rows) {
            byDate.computeIfAbsent(r.date, d -> new ArrayList<>())
                    .add(new SaleLine(r.productCode, r.quantity));
        }

        List<ConsumedMaterial> allConsumption = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        String batchNo = null;
        int lineCount = 0;
        for (Map.Entry<LocalDate, List<SaleLine>> e : byDate.entrySet()) {
            SalesResult r = salesService.recordSales(new SalesRequest(e.getKey(), e.getValue()), user);
            if (batchNo == null) batchNo = r.batchNo();
            allConsumption.addAll(r.consumption());
            warnings.addAll(r.warnings());
            lineCount += r.salesLines();
        }
        log.info("Sales import by {}: {} rows across {} dates", user, lineCount, byDate.size());
        return new SalesResult(batchNo, rows.get(0).date, lineCount, allConsumption, warnings);
    }

    private List<Row3> parseExcel(InputStream in) throws Exception {
        List<Row3> result = new ArrayList<>();
        try (Workbook wb = new XSSFWorkbook(in)) {
            Sheet sheet = wb.getSheetAt(0);
            DataFormatter fmt = new DataFormatter();
            boolean first = true;
            for (Row row : sheet) {
                if (row == null) continue;
                String c0 = fmt.formatCellValue(row.getCell(0)).trim();
                String c1 = fmt.formatCellValue(row.getCell(1)).trim();
                String c2 = fmt.formatCellValue(row.getCell(2)).trim();
                if (first && isHeader(c0, c2)) { first = false; continue; }
                first = false;
                if (c0.isEmpty() && c1.isEmpty()) continue;
                result.add(toRow(c0, c1, c2));
            }
        }
        return result;
    }

    private List<Row3> parseCsv(InputStream in) throws Exception {
        List<Row3> result = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new InputStreamReader(in))) {
            String[] line;
            boolean first = true;
            while ((line = reader.readNext()) != null) {
                if (line.length < 3) continue;
                String c0 = line[0].trim(), c1 = line[1].trim(), c2 = line[2].trim();
                if (first && isHeader(c0, c2)) { first = false; continue; }
                first = false;
                if (c0.isEmpty() && c1.isEmpty()) continue;
                result.add(toRow(c0, c1, c2));
            }
        }
        return result;
    }

    private boolean isHeader(String c0, String c2) {
        String a = c0.toLowerCase();
        return a.contains("date") || a.contains("ngay");
    }

    private Row3 toRow(String dateStr, String code, String qtyStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr);
            BigDecimal qty = new BigDecimal(qtyStr);
            return new Row3(date, code, qty);
        } catch (Exception e) {
            throw new BusinessException("Invalid row [" + dateStr + ", " + code + ", " + qtyStr
                    + "] - date must be yyyy-MM-dd and quantity numeric");
        }
    }

    private record Row3(LocalDate date, String productCode, BigDecimal quantity) {}
}
