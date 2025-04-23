package com.example.medicinedistribution.Util;

import com.example.medicinedistribution.DTO.EmployeeDTO;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PayrollExportUtils {

    public static boolean exportPayrollToExcel(List<EmployeeDTO> employees,
                                              int month,
                                              int year,
                                              String filePath,
                                              TableView<EmployeeDTO> payrollTable) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Payroll Report");

            // Create styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle normalStyle = createNormalStyle(workbook);

            // Set column widths
            for (int i = 0; i < 25; i++) {
                sheet.setColumnWidth(i, 4000);
            }

            int rowNum = 0;

            // Create title
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("BẢNG LƯƠNG THÁNG " + month + "/" + year);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 24));

            // Add date information
            Row dateRow = sheet.createRow(rowNum++);
            Cell dateCell = dateRow.createCell(0);
            dateCell.setCellValue("Ngày xuất: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            dateCell.setCellStyle(normalStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 24));

            // Empty row
            rowNum++;

            // Create header row
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {
                "STT", "Họ và tên", "Phòng ban", "Chức vụ", "Lương cơ bản",
                "Ngày công thực tế", "Đi trễ", "Nghỉ không phép", "Phụ cấp chức vụ",
                "Phụ cấp ăn trưa", "Phụ cấp xăng xe", "Phụ cấp điện thoại", "Phụ cấp trách nhiệm",
                "Phụ cấp khác", "Tổng phụ cấp", "Thưởng", "Tổng thu nhập",
                "Lương tính BHXH", "BHXH", "BHYT", "BHTN", "Tổng BHXH", "Thuế TNCN",
                "Phạt", "Tổng khấu trừ", "Thực lãnh"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Create data rows
            for (int i = 0; i < employees.size(); i++) {
                EmployeeDTO employee = employees.get(i);
                int tableIndex = i;

                Row row = sheet.createRow(rowNum++);

                // Set regular data
                row.createCell(0).setCellValue(i + 1);
                row.createCell(1).setCellValue(employee.getFullName());

                // Get dynamic data from TableView columns
                setCellValue(row, 2, getColumnValue(payrollTable, "colDepartment", tableIndex));
                setCellValue(row, 3, getColumnValue(payrollTable, "colPosition", tableIndex));
                setCellValueAsCurrency(row, 4, getColumnValueAsBigDecimal(payrollTable, "colBaseSalary", tableIndex), currencyStyle);
                setCellValue(row, 5, getColumnValue(payrollTable, "colActualWorkDays", tableIndex));
                setCellValue(row, 6, getColumnValue(payrollTable, "colLateArrival", tableIndex));
                setCellValue(row, 7, getColumnValue(payrollTable, "colUnauthorizedLeave", tableIndex));
                setCellValueAsCurrency(row, 8, getColumnValueAsBigDecimal(payrollTable, "colPositionAllowance", tableIndex), currencyStyle);
                setCellValueAsCurrency(row, 9, getColumnValueAsBigDecimal(payrollTable, "colMealAllowance", tableIndex), currencyStyle);
                setCellValueAsCurrency(row, 10, getColumnValueAsBigDecimal(payrollTable, "colGasAllowance", tableIndex), currencyStyle);
                setCellValueAsCurrency(row, 11, getColumnValueAsBigDecimal(payrollTable, "colPhoneAllowance", tableIndex), currencyStyle);
                setCellValueAsCurrency(row, 12, getColumnValueAsBigDecimal(payrollTable, "colResponsibilityAllowance", tableIndex), currencyStyle);
                setCellValueAsCurrency(row, 13, getColumnValueAsBigDecimal(payrollTable, "colOtherAllowance", tableIndex), currencyStyle);
                setCellValueAsCurrency(row, 14, getColumnValueAsBigDecimal(payrollTable, "colTotalAllowance", tableIndex), currencyStyle);
                setCellValueAsCurrency(row, 15, getColumnValueAsBigDecimal(payrollTable, "colBonus", tableIndex), currencyStyle);
                setCellValueAsCurrency(row, 16, getColumnValueAsBigDecimal(payrollTable, "colTotalIncome", tableIndex), currencyStyle);
                setCellValueAsCurrency(row, 17, getColumnValueAsBigDecimal(payrollTable, "colInsuranceSalary", tableIndex), currencyStyle);
                setCellValueAsCurrency(row, 18, getColumnValueAsBigDecimal(payrollTable, "colSocialInsurance", tableIndex), currencyStyle);
                setCellValueAsCurrency(row, 19, getColumnValueAsBigDecimal(payrollTable, "colHealthInsurance", tableIndex), currencyStyle);
                setCellValueAsCurrency(row, 20, getColumnValueAsBigDecimal(payrollTable, "colUnemploymentInsurance", tableIndex), currencyStyle);
                setCellValueAsCurrency(row, 21, getColumnValueAsBigDecimal(payrollTable, "colTotalInsurance", tableIndex), currencyStyle);
                setCellValueAsCurrency(row, 22, getColumnValueAsBigDecimal(payrollTable, "colIncomeTax", tableIndex), currencyStyle);
                setCellValueAsCurrency(row, 23, getColumnValueAsBigDecimal(payrollTable, "colPenalty", tableIndex), currencyStyle);
                setCellValueAsCurrency(row, 24, getColumnValueAsBigDecimal(payrollTable, "colTotalDeduction", tableIndex), currencyStyle);
                setCellValueAsCurrency(row, 25, getColumnValueAsBigDecimal(payrollTable, "colNetSalary", tableIndex), currencyStyle);
            }

            // Add total row
            Row totalRow = sheet.createRow(rowNum++);
            Cell totalLabelCell = totalRow.createCell(0);
            totalLabelCell.setCellValue("Tổng cộng");
            totalLabelCell.setCellStyle(headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 3));

            // Set total formulas
            for (int i = 4; i <= 25; i++) {
                if (i != 5 && i != 6 && i != 7) { // Skip non-currency columns
                    Cell totalCell = totalRow.createCell(i);
                    totalCell.setCellFormula("SUM(" + CellReference.convertNumToColString(i) + "5:" +
                                           CellReference.convertNumToColString(i) + (rowNum-1) + ")");
                    totalCell.setCellStyle(currencyStyle);
                }
            }

            // Write to file
            try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
                workbook.write(outputStream);
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

// Modified helper method to get column value from TableView
@SuppressWarnings("unchecked")
private static <T> T getColumnValue(TableView<EmployeeDTO> table, String columnId, int rowIndex) {
    // First try by ID if not null
    for (TableColumn<EmployeeDTO, ?> column : table.getColumns()) {
        if (column.getId() != null && column.getId().equals(columnId)) {
            return (T) column.getCellData(rowIndex);
        }
    }

    // Search by column ID without 'col' prefix if needed
    String idWithoutPrefix = columnId.startsWith("col") ? columnId.substring(3) : columnId;

    // Search recursively through all columns and nested columns
    return findColumnValueByText(table.getColumns(), idWithoutPrefix, rowIndex);
}

@SuppressWarnings("unchecked")
private static <T> T findColumnValueByText(List<TableColumn<EmployeeDTO, ?>> columns, String columnText, int rowIndex) {
    for (TableColumn<EmployeeDTO, ?> column : columns) {
        // Check if this column has the ID or text we're looking for
        if (column.getId() != null && (column.getId().equals("col" + columnText) ||
                                     column.getId().equals(columnText))) {
            return (T) column.getCellData(rowIndex);
        }

        // Check if the column label contains the text we're looking for
        Label label = getLabelFromColumn(column);
        if (label != null && matchesColumnText(label.getText(), columnText)) {
            return (T) column.getCellData(rowIndex);
        }

        // Check nested columns recursively
        if (!column.getColumns().isEmpty()) {
            T result = findColumnValueByText(column.getColumns(), columnText, rowIndex);
            if (result != null) {
                return result;
            }
        }
    }

    return null;
}

private static Label getLabelFromColumn(TableColumn<EmployeeDTO, ?> column) {
    if (column.getGraphic() instanceof Label) {
        return (Label) column.getGraphic();
    }
    return null;
}

private static boolean matchesColumnText(String labelText, String searchText) {
    // Convert to lowercase and normalize for Vietnamese text
    String normalizedLabel = labelText.toLowerCase().replaceAll("\\s+", "");
    String normalizedSearch = searchText.toLowerCase().replaceAll("\\s+", "");

    // Match column text by partial content
    return normalizedLabel.contains(normalizedSearch) ||
           normalizedSearch.contains(normalizedLabel);
}

    // Helper method to get column value as BigDecimal
    private static BigDecimal getColumnValueAsBigDecimal(TableView<EmployeeDTO> table, String columnId, int rowIndex) {
        Object value = getColumnValue(table, columnId, rowIndex);
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        } else if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        return BigDecimal.ZERO;
    }

    // Helper method to set cell value
    private static void setCellValue(Row row, int colIndex, Object value) {
        Cell cell = row.createCell(colIndex);
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else {
            cell.setCellValue(value.toString());
        }
    }

    // Helper method to set cell value with currency format
    private static void setCellValueAsCurrency(Row row, int colIndex, BigDecimal value, CellStyle style) {
        Cell cell = row.createCell(colIndex);
        if (value != null) {
            cell.setCellValue(value.doubleValue());
        } else {
            cell.setCellValue(0.0);
        }
        cell.setCellStyle(style);
    }

    // Create header cell style
    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        return style;
    }

    // Create title cell style
    private static CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    // Create currency cell style
    private static CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        return style;
    }

    // Create normal cell style
    private static CellStyle createNormalStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        return style;
    }

    // Add this method to the ExportUtils class
    public static void exportPayroll(TableView<EmployeeDTO> payrollTable, int month, int year) {
        // Create a file chooser dialog
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Payroll Report");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        fileChooser.setInitialFileName("Payroll_" + month + "_" + year + ".xlsx");

        // Show save dialog
        File file = fileChooser.showSaveDialog(payrollTable.getScene().getWindow());

        if (file != null) {
            // Get the list of employees from the table
            List<EmployeeDTO> employees = payrollTable.getItems();

            // Export to Excel
            boolean success = exportPayrollToExcel(employees, month, year, file.getAbsolutePath(), payrollTable);

            // Show result notification
            if (success) {
                NotificationUtil.showSuccessNotification("Export Successful",
                    "Payroll data exported successfully to: " + file.getAbsolutePath());
            } else {
                NotificationUtil.showErrorNotification("Export Failed",
                    "Failed to export payroll data");
            }
        }
    }
}