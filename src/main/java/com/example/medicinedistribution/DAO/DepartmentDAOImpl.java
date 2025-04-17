package com.example.medicinedistribution.DAO;

import com.example.medicinedistribution.DAO.Interface.DepartmentDAO;
import com.example.medicinedistribution.DTO.DepartmentDTO;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
@Slf4j
public class DepartmentDAOImpl implements DepartmentDAO {
    @Override
    public Integer insert(DepartmentDTO departmentDTO, Connection conn) {
        String sql = "INSERT INTO department (departmentName) VALUES (?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, departmentDTO.getDepartmentName());
            if (stmt.executeUpdate() > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return -1;
    }

    @Override
    public boolean update(DepartmentDTO departmentDTO, Connection conn) {
        String sql = "UPDATE department SET departmentName = ? WHERE departmentId = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, departmentDTO.getDepartmentName());
            stmt.setInt(2, departmentDTO.getDepartmentId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return false;
    }

    @Override
    public boolean delete(Integer id, Connection conn) {
        String sql = "DELETE FROM department WHERE departmentId = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return false;
    }

    @Override
    public DepartmentDTO findById(Integer id, Connection conn) {
        String sql = "SELECT * FROM department WHERE departmentId = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return DepartmentDTO.builder()
                        .departmentId(rs.getInt("departmentId"))
                        .departmentName(rs.getString("departmentName"))
                        .build();
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    @Override
    public List<DepartmentDTO> findAll(Connection conn) {
        String sql = "SELECT * FROM department";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            List<DepartmentDTO> departments = new ArrayList<>();
            while (rs.next()) {
                departments.add(DepartmentDTO.builder()
                        .departmentId(rs.getInt("departmentId"))
                        .departmentName(rs.getString("departmentName"))
                        .build());
            }
            return departments;
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return null;
    }
}
