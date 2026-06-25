package com.cafe.inventory.service;

import com.cafe.inventory.dto.GoodsReceiptDtos.*;
import com.cafe.inventory.entity.Material;
import com.cafe.inventory.exception.BusinessException;
import com.cafe.inventory.repository.MaterialRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Imports a goods receipt from Excel (.xlsx) or CSV.
 * Expected columns (with or without header): Material Code | Quantity | Unit Price
 * All rows become a single goods receipt dated today.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoodsReceiptImportService {

    private final GoodsReceiptService goodsReceiptService;
    private final MaterialRepository materialRepository;

    public ReceiptResponse importFile(MultipartFile file, String user) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Uploaded file is empty");
        }
        String name = Optional.ofNullable(file.getOriginalFilename()).orElse("").toLowerCase();
        List<String[]> rows;
        try (InputStream in = file.getInputStream()) {
            if (name.endsWith(".xlsx")) rows = parseExcel(in);
            else if (name.endsWith(".csv")) rows = parseCsv(in);
            else throw new BusinessException("Unsupported file type. Use .xlsx or .csv");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("Failed to read file: " + e.getMessage());
        }

        List<ReceiptLine> lines = new ArrayList<>();
        for (String[] r : rows) {
            String code = r[0].trim();
            Material m = materialRepository.findByMaterialCode(code)
                    .orElseThrow(() -> new BusinessException("Material code not found: " + code));
            try {
                BigDecimal qty = new BigDecimal(r[1].trim());
                // 3rd column = amount (thành tiền); unit price is derived later
                BigDecimal amount = r.length > 2 && !r[2].isBlank() ? new BigDecimal(r[2].trim()) : BigDecimal.ZERO;
                if (qty.compareTo(BigDecimal.ZERO) > 0) {
                    lines.add(new ReceiptLine(m.getId(), qty, amount));
                }
            } catch (NumberFormatException ex) {
                throw new BusinessException("Invalid number in row for material " + code);
            }
        }
        if (lines.isEmpty()) throw new BusinessException("No valid data rows found in file");

        log.info("Goods receipt import by {}: {} lines", user, lines.size());
        return goodsReceiptService.create(
                new ReceiptRequest(LocalDate.now(), null, "Imported from file", lines), user);
    }

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
                if (first && isHeader(c0)) { first = false; continue; }
                first = false;
                if (c0.isEmpty() && c1.isEmpty()) continue;
                result.add(new String[]{c0, c1, c2});
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
                if (first && isHeader(c0)) { first = false; continue; }
                first = false;
                if (c0.isEmpty()) continue;
                result.add(new String[]{c0, line.length > 1 ? line[1].trim() : "0", line.length > 2 ? line[2].trim() : "0"});
            }
        }
        return result;
    }

    private boolean isHeader(String c0) {
        String a = c0.toLowerCase();
        return a.contains("code") || a.contains("material") || a.contains("ma") || a.contains("mã");
    }
}
