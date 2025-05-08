package com.example.medicinedistribution.DAO;

import com.example.medicinedistribution.DAO.Interface.GoodsReceiptDetailDAO;
import com.example.medicinedistribution.DTO.GoodsReceiptDetailDTO;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class GoodsReceiptDetailDAOImpl implements GoodsReceiptDetailDAO {

    @Override
    public Integer insert(GoodsReceiptDetailDTO goodsReceiptDetailDTO, Connection conn) {
        String sql = "INSERT INTO goodsreceiptdetail (goodsReceiptId, productId, price, quantity, total) VALUES (? ,? , ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, goodsReceiptDetailDTO.getGoodsReceiptId());
            stmt.setInt(2, goodsReceiptDetailDTO.getProductId());
            stmt.setBigDecimal(3, goodsReceiptDetailDTO.getPrice());
            stmt.setInt(4, goodsReceiptDetailDTO.getQuantity());
            stmt.setBigDecimal(5, goodsReceiptDetailDTO.getTotal());

            return stmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Lỗi khi thêm hóa đơn nhập: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public boolean update(GoodsReceiptDetailDTO goodsReceiptDetailDTO, Connection conn) {
        throw new UnsupportedOperationException("Không hỗ trợ cập nhật hóa đơn nhập");
    }

    @Override
    public boolean delete(Integer integer, Connection conn) {
        String sql = "DELETE FROM goodsreceiptdetail WHERE goodsReceiptId = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, integer);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return false;
    }

    @Override
    public GoodsReceiptDetailDTO findById(Integer integer, Connection conn) {
        throw new UnsupportedOperationException("Không hỗ trợ tìm hóa đơn nhập theo ID");
    }

    @Override
    public List<GoodsReceiptDetailDTO> findAll(Connection conn) {
        throw new UnsupportedOperationException("Không hỗ trợ tìm tất cả hóa đơn nhập");
    }
 public List<GoodsReceiptDetailDTO> findByGoodsReceiptId(Integer goodsReceiptId, Connection conn) {
     String sql = "SELECT d.*, p.productName, p.unit FROM goodsreceiptdetail d " +
                  "JOIN product p ON d.productId = p.productId " +
                  "WHERE d.goodsReceiptId = ?";

     try (PreparedStatement stmt = conn.prepareStatement(sql)) {
         stmt.setInt(1, goodsReceiptId);
         try (ResultSet rs = stmt.executeQuery()) {
             List<GoodsReceiptDetailDTO> goodsReceiptDetailDTOList = new ArrayList<>();
             while (rs.next()) {
                 goodsReceiptDetailDTOList.add(GoodsReceiptDetailDTO.builder()
                         .goodsReceiptId(rs.getInt("goodsReceiptId"))
                         .productId(rs.getInt("productId"))
                         .productName(rs.getString("productName"))
                         .unit(rs.getString("unit"))
                         .price(rs.getBigDecimal("price"))
                         .quantity(rs.getInt("quantity"))
                         .total(rs.getBigDecimal("total"))
                         .build());
             }
             return goodsReceiptDetailDTOList;
         }
     } catch (SQLException e) {
         log.error("Lỗi khi tìm chi tiết phiếu nhập: {}", e.getMessage());
     }
     return null;
 }
}
