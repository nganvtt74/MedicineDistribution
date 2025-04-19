package com.example.medicinedistribution.DAO;

import com.example.medicinedistribution.DAO.Interface.InvoiceDAO;
import com.example.medicinedistribution.DTO.InvoiceDTO;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

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
}
