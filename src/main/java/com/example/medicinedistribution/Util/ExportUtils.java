package com.example.medicinedistribution.Util;

import com.example.medicinedistribution.BUS.BUSFactory;
import com.example.medicinedistribution.BUS.Interface.CustomerBUS;
import com.example.medicinedistribution.DTO.*;
import com.itextpdf.layout.font.FontProvider;
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
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

@Slf4j
public class ExportUtils {
    public static void exportRevenueStatistics(ObservableList<StatisticDTO> items, LocalDate value, LocalDate value1,
                                               BigDecimal decimal, int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public static void exportExpenseStatistics(ObservableList<StatisticDTO> items, LocalDate value, LocalDate value1,
                                               BigDecimal decimal, int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public static void exportProfitStatistics(ObservableList<StatisticDTO> items, LocalDate value, LocalDate value1,
                                              BigDecimal decimal, BigDecimal decimal1, BigDecimal decimal2) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public static void exportProductSalesStatistics(ObservableList<ProductStatisticDTO> items, LocalDate value, LocalDate value1, int i, int i1, double v) {
        throw new UnsupportedOperationException("Not supported yet.");
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
}
