package com.example.medicinedistribution.DAO;

import com.example.medicinedistribution.DAO.Interface.GoodsReceiptDAO;
import com.example.medicinedistribution.DTO.GoodsReceiptDTO;
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
public class GoodsReceiptDAOImpl implements GoodsReceiptDAO {

    @Override
    public Integer insert(GoodsReceiptDTO goodsReceiptDTO, Connection conn) {
        String sql ="INSERT INTO goodsreceipt (employeeId, manufacturerId, date, total) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, goodsReceiptDTO.getEmployeeId());
            stmt.setInt(2, goodsReceiptDTO.getManufacturerId());
            stmt.setDate(3, Date.valueOf(goodsReceiptDTO.getDate()));
            stmt.setBigDecimal(4, goodsReceiptDTO.getTotal());

            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            log.error("Lỗi khi thêm hóa đơn nhập: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public boolean update(GoodsReceiptDTO goodsReceiptDTO, Connection conn) {
        throw new UnsupportedOperationException("Không hỗ trợ cập nhật hóa đơn nhập");
    }

    @Override
    public boolean delete(Integer integer, Connection conn) {
        String sql = "DELETE FROM goodsreceipt WHERE goodsReceiptId = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, integer);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return false;
    }

    @Override
    public GoodsReceiptDTO findById(Integer integer, Connection conn) {
        String sql = "SELECT * FROM goodsreceipt WHERE goodsReceiptId = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, integer);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return GoodsReceiptDTO.builder()
                            .goodsReceiptId(rs.getInt("goodsReceiptId"))
                            .employeeId(rs.getInt("employeeId"))
                            .manufacturerId(rs.getInt("manufacturerId"))
                            .date(rs.getDate("date").toLocalDate())
                            .total(rs.getBigDecimal("total"))
                            .build();
                }
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    @Override
    public List<GoodsReceiptDTO> findAll(Connection conn) {
        String sql = "SELECT * FROM goodsreceipt";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            List<GoodsReceiptDTO> goodsReceipts = new ArrayList<>();
            while (rs.next()) {
                goodsReceipts.add(GoodsReceiptDTO.builder()
                        .goodsReceiptId(rs.getInt("goodsReceiptId"))
                        .employeeId(rs.getInt("employeeId"))
                        .manufacturerId(rs.getInt("manufacturerId"))
                        .date(rs.getDate("date").toLocalDate())
                        .total(rs.getBigDecimal("total"))
                        .build());
            }
            return goodsReceipts;
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    @Override
    public List<StatisticDTO> getExpenseStatistics(LocalDate fromDate, LocalDate toDate, String groupBy, String viewType ,Connection conn) {
        List<StatisticDTO> result = new ArrayList<>();
        String sql = """
                SELECT 
                    %s as period,
                    SUM(total) as amount,
                    COUNT(*) as count
                FROM 
                    goodsreceipt
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
            log.error("Error executing getExpenseStatistics query", e);
            throw new RuntimeException("Lỗi khi truy vấn thống kê chi phí", e);
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

    @Override
    public Map<String, BigDecimal> getExpenseByManufacturerStatistics(LocalDate fromDate, LocalDate toDate , Connection conn) {
        Map<String, BigDecimal> result = new HashMap<>();
        String sql = """
            SELECT
                m.manufacturerName,
                SUM(grd.price * grd.quantity) as total_expense
            FROM
                goodsreceiptdetail grd
            JOIN
                goodsreceipt gr ON grd.goodsReceiptId = gr.goodsReceiptId
            JOIN
                manufacturer m ON gr.manufacturerId = m.manufacturerId
            WHERE\s
                gr.date BETWEEN ? AND ?
            GROUP BY
                m.manufacturerId, m.manufacturerName
            ORDER BY
                total_expense DESC
           \s""";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, java.sql.Date.valueOf(fromDate));
            stmt.setDate(2, java.sql.Date.valueOf(toDate));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String manufacturerName = rs.getString("manufacturerName");
                    BigDecimal totalExpense = rs.getBigDecimal("total_expense");

                    result.put(manufacturerName, totalExpense);
                }
            }
        } catch (SQLException e) {
            log.error("Error executing getExpenseByManufacturerStatistics query", e);
            throw new RuntimeException("Lỗi khi truy vấn thống kê chi phí theo nhà sản xuất", e);
        }

        return result;
    }
}
