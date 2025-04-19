package com.example.medicinedistribution.DAO;

import com.example.medicinedistribution.DAO.Interface.GoodsReceiptDAO;
import com.example.medicinedistribution.DTO.GoodsReceiptDTO;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
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
}
