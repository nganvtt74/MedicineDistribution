package com.example.medicinedistribution.DAO;

import com.example.medicinedistribution.DAO.Interface.InvoiceDAO;
import com.example.medicinedistribution.DTO.InvoiceDTO;
import com.example.medicinedistribution.DTO.ProductStatisticDTO;
import com.example.medicinedistribution.DTO.StatisticDTO;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class InvoiceDAOImpl implements InvoiceDAO {

    @Override
    public Integer insert(InvoiceDTO invoiceDTO, Connection conn) {
        String sql = "INSERT INTO invoice (employeeId, customerId, date, total) VALUES (?, ?, ?, ?)";
        try(PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){

            stmt.setInt(1, invoiceDTO.getEmployeeId());
            stmt.setInt(2, invoiceDTO.getCustomerId());
            stmt.setDate(3, java.sql.Date.valueOf(invoiceDTO.getDate()));
            stmt.setBigDecimal(4, invoiceDTO.getTotal());
            stmt.executeUpdate();
            try (var rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            log.error("Lỗi khi thêm hóa đơn: {}", e.getMessage());

        }
        return null;
    }

    @Override
    public boolean update(InvoiceDTO invoiceDTO, Connection conn) {
        throw new UnsupportedOperationException("Không hỗ trợ cập nhật hóa đơn");
    }

    @Override
    public boolean delete(Integer integer, Connection conn) {
        String sql = "DELETE FROM invoice WHERE invoiceId = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, integer);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return false;
    }

    @Override
    public InvoiceDTO findById(Integer integer, Connection conn) {
        String sql = "SELECT * FROM invoice WHERE invoiceId = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, integer);
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return InvoiceDTO.builder()
                            .invoiceId(rs.getInt("invoiceId"))
                            .employeeId(rs.getInt("employeeId"))
                            .customerId(rs.getInt("customerId"))
                            .date(rs.getDate("date").toLocalDate())
                            .total(rs.getBigDecimal("total"))
                            .build();
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    @Override
    public List<InvoiceDTO> findAll(Connection conn) {
        String sql = "SELECT * FROM invoice";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            try (var rs = stmt.executeQuery()) {
                List<InvoiceDTO> invoiceDTOList = new java.util.ArrayList<>();
                while (rs.next()) {
                    invoiceDTOList.add(InvoiceDTO.builder()
                            .invoiceId(rs.getInt("invoiceId"))
                            .employeeId(rs.getInt("employeeId"))
                            .customerId(rs.getInt("customerId"))
                            .date(rs.getDate("date").toLocalDate())
                            .total(rs.getBigDecimal("total"))
                            .build());
                }
                return invoiceDTOList;
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    @Override
    public Map<String, BigDecimal> getRevenueByCategoryStatistics(LocalDate fromDate, LocalDate toDate, Connection conn) {
        Map<String, BigDecimal> result = new HashMap<>();
        String sql = """
                SELECT 
                    c.categoryName, 
                    SUM(id.price * id.quantity) as total_revenue
                FROM 
                    invoicedetail id
                JOIN 
                    invoice i ON id.invoiceId = i.invoiceId
                JOIN 
                    product p ON id.productId = p.productId
                JOIN 
                    category c ON p.categoryId = c.categoryId
                WHERE 
                    i.date BETWEEN ? AND ?
                GROUP BY 
                    c.categoryId, c.categoryName
                ORDER BY 
                    total_revenue DESC
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, java.sql.Date.valueOf(fromDate));
            stmt.setDate(2, java.sql.Date.valueOf(toDate));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String categoryName = rs.getString("categoryName");
                    BigDecimal totalRevenue = rs.getBigDecimal("total_revenue");
                    result.put(categoryName, totalRevenue);
                }
            }
        } catch (SQLException e) {
            log.error("Error executing getRevenueByCategoryStatistics query", e);
            throw new RuntimeException("Lỗi khi truy vấn thống kê doanh thu theo danh mục", e);
        }

        return result;
    }

    @Override
    public List<StatisticDTO> getRevenueStatistics(LocalDate fromDate, LocalDate toDate, String groupBy, String viewType, Connection conn) {
        List<StatisticDTO> result = new ArrayList<>();
        String sql = """
                SELECT
                    %s as period,
                    SUM(total) as amount,
                    COUNT(*) as count
                FROM
                    invoice
                WHERE
                    date BETWEEN ? AND ?
                GROUP BY
                    period
                ORDER BY
                    MIN(date)
                """.formatted(groupBy);

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, java.sql.Date.valueOf(fromDate));
            stmt.setDate(2, java.sql.Date.valueOf(toDate));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String period = rs.getString("period");
                    BigDecimal amount = rs.getBigDecimal("amount");
                    int count = rs.getInt("count");

                    // Format period for display based on viewType
                    String displayPeriod = formatPeriodForDisplay(period, viewType);

                    StatisticDTO dto = new StatisticDTO(displayPeriod, amount, count);
                    result.add(dto);
                }
            }
        } catch (SQLException e) {
            log.error("Error executing getRevenueStatistics query", e);
            throw new RuntimeException("Lỗi khi truy vấn thống kê doanh thu", e);
        }

        return result;
    }

    @Override
    public BigDecimal getDailySales(LocalDate now, Connection connection) {
        String sql = "SELECT SUM(total) FROM invoice WHERE DATE(date) = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDate(1, java.sql.Date.valueOf(now));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1);
                }
            }
        } catch (SQLException e) {
            log.error("Error executing getDailySales query", e);
            throw new RuntimeException("Lỗi khi truy vấn doanh thu hàng ngày", e);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public List<ProductStatisticDTO> getProductSalesStatistics(LocalDate fromDate, LocalDate toDate, String groupBy, String viewType, Connection connection) {
        // public ProductStatisticDTO(Integer productId, String productName,
        //                             String categoryName, int quantity)
        List<ProductStatisticDTO> result = new ArrayList<>();
        String sql = """
                SELECT
                    p.productId,
                    p.productName,
                    c.categoryName,
                    SUM(id.quantity) as quantity
                FROM
                    invoicedetail id
                JOIN
                    invoice i ON id.invoiceId = i.invoiceId
                JOIN
                    product p ON id.productId = p.productId
                JOIN
                    category c ON p.categoryId = c.categoryId
                WHERE
                    i.date BETWEEN ? AND ?
                GROUP BY
                    %s, p.productId, p.productName, c.categoryName
                ORDER BY
                    quantity DESC
                """.formatted(groupBy);
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDate(1, java.sql.Date.valueOf(fromDate));
            stmt.setDate(2, java.sql.Date.valueOf(toDate));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int productId = rs.getInt("productId");
                    String productName = rs.getString("productName");
                    String categoryName = rs.getString("categoryName");
                    int quantity = rs.getInt("quantity");

                    ProductStatisticDTO dto = new ProductStatisticDTO(productId, productName, categoryName, quantity);
                    result.add(dto);
                }
            }
        } catch (SQLException e) {
            log.error("Error executing getProductSalesStatistics query", e);
            throw new RuntimeException("Lỗi khi truy vấn thống kê doanh thu theo sản phẩm", e);
        }
        return result;
    }

    @Override
    public Map<String, Integer> getProductSalesByCategoryStatistics(LocalDate fromDate, LocalDate toDate, Connection connection) {
        Map<String, Integer> result = new HashMap<>();
        String sql = """
                SELECT
                    c.categoryName,
                    SUM(id.quantity) as quantity
                FROM
                    invoicedetail id
                JOIN
                    invoice i ON id.invoiceId = i.invoiceId
                JOIN
                    product p ON id.productId = p.productId
                JOIN
                    category c ON p.categoryId = c.categoryId
                WHERE
                    i.date BETWEEN ? AND ?
                GROUP BY
                    c.categoryName
                ORDER BY
                    quantity DESC
                """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDate(1, java.sql.Date.valueOf(fromDate));
            stmt.setDate(2, java.sql.Date.valueOf(toDate));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String categoryName = rs.getString("categoryName");
                    int quantity = rs.getInt("quantity");
                    result.put(categoryName, quantity);
                }
            }
        } catch (SQLException e) {
            log.error("Error executing getProductSalesByCategoryStatistics query", e);
            throw new RuntimeException("Lỗi khi truy vấn thống kê doanh thu theo danh mục sản phẩm", e);
        }
        return result;
    }

    // Helper method to format period for display
    private String formatPeriodForDisplay(String period, String viewType) {
        try {
            switch (viewType) {
                case "Ngày":
                    // Convert from yyyy-MM-dd to dd/MM/yyyy
                    LocalDate date = LocalDate.parse(period);
                    return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                case "Tuần":
                    // Format for week (yearweek format)
                    String[] parts = period.split("(?<=\\d{4})");
                    return "Tuần " + parts[1] + "/" + parts[0];
                case "Tháng":
                    // Convert from yyyy-MM to MM/yyyy
                    String[] monthParts = period.split("-");
                    return monthParts[1] + "/" + monthParts[0];
                case "Quý":
                    // Quarter format is already "yyyy-Qn"
                    return "Quý " + period.substring(period.length() - 1) + "/" + period.substring(0, 4);
                case "Năm":
                    // Year format is already good
                    return period;
                default:
                    return period;
            }
        } catch (Exception e) {
            // If any parsing error, return the original period
            log.warn("Error formatting period {}: {}", period, e.getMessage());
            return period;
        }
    }
}
