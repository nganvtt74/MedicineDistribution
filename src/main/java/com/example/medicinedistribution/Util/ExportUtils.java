package com.example.medicinedistribution.Util;

import com.example.medicinedistribution.BUS.BUSFactory;
import com.example.medicinedistribution.BUS.Interface.CustomerBUS;
import com.example.medicinedistribution.DTO.*;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.font.FontProvider;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.lowagie.text.pdf.BaseFont;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import lombok.extern.slf4j.Slf4j;
import org.xhtmlrenderer.pdf.ITextRenderer;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.html2pdf.resolver.font.DefaultFontProvider;
import com.itextpdf.styledxmlparser.css.media.MediaDeviceDescription;
import com.itextpdf.styledxmlparser.css.media.MediaType;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class ExportUtils {
public static void exportRevenueStatistics(List<StatisticDTO> revenueStats,
                                          LocalDate fromDate,
                                          LocalDate toDate,
                                          BigDecimal totalRevenue,
                                          int totalInvoices) throws Exception {

    // Create a file chooser dialog
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Lưu Báo Cáo Doanh Thu");
    fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
    fileChooser.setInitialFileName("BaoCao_DoanhThu_" +
            LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf");

    File file = fileChooser.showSaveDialog(null);
    if (file == null) return;

    // Create PDF document
    PdfWriter writer = new PdfWriter(file);
    PdfDocument pdf = new PdfDocument(writer);
    Document document = new Document(pdf);

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

    // Add title
    Paragraph title = new Paragraph("BÁO CÁO DOANH THU")
            .setFontSize(18)
            .setBold()
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(5);
    document.add(title);

    // Add date range
    String dateRange = "Kỳ báo cáo: " + fromDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    + " đến " + toDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    document.add(new Paragraph(dateRange)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(15));

    // Add export date
    document.add(new Paragraph("Ngày xuất: " + LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")))
            .setTextAlignment(TextAlignment.RIGHT)
            .setFontSize(10)
            .setMarginBottom(20));

    // Add summary section
    document.add(new Paragraph("TỔNG KẾT")
            .setFontSize(14)
            .setBold()
            .setMarginBottom(10));

    Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
            .setWidth(UnitValue.createPercentValue(100));

    // Add total revenue
    summaryTable.addCell(createHeaderCell("Tổng doanh thu:"));
    summaryTable.addCell(createValueCell(CurrencyUtils.formatVND(totalRevenue)));

    // Add total invoices
    summaryTable.addCell(createHeaderCell("Tổng hóa đơn:"));
    summaryTable.addCell(createValueCell(String.valueOf(totalInvoices)));

    // Add average revenue per invoice
    BigDecimal averageRevenue = totalInvoices > 0 ?
            totalRevenue.divide(BigDecimal.valueOf(totalInvoices), 0, BigDecimal.ROUND_HALF_UP) :
            BigDecimal.ZERO;

    summaryTable.addCell(createHeaderCell("Doanh thu trung bình/hóa đơn:"));
    summaryTable.addCell(createValueCell(CurrencyUtils.formatVND(averageRevenue)));

    document.add(summaryTable);
    document.add(new Paragraph("\n"));

    // Add details section
    document.add(new Paragraph("CHI TIẾT THEO THỜI GIAN")
            .setFontSize(14)
            .setBold()
            .setMarginBottom(10));

    // Create table
    Table table = new Table(UnitValue.createPercentArray(new float[]{40, 30, 30}))
            .setWidth(UnitValue.createPercentValue(100));

    // Add header row
    table.addHeaderCell(createTableHeaderCell("Thời gian"));
    table.addHeaderCell(createTableHeaderCell("Doanh thu"));
    table.addHeaderCell(createTableHeaderCell("Số hóa đơn"));

    // Add data rows
    for (StatisticDTO stat : revenueStats) {
        table.addCell(createTableCell(stat.getPeriod()));
        table.addCell(createTableCell(CurrencyUtils.formatVND(stat.getAmount())));
        table.addCell(createTableCell(String.valueOf(stat.getCount())));
    }

    document.add(table);

    // Add footer note
    document.add(new Paragraph("\nGhi chú: Báo cáo này được tạo tự động từ hệ thống.")
            .setFontSize(8)
            .setFontColor(ColorConstants.GRAY)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginTop(20));

    // Close the document
    document.close();
    NotificationUtil.showSuccessNotification("Xuất báo cáo thành công",
            "Báo cáo doanh thu đã được xuất thành công");

    // Open the PDF file
    if (Desktop.isDesktopSupported()) {
        Desktop.getDesktop().open(file);
    }
}

// Helper methods for creating cells with consistent styling
private static Cell createHeaderCell(String text) {
    return new Cell()
            .add(new Paragraph(text).setBold())
            .setPadding(5)
            .setBorder(Border.NO_BORDER);
}

private static Cell createValueCell(String text) {
    return new Cell()
            .add(new Paragraph(text))
            .setPadding(5)
            .setBorder(Border.NO_BORDER);
}

private static Cell createTableHeaderCell(String text) {
    return new Cell()
            .add(new Paragraph(text).setBold())
            .setBackgroundColor(new DeviceRgb(21, 101, 192))
            .setFontColor(ColorConstants.WHITE)
            .setPadding(5)
            .setTextAlignment(TextAlignment.CENTER);
}

private static Cell createTableCell(String text) {
    return new Cell()
            .add(new Paragraph(text))
            .setPadding(5)
            .setTextAlignment(TextAlignment.CENTER);
}

/**
 * Exports expense statistics to a PDF file
 *
 * @param expenseStats The list of expense statistics data
 * @param fromDate Starting date of the report period
 * @param toDate Ending date of the report period
 * @param totalExpense Total expense amount
 * @param totalReceipts Total number of receipts
 * @throws Exception If an error occurs during PDF generation
 */
public static void exportExpenseStatistics(List<StatisticDTO> expenseStats,
                                          LocalDate fromDate,
                                          LocalDate toDate,
                                          BigDecimal totalExpense,
                                          int totalReceipts) throws Exception {

    // Create a file chooser dialog
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Save Expense Report");
    fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
    fileChooser.setInitialFileName("Expense_Report_" +
            LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf");

    File file = fileChooser.showSaveDialog(null);
    if (file == null) return;

    // Create PDF document
    PdfWriter writer = new PdfWriter(file);
    PdfDocument pdf = new PdfDocument(writer);
    Document document = new Document(pdf);
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
    // Add title
    Paragraph title = new Paragraph("CHI PHÍ NHẬP HÀNG")
            .setFontSize(18)
            .setBold()
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(5);
    document.add(title);

    // Add date range
    String dateRange = "Kỳ báo cáo: " + fromDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    + " đến " + toDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    document.add(new Paragraph(dateRange)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(15));

    // Add export date
    document.add(new Paragraph("Ngày xuất: " + LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")))
            .setTextAlignment(TextAlignment.RIGHT)
            .setFontSize(10)
            .setMarginBottom(20));

    // Add summary section
    document.add(new Paragraph("TỔNG KẾT")
            .setFontSize(14)
            .setBold()
            .setMarginBottom(10));

    Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
            .setWidth(UnitValue.createPercentValue(100));

    // Add total expense
    summaryTable.addCell(createHeaderCell("Tổng chi phí:"));
    summaryTable.addCell(createValueCell(CurrencyUtils.formatVND(totalExpense)));

    // Add total receipts
    summaryTable.addCell(createHeaderCell("Tổng phiếu nhập:"));
    summaryTable.addCell(createValueCell(String.valueOf(totalReceipts)));

    // Add average expense per receipt
    BigDecimal averageExpense = totalReceipts > 0 ?
            totalExpense.divide(BigDecimal.valueOf(totalReceipts), 0, BigDecimal.ROUND_HALF_UP) :
            BigDecimal.ZERO;

    summaryTable.addCell(createHeaderCell("Chi phí trung bình/phiếu nhập:"));
    summaryTable.addCell(createValueCell(CurrencyUtils.formatVND(averageExpense)));

    document.add(summaryTable);
    document.add(new Paragraph("\n"));

    // Add details section
    document.add(new Paragraph("CHI TIẾT THEO THỜI GIAN")
            .setFontSize(14)
            .setBold()
            .setMarginBottom(10));

    // Create table
    Table table = new Table(UnitValue.createPercentArray(new float[]{40, 30, 30}))
            .setWidth(UnitValue.createPercentValue(100));

    // Add header row
    table.addHeaderCell(createTableHeaderCell("Thời gian"));
    table.addHeaderCell(createTableHeaderCell("Tổng chi phí"));
    table.addHeaderCell(createTableHeaderCell("Số phiếu nhập"));

    // Add data rows
    for (StatisticDTO stat : expenseStats) {
        table.addCell(createTableCell(stat.getPeriod()));
        table.addCell(createTableCell(CurrencyUtils.formatVND(stat.getAmount())));
        table.addCell(createTableCell(String.valueOf(stat.getCount())));
    }

    document.add(table);
    // Add footer note
    document.add(new Paragraph("\nGhi chú: Báo cáo này được tạo tự động từ hệ thống.")
            .setFontSize(8)
            .setFontColor(ColorConstants.GRAY)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginTop(20));

    // Close the document
    document.close();

    // Open the PDF file
    if (Desktop.isDesktopSupported()) {
        Desktop.getDesktop().open(file);
    }
}
    /**
     * Exports profit statistics to a PDF file
     *
     * @param profitStats The list of profit statistics data
     * @param fromDate Starting date of the report period
     * @param toDate Ending date of the report period
     * @param totalRevenue Total revenue amount
     * @param totalExpense Total expense amount
     * @param totalProfit Total profit amount
     * @throws Exception If an error occurs during PDF generation
     */
    public static void exportProfitStatistics(List<StatisticDTO> profitStats,
                                             LocalDate fromDate,
                                             LocalDate toDate,
                                             BigDecimal totalRevenue,
                                             BigDecimal totalExpense,
                                             BigDecimal totalProfit) throws Exception {

        // Create a file chooser dialog
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Lưu Báo Cáo Lợi Nhuận");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName("BaoCao_LoiNhuan_" +
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf");

        File file = fileChooser.showSaveDialog(null);
        if (file == null) return;

        // Create PDF document
        PdfWriter writer = new PdfWriter(file);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

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

        // Add title
        Paragraph title = new Paragraph("BÁO CÁO LỢI NHUẬN")
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5);
        document.add(title);

        // Add date range
        String dateRange = "Kỳ báo cáo: " + fromDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        + " đến " + toDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        document.add(new Paragraph(dateRange)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(15));

        // Add export date
        document.add(new Paragraph("Ngày xuất: " + LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")))
                .setTextAlignment(TextAlignment.RIGHT)
                .setFontSize(10)
                .setMarginBottom(20));

        // Add summary section
        document.add(new Paragraph("TỔNG KẾT")
                .setFontSize(14)
                .setBold()
                .setMarginBottom(10));

        Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .setWidth(UnitValue.createPercentValue(100));

        // Add total revenue
        summaryTable.addCell(createHeaderCell("Tổng doanh thu:"));
        summaryTable.addCell(createValueCell(CurrencyUtils.formatVND(totalRevenue)));

        // Add total expense
        summaryTable.addCell(createHeaderCell("Tổng chi phí:"));
        summaryTable.addCell(createValueCell(CurrencyUtils.formatVND(totalExpense)));

        // Add total profit
        summaryTable.addCell(createHeaderCell("Lợi nhuận:"));
        summaryTable.addCell(createValueCell(CurrencyUtils.formatVND(totalProfit)));

        // Add profit margin
        BigDecimal profitMargin = BigDecimal.ZERO;
        if (totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
            profitMargin = totalProfit.multiply(new BigDecimal("100"))
                    .divide(totalRevenue, 2, RoundingMode.HALF_UP);
        }

        summaryTable.addCell(createHeaderCell("Tỷ suất lợi nhuận:"));
        summaryTable.addCell(createValueCell(profitMargin + "%"));

        document.add(summaryTable);
        document.add(new Paragraph("\n"));

        // Add profit chart
        document.add(new Paragraph("DOANH THU VÀ CHI PHÍ")
                .setFontSize(14)
                .setBold()
                .setMarginBottom(10));

        // Create comparison chart
        Table comparisonTable = new Table(UnitValue.createPercentArray(new float[]{33, 33, 33}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(20);

        comparisonTable.addCell(createChartCell("DOANH THU", totalRevenue, new DeviceRgb(46, 125, 50)));
        comparisonTable.addCell(createChartCell("CHI PHÍ", totalExpense, new DeviceRgb(198, 40, 40)));
        comparisonTable.addCell(createChartCell("LỢI NHUẬN", totalProfit, new DeviceRgb(13, 71, 161)));

        document.add(comparisonTable);
        document.add(new Paragraph("\n"));

        // Add details section
        document.add(new Paragraph("CHI TIẾT LỢI NHUẬN THEO THỜI GIAN")
                .setFontSize(14)
                .setBold()
                .setMarginBottom(10));

        // Create table
        Table table = new Table(UnitValue.createPercentArray(new float[]{25, 25, 25, 25}))
                .setWidth(UnitValue.createPercentValue(100));

        // Add header row
        table.addHeaderCell(createTableHeaderCell("Thời gian"));
        table.addHeaderCell(createTableHeaderCell("Doanh thu"));
        table.addHeaderCell(createTableHeaderCell("Chi phí"));
        table.addHeaderCell(createTableHeaderCell("Lợi nhuận"));

        // Add data rows
        for (StatisticDTO stat : profitStats) {
            table.addCell(createTableCell(stat.getPeriod()));
            table.addCell(createTableCell(CurrencyUtils.formatVND(stat.getRevenue())));
            table.addCell(createTableCell(CurrencyUtils.formatVND(stat.getExpense())));

            Cell profitCell = createTableCell(CurrencyUtils.formatVND(stat.getProfit()));
            if (stat.getProfit().compareTo(BigDecimal.ZERO) < 0) {
                profitCell.setFontColor(ColorConstants.RED);
            } else {
                profitCell.setFontColor(new DeviceRgb(0, 100, 0));
            }
            table.addCell(profitCell);
        }

        document.add(table);

        // Add footer note
        document.add(new Paragraph("\nGhi chú: Báo cáo này được tạo tự động từ hệ thống.")
                .setFontSize(8)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20));

        // Close the document
        document.close();
        NotificationUtil.showSuccessNotification("Xuất báo cáo thành công",
                "Báo cáo lợi nhuận đã được xuất thành công");

        // Open the PDF file
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(file);
        }
    }
    private static Cell createChartCell(String label, BigDecimal value, DeviceRgb color) {
        Cell cell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setPadding(5);

        Paragraph chartLabel = new Paragraph(label)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);

        Paragraph chartValue = new Paragraph(CurrencyUtils.formatVND(value))
                .setFontColor(color)
                .setBold()
                .setFontSize(14)
                .setTextAlignment(TextAlignment.CENTER);

        cell.add(chartLabel).add(chartValue);
        return cell;
    }

/**
 * Exports product sales statistics to a PDF file
 *
 * @param productStats The list of product sales statistics data
 * @param fromDate Starting date of the report period
 * @param toDate Ending date of the report period
 * @param totalQuantity Total quantity of products sold
 * @param totalProducts Total number of unique products sold
 * @param averageQuantity Average quantity sold per product
 * @throws Exception If an error occurs during PDF generation
 */
public static void exportProductSalesStatistics(List<ProductStatisticDTO> productStats,
                                               LocalDate fromDate,
                                               LocalDate toDate,
                                               int totalQuantity,
                                               int totalProducts,
                                               double averageQuantity) throws Exception {

    // Create a file chooser dialog
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Lưu Báo Cáo Thống Kê Sản Phẩm");
    fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
    fileChooser.setInitialFileName("BaoCao_SanPham_" +
            LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf");

    File file = fileChooser.showSaveDialog(null);
    if (file == null) return;

    // Create PDF document
    PdfWriter writer = new PdfWriter(file);
    PdfDocument pdf = new PdfDocument(writer);
    Document document = new Document(pdf);

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

    // Add title
    Paragraph title = new Paragraph("BÁO CÁO THỐNG KÊ SẢN PHẨM BÁN CHẠY")
            .setFontSize(18)
            .setBold()
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(5);
    document.add(title);

    // Add date range
    String dateRange = "Kỳ báo cáo: " + fromDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    + " đến " + toDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    document.add(new Paragraph(dateRange)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(15));

    // Add export date
    document.add(new Paragraph("Ngày xuất: " + LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")))
            .setTextAlignment(TextAlignment.RIGHT)
            .setFontSize(10)
            .setMarginBottom(20));

    // Add summary section
    document.add(new Paragraph("TỔNG KẾT")
            .setFontSize(14)
            .setBold()
            .setMarginBottom(10));

    Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
            .setWidth(UnitValue.createPercentValue(100));

    // Add total quantity
    summaryTable.addCell(createHeaderCell("Tổng số lượng bán:"));
    summaryTable.addCell(createValueCell(String.format("%,d", totalQuantity)));

    // Add total products
    summaryTable.addCell(createHeaderCell("Tổng số sản phẩm:"));
    summaryTable.addCell(createValueCell(String.format("%,d", totalProducts)));

    // Add average quantity
    summaryTable.addCell(createHeaderCell("Trung bình số lượng bán/sản phẩm:"));
    summaryTable.addCell(createValueCell(String.format("%.1f", averageQuantity)));

    document.add(summaryTable);
    document.add(new Paragraph("\n"));

    // Add top 10 best-selling products section
    document.add(new Paragraph("TOP 10 SẢN PHẨM BÁN CHẠY NHẤT")
            .setFontSize(14)
            .setBold()
            .setMarginBottom(10));

    // Create a chart for top 10 products
    List<ProductStatisticDTO> top10Products = productStats.stream()
            .sorted((a, b) -> b.getQuantity() - a.getQuantity())
            .limit(10)
            .collect(Collectors.toList());

    // Create a visual representation of top products
    Table chartTable = new Table(UnitValue.createPercentArray(new float[]{40, 60}))
            .setWidth(UnitValue.createPercentValue(100));

    // Find max quantity for scaling
    int maxQuantity = top10Products.stream()
            .mapToInt(ProductStatisticDTO::getQuantity)
            .max()
            .orElse(1);

    // Add chart bars for top products
 // Create a better visual bar chart
 for (ProductStatisticDTO product : top10Products) {
     // Create the product label cell
     Cell labelCell = new Cell()
             .add(new Paragraph(product.getProductName()))
             .setTextAlignment(TextAlignment.RIGHT)
             .setPadding(5)
             .setBorder(Border.NO_BORDER);

     // Calculate bar width as percentage of max
     int widthPercentage = product.getQuantity() * 100 / maxQuantity;

     // Create bar visualization using Unicode block characters
     StringBuilder barBuilder = new StringBuilder();
     barBuilder.append("█".repeat(widthPercentage / 5)); // Each block represents 5%

     // Create bar cell with colored background
     Cell barCell = new Cell()
             .add(new Paragraph(barBuilder.toString() + " " + product.getQuantity())
                     .setFontColor(ColorConstants.WHITE)
                     .setBold())
             .setBackgroundColor(new DeviceRgb(33, 150, 243))
             .setPadding(5)
             .setBorder(Border.NO_BORDER);

     chartTable.addCell(labelCell);
     chartTable.addCell(barCell);
 }
    document.add(chartTable);
    document.add(new Paragraph("\n"));

    // Add category distribution section
    document.add(new Paragraph("PHÂN PHỐI THEO DANH MỤC")
            .setFontSize(14)
            .setBold()
            .setMarginBottom(10));

    // Calculate category distribution
    Map<String, Integer> categoryStats = new HashMap<>();
    for (ProductStatisticDTO product : productStats) {
        String category = product.getCategoryName();
        categoryStats.put(category, categoryStats.getOrDefault(category, 0) + product.getQuantity());
    }

    // Create category table
    Table categoryTable = new Table(UnitValue.createPercentArray(new float[]{50, 25, 25}))
            .setWidth(UnitValue.createPercentValue(100));

    categoryTable.addHeaderCell(createTableHeaderCell("Danh mục"));
    categoryTable.addHeaderCell(createTableHeaderCell("Số lượng"));
    categoryTable.addHeaderCell(createTableHeaderCell("Tỷ lệ"));

    // Add category rows
    for (Map.Entry<String, Integer> entry : categoryStats.entrySet()) {
        String category = entry.getKey();
        int quantity = entry.getValue();
        double percentage = (double) quantity / totalQuantity * 100;

        categoryTable.addCell(createTableCell(category));
        categoryTable.addCell(createTableCell(String.format("%,d", quantity)));
        categoryTable.addCell(createTableCell(String.format("%.1f%%", percentage)));
    }

    document.add(categoryTable);
    document.add(new Paragraph("\n"));

    // Add detailed product list section
    document.add(new Paragraph("CHI TIẾT DANH SÁCH SẢN PHẨM")
            .setFontSize(14)
            .setBold()
            .setMarginBottom(10));

    // Create detailed table
    Table detailTable = new Table(UnitValue.createPercentArray(new float[]{15, 40, 25, 20}))
            .setWidth(UnitValue.createPercentValue(100));

    detailTable.addHeaderCell(createTableHeaderCell("Mã SP"));
    detailTable.addHeaderCell(createTableHeaderCell("Tên sản phẩm"));
    detailTable.addHeaderCell(createTableHeaderCell("Danh mục"));
    detailTable.addHeaderCell(createTableHeaderCell("Số lượng"));

    // Add all products to table
    for (ProductStatisticDTO product : productStats) {
        detailTable.addCell(createTableCell(String.valueOf(product.getProductId())));
        detailTable.addCell(createTableCell(product.getProductName()));
        detailTable.addCell(createTableCell(product.getCategoryName()));
        detailTable.addCell(createTableCell(String.valueOf(product.getQuantity())));
    }

    document.add(detailTable);

    // Add footer note
    document.add(new Paragraph("\nGhi chú: Báo cáo này được tạo tự động từ hệ thống.")
            .setFontSize(8)
            .setFontColor(ColorConstants.GRAY)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginTop(20));

    // Close the document
    document.close();

    NotificationUtil.showSuccessNotification(
            "Xuất báo cáo thành công",
            "Báo cáo thống kê sản phẩm \n đã được xuất thành công"
    );

    // Open the PDF file
    if (Desktop.isDesktopSupported()) {
        Desktop.getDesktop().open(file);
    }
}
/**
 * Exports the invoice data to a file pdf.
 *
 * @param invoice The invoice data to export.
 *                invoiceDetail in the invoiceDTO
 * using itextpdf library
 * using html to pdf converter
 */
public static void exportInvoice(InvoiceDTO invoice, BUSFactory busFactory) {
    try {
        LocalDate invoiceDate = invoice.getDate();
        int month = invoiceDate.getMonthValue();
        int year = invoiceDate.getYear();
        String directoryPath = "./invoice/" + month + "_" + year;
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Define the file path directly
        File file = new File(directory, "HoaDon_" + invoice.getInvoiceId() + ".pdf");

        // Create HTML content
        String htmlContent = generateInvoiceHtml(invoice, busFactory);

        // Create PDF using iText pdfHTML
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            ConverterProperties properties = new ConverterProperties();

            // Set up font provider for Vietnamese support
            FontProvider fontProvider = new DefaultFontProvider(false, false, false);
            String fontPath = "./fonts/arial-unicode-ms.ttf"; // Đường dẫn đến file font
            fontProvider.addFont(fontPath);
            properties.setFontProvider(fontProvider);

            HtmlConverter.convertToPdf(htmlContent, outputStream, properties);
        }

        NotificationUtil.showSuccessNotification("Xuất hóa đơn thành công",
            "Hóa đơn đã được lưu tại: " + file.getAbsolutePath());

        // Open the PDF file
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(file);
        }
    } catch (Exception e) {
        log.error("Lỗi khi xuất hóa đơn: {}", e.getMessage(), e);
        NotificationUtil.showErrorNotification("Lỗi", "Không thể xuất hóa đơn: " + e.getMessage());
    }
}

/**
 * Generate HTML content for the invoice
 */
private static String generateInvoiceHtml(InvoiceDTO invoice, BUSFactory busFactory) {
    StringBuilder html = new StringBuilder();
    CustomerDTO customer = busFactory.getCustomerBUS().findById(invoice.getCustomerId());
    EmployeeDTO employee = busFactory.getEmployeeBUS().findById(invoice.getEmployeeId());

    // Start HTML document
    html.append("<!DOCTYPE html>\n")
            .append("<html>\n")
            .append("<head>\n")
            .append("<meta charset=\"UTF-8\" />\n")
            .append("<title>Hóa Đơn</title>\n")
            .append("<style>\n")
            .append("body { font-family: 'Arial Unicode MS', Arial, sans-serif; margin: 20px; }\n")
            .append(".header { text-align: center; margin-bottom: 20px; }\n")
            .append(".invoice-info { margin-bottom: 20px; }\n")
            .append(".info-column { display: inline-block; vertical-align: top; }\n")
            .append(".info-column:first-child { width: 300px; margin-right: 20px; }\n")
            .append(".info-column:last-child { width: 300px; }\n")
            .append(".info-row { margin-bottom: 5px; }\n")
            .append(".info-label { font-weight: bold; display: inline-block; width: 120px; }\n")
            .append(".divider { border-top: 1px solid #ccc; margin: 15px 0; }\n")
            .append("table { width: 100%; border-collapse: collapse; }\n")
            .append("th, td { border: 1px solid #ddd; padding: 8px; }\n")
            .append("th { background-color: #f2f2f2; text-align: left; }\n")
            .append(".text-right { text-align: right; }\n")
            .append(".total-row { font-weight: bold; font-size: 16px; margin-top: 15px; text-align: right; }\n")
            .append(".footer { margin-top: 30px; text-align: center; font-style: italic; }\n")
            .append("</style>\n")
            .append("</head>\n")
            .append("<body>\n");

    // Header
    html.append("<div class=\"header\">\n")
            .append("<h1>HÓA ĐƠN BÁN HÀNG</h1>\n")
            .append("</div>\n");

    // Invoice Info
    html.append("<div class=\"invoice-info\">\n")
            // Left column
            .append("<div class=\"info-column\">\n")
            .append("<div class=\"info-row\"><span class=\"info-label\">Mã hóa đơn:</span> ").append(invoice.getInvoiceId()).append("</div>\n")
            .append("<div class=\"info-row\"><span class=\"info-label\">Ngày lập:</span> ").append(formatDate(invoice.getDate())).append("</div>\n")
            .append("<div class=\"info-row\"><span class=\"info-label\">Nhân viên:</span> ")
            .append(employee != null ? employee.getFullName() : invoice.getEmployeeId()).append("</div>\n")
            .append("</div>\n")
            // Right column
            .append("<div class=\"info-column\">\n")
            .append("<div class=\"info-row\"><span class=\"info-label\">Khách hàng:</span> ").append(customer.getCustomerName()).append("</div>\n")
            .append("<div class=\"info-row\"><span class=\"info-label\">Số điện thoại:</span> ").append(customer.getPhone()).append("</div>\n")
            .append("<div class=\"info-row\"><span class=\"info-label\">Địa chỉ:</span> ").append(customer.getAddress()).append("</div>\n")
            .append("</div>\n")
            .append("</div>\n");

    // Divider
    html.append("<div class=\"divider\"></div>\n");

    // Product Details Table
    html.append("<h3>Chi tiết sản phẩm:</h3>\n")
            .append("<table>\n")
            .append("<thead>\n")
            .append("<tr>\n")
            .append("<th>Mã SP</th>\n")
            .append("<th>Tên sản phẩm</th>\n")
            .append("<th>Số lượng</th>\n")
            .append("<th>Đơn vị</th>\n")
            .append("<th>Đơn giá</th>\n")
            .append("<th>Thành tiền</th>\n")
            .append("</tr>\n")
            .append("</thead>\n")
            .append("<tbody>\n");

    // Table rows for each product
    BigDecimal total = BigDecimal.ZERO;
    for (InvoiceDetailDTO detail : invoice.getDetails()) {
        BigDecimal lineTotal = detail.getPrice().multiply(BigDecimal.valueOf(detail.getQuantity()));
        total = total.add(lineTotal);

        html.append("<tr>\n")
                .append("<td>").append(detail.getProductId()).append("</td>\n")
                .append("<td>").append(detail.getProductName()).append("</td>\n")
                .append("<td>").append(detail.getQuantity()).append("</td>\n")
                .append("<td>").append(detail.getUnit()).append("</td>\n")
                .append("<td class=\"text-right\">").append(formatCurrency(detail.getPrice())).append("</td>\n")
                .append("<td class=\"text-right\">").append(formatCurrency(lineTotal)).append("</td>\n")
                .append("</tr>\n");
    }

    // Close table
    html.append("</tbody>\n")
            .append("</table>\n");

    // Total amount
    html.append("<div class=\"total-row\">")
            .append("Tổng tiền: ").append(formatCurrency(total))
            .append("</div>\n");

    // Footer
    html.append("<div class=\"footer\">\n")
            .append("<p>Cảm ơn quý khách đã mua hàng!</p>\n")
            .append("</div>\n");

    // Close document
    html.append("</body>\n")
            .append("</html>");

    return html.toString();
}

/**
 * Format a date to dd/MM/yyyy
 */
private static String formatDate(LocalDate date) {
    if (date == null) return "";
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    return date.format(formatter);
}

/**
 * Format a BigDecimal as currency
 */
private static String formatCurrency(BigDecimal amount) {
    if (amount == null) return "0₫";
    DecimalFormat formatter = new DecimalFormat("#,###");
    return formatter.format(amount) + "₫";
}

    /**
     * Exports the goods receipt data to a PDF file.
     *
     * @param receipt The goods receipt data to export.
     * @param busFactory BUSFactory for data access
     */
    public static void exportGoodsReceipt(GoodsReceiptDTO receipt, BUSFactory busFactory) {
        try {
            LocalDate receiptDate = receipt.getDate();
            int month = receiptDate.getMonthValue();
            int year = receiptDate.getYear();
            String directoryPath = "./goodsReceipt/" + month + "_" + year;
            File directory = new File(directoryPath);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Define the file path directly
            File file = new File(directory, "PhieuNhap_" + receipt.getGoodsReceiptId() + ".pdf");

            // Create HTML content
            String htmlContent = generateGoodsReceiptHtml(receipt, busFactory);

            // Create PDF using iText pdfHTML
            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                ConverterProperties properties = new ConverterProperties();

                // Set up font provider for Vietnamese support
                FontProvider fontProvider = new DefaultFontProvider(false, false, false);
                String fontPath = "./fonts/arial-unicode-ms.ttf"; // Đường dẫn đến file font
                fontProvider.addFont(fontPath);
                properties.setFontProvider(fontProvider);

                HtmlConverter.convertToPdf(htmlContent, outputStream, properties);
            }

            NotificationUtil.showSuccessNotification("Xuất phiếu nhập thành công",
                    "Phiếu nhập đã được lưu tại: " + file.getAbsolutePath());

            // Open the PDF file
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }
        } catch (Exception e) {
            log.error("Lỗi khi xuất phiếu nhập: {}", e.getMessage(), e);
            NotificationUtil.showErrorNotification("Lỗi", "Không thể xuất phiếu nhập: " + e.getMessage());
        }
    }

    /**
     * Generate HTML content for the goods receipt
     */
    private static String generateGoodsReceiptHtml(GoodsReceiptDTO receipt, BUSFactory busFactory) {
        StringBuilder html = new StringBuilder();
        ManufacturerDTO manufacturer = busFactory.getManufacturerBUS().findById(receipt.getManufacturerId());
        EmployeeDTO employee = busFactory.getEmployeeBUS().findById(receipt.getEmployeeId());

        // Start HTML document
        html.append("<!DOCTYPE html>\n")
                .append("<html>\n")
                .append("<head>\n")
                .append("<meta charset=\"UTF-8\" />\n")
                .append("<title>Phiếu Nhập</title>\n")
                .append("<style>\n")
                .append("body { font-family: 'Arial Unicode MS', Arial, sans-serif; margin: 20px; }\n")
                .append(".header { text-align: center; margin-bottom: 20px; }\n")
                .append(".receipt-info { margin-bottom: 20px; }\n")
                .append(".info-column { display: inline-block; vertical-align: top; }\n")
                .append(".info-column:first-child { width: 300px; margin-right: 20px; }\n")
                .append(".info-column:last-child { width: 300px; }\n")
                .append(".info-row { margin-bottom: 5px; }\n")
                .append(".info-label { font-weight: bold; display: inline-block; width: 120px; }\n")
                .append(".divider { border-top: 1px solid #ccc; margin: 15px 0; }\n")
                .append("table { width: 100%; border-collapse: collapse; }\n")
                .append("th, td { border: 1px solid #ddd; padding: 8px; }\n")
                .append("th { background-color: #f2f2f2; text-align: left; }\n")
                .append(".text-right { text-align: right; }\n")
                .append(".total-row { font-weight: bold; font-size: 16px; margin-top: 15px; text-align: right; }\n")
                .append(".footer { margin-top: 30px; text-align: center; font-style: italic; }\n")
                .append("</style>\n")
                .append("</head>\n")
                .append("<body>\n");

        // Header
        html.append("<div class=\"header\">\n")
                .append("<h1>PHIẾU NHẬP HÀNG</h1>\n")
                .append("</div>\n");

        // Receipt Info
        html.append("<div class=\"receipt-info\">\n")
                // Left column
                .append("<div class=\"info-column\">\n")
                .append("<div class=\"info-row\"><span class=\"info-label\">Mã phiếu nhập:</span> ").append(receipt.getGoodsReceiptId()).append("</div>\n")
                .append("<div class=\"info-row\"><span class=\"info-label\">Ngày lập:</span> ").append(formatDate(receipt.getDate())).append("</div>\n")
                .append("<div class=\"info-row\"><span class=\"info-label\">Nhân viên:</span> ")
                .append(employee != null ? employee.getFullName() : receipt.getEmployeeId()).append("</div>\n")
                .append("</div>\n")
                // Right column
                .append("<div class=\"info-column\">\n")
                .append("<div class=\"info-row\"><span class=\"info-label\">Nhà sản xuất:</span> ").append(manufacturer.getManufacturerName()).append("</div>\n")
                .append("<div class=\"info-row\"><span class=\"info-label\">Số điện thoại:</span> ").append(manufacturer.getPhone()).append("</div>\n")
                .append("<div class=\"info-row\"><span class=\"info-label\">Địa chỉ:</span> ").append(manufacturer.getAddress()).append("</div>\n")
                .append("</div>\n")
                .append("</div>\n");

        // Divider
        html.append("<div class=\"divider\"></div>\n");

        // Product Details Table
        html.append("<h3>Chi tiết sản phẩm nhập:</h3>\n")
                .append("<table>\n")
                .append("<thead>\n")
                .append("<tr>\n")
                .append("<th>Mã SP</th>\n")
                .append("<th>Tên sản phẩm</th>\n")
                .append("<th>Số lượng</th>\n")
                .append("<th>Đơn vị</th>\n")
                .append("<th>Đơn giá</th>\n")
                .append("<th>Thành tiền</th>\n")
                .append("</tr>\n")
                .append("</thead>\n")
                .append("<tbody>\n");

        // Table rows for each product
        BigDecimal total = BigDecimal.ZERO;
        for (GoodsReceiptDetailDTO detail : receipt.getDetails()) {
            BigDecimal lineTotal = detail.getPrice().multiply(BigDecimal.valueOf(detail.getQuantity()));
            total = total.add(lineTotal);

            html.append("<tr>\n")
                    .append("<td>").append(detail.getProductId()).append("</td>\n")
                    .append("<td>").append(detail.getProductName()).append("</td>\n")
                    .append("<td>").append(detail.getQuantity()).append("</td>\n")
                    .append("<td>").append(detail.getUnit()).append("</td>\n")
                    .append("<td class=\"text-right\">").append(formatCurrency(detail.getPrice())).append("</td>\n")
                    .append("<td class=\"text-right\">").append(formatCurrency(lineTotal)).append("</td>\n")
                    .append("</tr>\n");
        }

        // Close table
        html.append("</tbody>\n")
                .append("</table>\n");

        // Total amount
        html.append("<div class=\"total-row\">")
                .append("Tổng tiền: ").append(formatCurrency(total))
                .append("</div>\n");

        // Footer
        html.append("<div class=\"footer\">\n")
                .append("<p>Phiếu nhập hàng từ ").append(manufacturer.getManufacturerName()).append("</p>\n")
                .append("</div>\n");

        // Close document
        html.append("</body>\n")
                .append("</html>");

        return html.toString();
    }
    public static void exportEmployeeStatistics(LocalDate startDate, LocalDate endDate, int currentEmployees, int newEmployees, int maternityLeaveEmployees, BigDecimal totalSalary, BigDecimal totalDeductions, List<PayrollDTO> payrollList) throws Exception {
        // Create a file chooser dialog
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Lưu Báo Cáo Nhân Viên");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName("BaoCao_NhanVien_" +
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf");

        File file = fileChooser.showSaveDialog(null);
        if (file == null) return;

        // Create PDF document
        PdfWriter writer = new PdfWriter(file);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        try {
            // Load font that supports Vietnamese
            String fontPath = "./fonts/arial-unicode-ms.ttf";
            PdfFont font = PdfFontFactory.createFont(fontPath, PdfEncodings.IDENTITY_H,
                    PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED);
            document.setFont(font);
        } catch (IOException e) {
            log.error("Error loading font: {}", e.getMessage());
            // Fall back to default font if custom font fails to load
            document.setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA));
        }

        // Add title
        Paragraph title = new Paragraph("BÁO CÁO THỐNG KÊ NHÂN VIÊN")
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5);
        document.add(title);

        // Add date range
        String dateRange = "Kỳ báo cáo: " + startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        + " đến " + endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        document.add(new Paragraph(dateRange)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(15));

        // Add export date
        document.add(new Paragraph("Ngày xuất: " + LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")))
                .setTextAlignment(TextAlignment.RIGHT)
                .setFontSize(10)
                .setMarginBottom(20));

        // Add summary section
        document.add(new Paragraph("TỔNG KẾT NHÂN SỰ")
                .setFontSize(14)
                .setBold()
                .setMarginBottom(10));

        Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .setWidth(UnitValue.createPercentValue(100));

        // Add employee counts
        summaryTable.addCell(createHeaderCell("Tổng nhân viên hiện tại:"));
        summaryTable.addCell(createValueCell(String.valueOf(currentEmployees)));

        summaryTable.addCell(createHeaderCell("Nhân viên mới:"));
        summaryTable.addCell(createValueCell(String.valueOf(newEmployees)));

        summaryTable.addCell(createHeaderCell("Nhân viên nghỉ thai sản:"));
        summaryTable.addCell(createValueCell(String.valueOf(maternityLeaveEmployees)));

        document.add(summaryTable);
        document.add(new Paragraph("\n"));

        // Add salary summary section
        document.add(new Paragraph("TỔNG KẾT LƯƠNG")
                .setFontSize(14)
                .setBold()
                .setMarginBottom(10));

        Table salaryTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .setWidth(UnitValue.createPercentValue(100));

        // Calculate net salary (after deductions)
        BigDecimal netSalary = totalSalary.subtract(totalDeductions);

        // Add salary statistics
        salaryTable.addCell(createHeaderCell("Tổng lương:"));
        salaryTable.addCell(createValueCell(CurrencyUtils.formatVND(totalSalary)));

        salaryTable.addCell(createHeaderCell("Tổng khấu trừ:"));
        salaryTable.addCell(createValueCell(CurrencyUtils.formatVND(totalDeductions)));

        salaryTable.addCell(createHeaderCell("Tổng thực lãnh:"));
        salaryTable.addCell(createValueCell(CurrencyUtils.formatVND(netSalary)));

        // Calculate average salary if there are employees
        if (currentEmployees > 0) {
            BigDecimal avgSalary = netSalary.divide(BigDecimal.valueOf(currentEmployees), 0, RoundingMode.HALF_UP);
            salaryTable.addCell(createHeaderCell("Lương thực lãnh trung bình:"));
            salaryTable.addCell(createValueCell(CurrencyUtils.formatVND(avgSalary)));
        }

        document.add(salaryTable);
        document.add(new Paragraph("\n"));


        // Add footer note
        document.add(new Paragraph("\nGhi chú: Báo cáo này được tạo tự động từ hệ thống.")
                .setFontSize(8)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20));

        // Close the document
        document.close();
        NotificationUtil.showSuccessNotification("Xuất báo cáo thành công",
                "Báo cáo thống kê nhân viên đã được xuất thành công");

        // Open the PDF file
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(file);
        }
    }
}
