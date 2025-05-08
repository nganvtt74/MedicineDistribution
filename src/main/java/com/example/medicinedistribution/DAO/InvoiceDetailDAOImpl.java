package com.example.medicinedistribution.DAO;

import com.example.medicinedistribution.DAO.Interface.InvoiceDetailDAO;
import com.example.medicinedistribution.DTO.InvoiceDetailDTO;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
@Slf4j
public class InvoiceDetailDAOImpl implements InvoiceDetailDAO {

    @Override
    public Integer insert(InvoiceDetailDTO invoiceDetailDTO, Connection conn) {
        String sql = "INSERT INTO invoicedetail (invoiceId, productId, quantity, price, total) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, invoiceDetailDTO.getInvoiceId());
            stmt.setInt(2, invoiceDetailDTO.getProductId());
            stmt.setInt(3, invoiceDetailDTO.getQuantity());
            stmt.setBigDecimal(4, invoiceDetailDTO.getPrice());
            stmt.setBigDecimal(5, invoiceDetailDTO.getTotal());
            return stmt.executeUpdate();
        } catch (Exception e) {
            log.error("Lỗi khi thêm hóa đơn: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public boolean update(InvoiceDetailDTO invoiceDetailDTO, Connection conn) {
        throw new UnsupportedOperationException("Không hỗ trợ cập nhật hóa đơn");
    }

    @Override
    public boolean delete(Integer integer, Connection conn) {
        String sql = "DELETE FROM invoicedetail WHERE invoiceId = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, integer);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return false;
    }

    @Override
    public InvoiceDetailDTO findById(Integer integer, Connection conn) {
        throw new UnsupportedOperationException("Không hỗ trợ tìm hóa đơn theo ID");
    }

    @Override
    public List<InvoiceDetailDTO> findAll(Connection conn) {
        throw new UnsupportedOperationException("Không hỗ trợ tìm tất cả hóa đơn");
    }

    @Override
    public List<InvoiceDetailDTO> findByInvoiceId(Integer invoiceId, Connection conn){
        String sql = "SELECT i.*, p.productName, p.unit FROM invoicedetail i " +
                "JOIN product p ON i.productId = p.productId " +
                "WHERE i.invoiceId = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, invoiceId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<InvoiceDetailDTO> invoiceDetails = new ArrayList<>();
                while (rs.next()) {
                    InvoiceDetailDTO invoiceDetailDTO = InvoiceDetailDTO.builder()
                            .invoiceId(rs.getInt("invoiceId"))
                            .productId(rs.getInt("productId"))
                            .productName(rs.getString("productName"))
                            .quantity(rs.getInt("quantity"))
                            .price(rs.getBigDecimal("price"))
                            .unit(rs.getString("unit"))
                            .total(rs.getBigDecimal("total"))
                            .build();
                    invoiceDetails.add(invoiceDetailDTO);
                }
                return invoiceDetails;
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }
}
