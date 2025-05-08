package com.example.medicinedistribution.Util;

import com.example.medicinedistribution.BUS.BUSFactory;
import com.example.medicinedistribution.BUS.Interface.EmployeeBUS;
import com.example.medicinedistribution.BUS.Interface.PayrollBUS;
import com.example.medicinedistribution.DAO.Interface.EmployeeDAO;
import com.example.medicinedistribution.DAO.Interface.PayrollDAO;
import com.example.medicinedistribution.DTO.EmployeeDTO;
import com.example.medicinedistribution.DTO.PayrollDTO;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.font.constants.StandardFonts;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
@Slf4j
public class PdfExportUtils {

    private static final Color HEADER_COLOR = new DeviceRgb(66, 133, 244);
    private static final Color LIGHT_BLUE = new DeviceRgb(231, 240, 254);
    private static NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private static BUSFactory busFactory;

    /**
     * Export payroll information for a specific employee to PDF
     * @param employeeId ID of the employee
     * @param month Month of payroll
     * @param year Year of payroll
     */
public static void exportEmployeePayrollToPdf(int employeeId, int month, int year, BUSFactory busFactory) {
    try {
        // Create directory structure if it doesn't exist
        String directoryPath = "./payroll/" + month + "_" + year;
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Define the file path directly
        File file = new File(directory, "Payroll_" + employeeId + "_" + month + "_" + year + ".pdf");

        // Get employee and payroll data
        PdfExportUtils.busFactory = busFactory;
        EmployeeBUS employeeBUS = busFactory.getEmployeeBUS();
        EmployeeDTO employee = employeeBUS.findById(employeeId);

        PayrollBUS payrollBUS = busFactory.getPayrollBUS();
        PayrollDTO payroll = payrollBUS.getPayrollByEmployeeId(employeeId, month, year);

        if (employee == null || payroll == null) {
            NotificationUtil.showErrorNotification("Export Error",
                    "Payroll data not found for employee ID " + employeeId + " in " + month + "/" + year);
            return;
        }

        // Create PDF document
        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(file));
        Document document = new Document(pdfDoc, PageSize.A4);
        document.setMargins(36, 36, 36, 36);

        try {
            // Load font that supports Vietnamese
            String fontPath = "./fonts/arial-unicode-ms.ttf";
            PdfFont font = PdfFontFactory.createFont(fontPath, PdfEncodings.IDENTITY_H,
                                                  PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED);
            document.setFont(font);
        } catch (IOException e) {
            log.error("Error loading font: {}", e.getMessage());
            // Fall back to default font if custom font fails to load
            document.setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA,
                             PdfEncodings.IDENTITY_H));
        }

        // Add document content
        addDocumentHeader(document, month, year);
        addEmployeeInfo(document, employee);
        addPayrollDetails(document, payroll);
        addSignatureSection(document);

        // Close document
        document.close();

        NotificationUtil.showSuccessNotification("Export Successful",
                "Payroll PDF exported successfully to: " + file.getAbsolutePath());
        // Automatically open the PDF file
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            } else {
                log.warn("Desktop not supported, cannot open PDF automatically");
            }
        } catch (IOException e) {
            log.error("Error opening PDF file: {}", e.getMessage());
        }
    } catch (Exception e) {
        log.error("Error exporting payroll to PDF: {}", e.getMessage());
        NotificationUtil.showErrorNotification("Export Failed",
                "Failed to export payroll data: " + e.getMessage());
    }
}

    private static void addDocumentHeader(Document document, int month, int year) {
        // Company information
        Paragraph company = new Paragraph("ABC PHARMACEUTICAL DISTRIBUTION COMPANY")
                .setFontSize(14)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
        document.add(company);

        // Document title
        Paragraph title = new Paragraph("SALARY SLIP FOR MONTH " + month + "/" + year)
                .setFontSize(16)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20);
        document.add(title);

        // Date
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        Paragraph date = new Paragraph("Export date: " + LocalDate.now().format(dtf))
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(10);
        document.add(date);
    }

    private static void addEmployeeInfo(Document document, EmployeeDTO employee) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginTop(20);

        table.addCell(createCell("Employee ID:", true, false));
        table.addCell(createCell(String.valueOf(employee.getEmployeeId()), false, false));

        table.addCell(createCell("Full name:", true, false));
        table.addCell(createCell(employee.getFullName(), false, false));

        table.addCell(createCell("Position:", true, false));
        table.addCell(createCell(PdfExportUtils.busFactory.getPositionBUS().findById(employee.getPositionId()).getPositionName(), false, false));

        document.add(table);

        // Add divider
        document.add(new Paragraph("")
                .setMarginTop(10)
                .setBorderBottom(new SolidBorder(ColorConstants.LIGHT_GRAY, 1)));
    }

    private static void addPayrollDetails(Document document, PayrollDTO payroll) {
        Paragraph header = new Paragraph("SALARY INFORMATION")
                .setFontSize(14)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(15);
        document.add(header);

        // Salary information
        Table salaryTable = new Table(UnitValue.createPercentArray(new float[]{60, 40}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginTop(10);

        // Income section
        addSectionHeader(salaryTable, "1. INCOME", 2);

        addRow(salaryTable, "Basic salary:", formatCurrency(payroll.getBase_salary()));
        addRow(salaryTable, "Actual working days:", String.valueOf(payroll.getActual_working_days()));
        addRow(salaryTable, "Number of late days:", String.valueOf(payroll.getLate_days()));
        addRow(salaryTable, "Number of unauthorized leave days:", String.valueOf(payroll.getLeave_days()));

        // Allowances
        addRow(salaryTable, "Position allowance:", formatCurrency(payroll.getPosition_allowance()));
        addRow(salaryTable, "Meal allowance:", formatCurrency(payroll.getMeal_allowance()));
        addRow(salaryTable, "Gas allowance:", formatCurrency(payroll.getGas_allowance()));
        addRow(salaryTable, "Phone allowance:", formatCurrency(payroll.getPhone_allowance()));
        addRow(salaryTable, "Responsibility allowance:", formatCurrency(payroll.getResponsibility_allowance()));
        addRow(salaryTable, "Other allowance:", formatCurrency(payroll.getOther_allowance()));
        addRow(salaryTable, "Total allowance:", formatCurrency(payroll.getTotal_allowance()));

        // Bonuses
        addRow(salaryTable, "Bonus:", formatCurrency(payroll.getBonus_total()));

        // Total income
        addRow(salaryTable, "Total income:", formatCurrency(payroll.getTaxable_income()), true);

        // Deduction section
        addSectionHeader(salaryTable, "2. DEDUCTIONS", 2);

        // Insurance
        addRow(salaryTable, "Insurance calculation salary:", formatCurrency(payroll.getSocial_insurance_salary()));
        addRow(salaryTable, "Social insurance (8%):", formatCurrency(payroll.getInsurance_social()));
        addRow(salaryTable, "Health insurance (1.5%):", formatCurrency(payroll.getInsurance_health()));
        addRow(salaryTable, "Accident insurance (1%):", formatCurrency(payroll.getInsurance_accident()));
        addRow(salaryTable, "Total insurance:", formatCurrency(payroll.getTotal_insurance()));

        // Other deductions
        addRow(salaryTable, "Personal income tax:", formatCurrency(payroll.getIncome_tax()));
        addRow(salaryTable, "Penalties:", formatCurrency(payroll.getPenalty_amount()));

        // Total deductions
        addRow(salaryTable, "Total deductions:", formatCurrency(payroll.getDeductible_income()), true);

        // Net salary section
        addSectionHeader(salaryTable, "3. NET SALARY", 2);
        Cell netSalaryLabelCell = createCell("Net salary:", true, true);
        salaryTable.addCell(netSalaryLabelCell);

        Cell netSalaryValueCell = createCell(formatCurrency(payroll.getNet_income()), false, true);
        netSalaryValueCell.setBackgroundColor(LIGHT_BLUE);
        netSalaryValueCell.setFontColor(ColorConstants.RED);
        netSalaryValueCell.setBold();
        netSalaryValueCell.setFontSize(12);
        salaryTable.addCell(netSalaryValueCell);

        document.add(salaryTable);
    }

    private static void addSignatureSection(Document document) {
        document.add(new Paragraph("")
                .setMarginTop(30));

        Table signTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .setWidth(UnitValue.createPercentValue(100));

        Cell employeeCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .add(new Paragraph("RECIPIENT").setBold().setTextAlignment(TextAlignment.CENTER))
                .add(new Paragraph("(Sign, full name)").setItalic().setTextAlignment(TextAlignment.CENTER).setFontSize(9));

        Cell managerCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .add(new Paragraph("PREPARED BY").setBold().setTextAlignment(TextAlignment.CENTER))
                .add(new Paragraph("(Sign, full name)").setItalic().setTextAlignment(TextAlignment.CENTER).setFontSize(9));

        signTable.addCell(employeeCell);
        signTable.addCell(managerCell);

        document.add(signTable);
    }

    private static Cell createCell(String content, boolean isHeader, boolean isHighlighted) {
        Cell cell = new Cell()
                .setPadding(5)
                .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f));

        if (isHeader) {
            cell.setBold();
        }

        if (isHighlighted) {
            cell.setBackgroundColor(HEADER_COLOR);
            cell.setFontColor(ColorConstants.WHITE);
        }

        cell.add(new Paragraph(content));
        return cell;
    }

    private static void addSectionHeader(Table table, String headerText, int colSpan) {
        Cell headerCell = new Cell(1, colSpan)
                .add(new Paragraph(headerText).setBold())
                .setBackgroundColor(HEADER_COLOR)
                .setFontColor(ColorConstants.WHITE)
                .setPadding(5);
        table.addCell(headerCell);
    }

    private static void addRow(Table table, String label, String value) {
        addRow(table, label, value, false);
    }

    private static void addRow(Table table, String label, String value, boolean highlighted) {
        table.addCell(createCell(label, true, highlighted));
        table.addCell(createCell(value, false, highlighted));
    }

    private static String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return currencyFormat.format(0);
        }
        return currencyFormat.format(amount);
    }
}