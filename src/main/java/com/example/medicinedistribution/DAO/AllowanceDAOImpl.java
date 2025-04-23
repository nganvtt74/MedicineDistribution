package com.example.medicinedistribution.DAO;

import com.example.medicinedistribution.DAO.Interface.AllowanceDAO;
import com.example.medicinedistribution.DTO.AllowanceDTO;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class AllowanceDAOImpl implements AllowanceDAO {

    @Override
    public Integer insert(AllowanceDTO allowance, Connection conn) {
        String sql = "INSERT INTO allowance (name, amount, is_insurance_included) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, allowance.getName());
            stmt.setBigDecimal(2, allowance.getAmount());
            stmt.setBoolean(3, allowance.getIs_insurance_included());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                log.error("Creating allowance failed, no rows affected.");
                return null;
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    log.error("Creating allowance failed, no ID obtained.");
                    return null;
                }
            }
        } catch (SQLException e) {
            log.error("Error inserting allowance: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public boolean update(AllowanceDTO allowance, Connection conn) {
        String sql = "UPDATE allowance SET name = ?, amount = ?, is_insurance_included = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, allowance.getName());
            stmt.setBigDecimal(2, allowance.getAmount());
            stmt.setBoolean(3, allowance.getIs_insurance_included());
            stmt.setInt(4, allowance.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("Error updating allowance: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(Integer id, Connection conn) {
        String sql = "DELETE FROM allowance WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("Error deleting allowance: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public AllowanceDTO findById(Integer id, Connection conn) {
        String sql = "SELECT * FROM allowance WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToAllowance(rs);
            }
        } catch (SQLException e) {
            log.error("Error finding allowance by ID: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public List<AllowanceDTO> findAll(Connection conn) {
        String sql = "SELECT * FROM allowance";
        List<AllowanceDTO> allowances = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                allowances.add(mapResultSetToAllowance(rs));
            }
        } catch (SQLException e) {
            log.error("Error finding all allowances: {}", e.getMessage());
        }

        return allowances;
    }

    private AllowanceDTO mapResultSetToAllowance(ResultSet rs) throws SQLException {
        return AllowanceDTO.builder()
                .id(rs.getInt("id"))
                .name(rs.getString("name"))
                .amount(rs.getBigDecimal("amount"))
                .is_insurance_included(rs.getBoolean("is_insurance_included"))
                .build();
    }
}