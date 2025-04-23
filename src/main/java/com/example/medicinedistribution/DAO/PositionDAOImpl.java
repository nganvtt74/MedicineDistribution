package com.example.medicinedistribution.DAO;

import com.example.medicinedistribution.DAO.Interface.PositionDAO;
import com.example.medicinedistribution.DTO.PositionDTO;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class PositionDAOImpl implements PositionDAO {
    @Override
    public Integer insert(PositionDTO positionDTO, Connection conn) {
        String sql = "INSERT INTO `position` (positionName, departmentId,Allowance) VALUES (?, ?, ?)";
        try(PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, positionDTO.getPositionName());
            stmt.setInt(2, positionDTO.getDepartmentId());
            stmt.setBigDecimal(3, positionDTO.getAllowance());
            if (stmt.executeUpdate() > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return -1;
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return -1;
    }

    @Override
    public boolean update(PositionDTO positionDTO, Connection conn) {
        String sql = "UPDATE `position` SET positionName = ?, departmentId = ? ,Allowance = ? WHERE positionId = ?";
        try(PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, positionDTO.getPositionName());
            stmt.setInt(2, positionDTO.getDepartmentId());
            stmt.setBigDecimal(3, positionDTO.getAllowance());
            stmt.setInt(4, positionDTO.getPositionId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return false;
    }

    @Override
    public boolean delete(Integer id, Connection conn) {
        String sql = "DELETE FROM `position` WHERE positionId = ?";
        try(PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return false;
    }

    @Override
    public PositionDTO findById(Integer id, Connection conn) {
        String sql = "SELECT * FROM `position` WHERE positionId = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return PositionDTO.builder()
                        .positionId(rs.getInt("positionId"))
                        .positionName(rs.getString("positionName"))
                        .departmentId(rs.getInt("departmentId"))
                        .Allowance(rs.getBigDecimal("Allowance"))
                        .build();
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    @Override
    public List<PositionDTO> findAll(Connection conn) {
        String sql = "SELECT * FROM `position`";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            return getPositionDTOS(stmt);
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    @Override
    public List<PositionDTO> findByDepartmentId(Integer id, Connection conn) {
        String sql = "SELECT * FROM `position` WHERE departmentId = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return getPositionDTOS(stmt);
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    private List<PositionDTO> getPositionDTOS(PreparedStatement stmt) throws SQLException {
        ResultSet rs = stmt.executeQuery();
        List<PositionDTO> positions = new ArrayList<>();
        while (rs.next()) {
            positions.add(PositionDTO.builder()
                    .positionId(rs.getInt("positionId"))
                    .positionName(rs.getString("positionName"))
                    .departmentId(rs.getInt("departmentId"))
                    .Allowance(rs.getBigDecimal("Allowance"))
                    .build());
        }
        return positions;
    }
}
