package com.framework.utils.data;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Excel data utility using Apache POI.
 * Supports .xlsx files for data-driven test scenarios.
 * Reads rows as Map<columnHeader, value> for clean step binding.
 */
public final class ExcelUtils {

    private static final Logger log = LogManager.getLogger(ExcelUtils.class);

    private ExcelUtils() {}

    /**
     * Read all rows from a sheet as a List of Maps.
     * First row is treated as header.
     *
     * Usage:
     *   List<Map<String,String>> data = ExcelUtils.readSheet("test-data/excel/login.xlsx", "LoginData");
     *   String username = data.get(0).get("username");
     */
    public static List<Map<String, String>> readSheet(String filePath, String sheetName) {
        List<Map<String, String>> records = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new RuntimeException("Sheet not found: [" + sheetName + "] in file: " + filePath);
            }

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                log.warn("Empty sheet: {}", sheetName);
                return records;
            }

            List<String> headers = new ArrayList<>();
            headerRow.forEach(cell -> headers.add(getCellValue(cell)));

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;

                Map<String, String> record = new LinkedHashMap<>();
                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = row.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    record.put(headers.get(j), getCellValue(cell));
                }
                records.add(record);
            }

            log.info("Read {} records from sheet [{}] in [{}]", records.size(), sheetName, filePath);

        } catch (IOException e) {
            throw new RuntimeException("Failed to read Excel file: " + filePath, e);
        }

        return records;
    }

    /**
     * Read a specific row by index (0-based, excluding header).
     */
    public static Map<String, String> readRow(String filePath, String sheetName, int rowIndex) {
        List<Map<String, String>> allRows = readSheet(filePath, sheetName);
        if (rowIndex >= allRows.size()) {
            throw new IndexOutOfBoundsException("Row index " + rowIndex + " out of bounds. Total rows: " + allRows.size());
        }
        return allRows.get(rowIndex);
    }

    /**
     * Read rows where a specific column matches a value.
     * Usage: readRowsWhere("data.xlsx", "Users", "role", "admin")
     */
    public static List<Map<String, String>> readRowsWhere(String filePath, String sheetName,
                                                          String columnName, String value) {
        return readSheet(filePath, sheetName).stream()
            .filter(row -> value.equalsIgnoreCase(row.get(columnName)))
            .toList();
    }

    /**
     * Convert Excel data to Object[][] for TestNG @DataProvider.
     */
    public static Object[][] toDataProvider(String filePath, String sheetName) {
        List<Map<String, String>> records = readSheet(filePath, sheetName);
        Object[][] data = new Object[records.size()][1];
        for (int i = 0; i < records.size(); i++) {
            data[i][0] = records.get(i);
        }
        return data;
    }

    // ── Private Helpers ──────────────────────────────────────────────────

    private static String getCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue().trim();
            case NUMERIC -> DateUtil.isCellDateFormatted(cell)
                ? cell.getLocalDateTimeCellValue().toString()
                : formatNumeric(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCachedFormulaResultType() == CellType.NUMERIC
                ? formatNumeric(cell.getNumericCellValue())
                : cell.getStringCellValue().trim();
            default      -> "";
        };
    }

    private static String formatNumeric(double value) {
        return value == Math.floor(value) ? String.valueOf((long) value) : String.valueOf(value);
    }

    private static boolean isRowEmpty(Row row) {
        for (Cell cell : row) {
            if (cell.getCellType() != CellType.BLANK) return false;
        }
        return true;
    }
}
