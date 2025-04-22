package com.example.medicinedistribution.DAO;

import com.example.medicinedistribution.DAO.Interface.PositionHistoryDAO;
import com.example.medicinedistribution.DTO.PositionHistoryDTO;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
@Slf4j
public class PositionHistoryDAOImpl implements PositionHistoryDAO {
    @Override
    public Integer insert(PositionHistoryDTO positionHistoryDTO, Connection conn) {
        String sql = "INSERT INTO position_history (date, position_name, employee_id, salary_before, salary_after) VALUES (?, ?, ?, ?, ?)";
        try(PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, java.sql.Date.valueOf(LocalDate.now()));
            stmt.setString(2, positionHistoryDTO.getPositionName());
            stmt.setInt(3, positionHistoryDTO.getEmployeeId());
            stmt.setBigDecimal(4, positionHistoryDTO.getSalaryBefore());
            stmt.setBigDecimal(5, positionHistoryDTO.getSalaryAfter());
            return stmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Error inserting PositionHistoryDTO: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public boolean update(PositionHistoryDTO positionHistoryDTO, Connection conn) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean delete(Integer integer, Connection conn) {
        String sql = "DELETE FROM position_history WHERE employee_id = ?";
        try(PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, integer);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("Error deleting PositionHistoryDTO: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public PositionHistoryDTO findById(Integer integer, Connection conn) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<PositionHistoryDTO> findAll(Connection conn) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<PositionHistoryDTO> findByEmployeeId(Integer employeeId, Connection conn) {
        String sql = "SELECT * FROM position_history WHERE employee_id = ?";
        try(PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, employeeId);
            ResultSet rs = stmt.executeQuery();
            List<PositionHistoryDTO> positionHistoryList = new ArrayList<>();
            while (rs.next()) {
                PositionHistoryDTO positionHistoryDTO = new PositionHistoryDTO();
                positionHistoryDTO.setId(rs.getInt("id"));
                positionHistoryDTO.setDate(rs.getDate("date").toLocalDate());
                positionHistoryDTO.setPositionName(rs.getString("position_name"));
                positionHistoryDTO.setEmployeeId(rs.getInt("employee_id"));
                positionHistoryDTO.setSalaryBefore(rs.getBigDecimal("salary_before"));
                positionHistoryDTO.setSalaryAfter(rs.getBigDecimal("salary_after"));
                positionHistoryList.add(positionHistoryDTO);
            }
            return positionHistoryList;
        } catch (SQLException e) {
            log.error("Error finding PositionHistoryDTO by employeeId: {}", e.getMessage());
            return null;
        }
    }
}
