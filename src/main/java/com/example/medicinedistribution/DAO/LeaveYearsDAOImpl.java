package com.example.medicinedistribution.DAO;

import com.example.medicinedistribution.DAO.Interface.LeaveYearsDAO;
import com.example.medicinedistribution.DTO.LeaveYears;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class LeaveYearsDAOImpl implements LeaveYearsDAO {

    @Override
    public Integer insert(LeaveYears leaveYears, Connection conn) {
        String sql = "INSERT INTO leaveyears (employeeId, leaveYear, validLeaveDays) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, leaveYears.getEmployeeId());
            stmt.setDate(2, Date.valueOf(leaveYears.getLeaveYear()));
            stmt.setInt(3, leaveYears.getValidLeaveDays());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            log.error("Error inserting leave year record", e);
        }
        return null;
    }

    @Override
    public boolean update(LeaveYears leaveYears, Connection conn) {
        String sql = "UPDATE leaveyears SET employeeId = ?, leaveYear = ?, validLeaveDays = ? WHERE leaveYearsId = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, leaveYears.getEmployeeId());
            stmt.setDate(2, Date.valueOf(leaveYears.getLeaveYear()));
            stmt.setInt(3, leaveYears.getValidLeaveDays());
            stmt.setInt(4, leaveYears.getLeaveYearsId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("Error updating leave year record", e);
            return false;
        }
    }

    @Override
    public boolean delete(Integer id, Connection conn) {
        String sql = "DELETE FROM leaveyears WHERE leaveYearsId = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("Error deleting leave year record", e);
            return false;
        }
    }

    @Override
    public LeaveYears findById(Integer id, Connection conn) {
        String sql = "SELECT * FROM leaveyears WHERE leaveYearsId = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToLeaveYears(rs);
                }
            }
        } catch (SQLException e) {
            log.error("Error finding leave year by ID", e);
        }
        return null;
    }

    @Override
    public List<LeaveYears> findAll(Connection conn) {
        List<LeaveYears> leaveYearsList = new ArrayList<>();
        String sql = "SELECT * FROM leaveyears";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                leaveYearsList.add(mapResultSetToLeaveYears(rs));
            }
        } catch (SQLException e) {
            log.error("Error finding all leave years", e);
        }
        return leaveYearsList;
    }

    @Override
    public List<LeaveYears> findByEmployeeId(Integer employeeId, Connection conn) {
        List<LeaveYears> leaveYearsList = new ArrayList<>();
        String sql = "SELECT * FROM leaveyears WHERE employeeId = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, employeeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    leaveYearsList.add(mapResultSetToLeaveYears(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error finding leave years for employee", e);
        }
        return leaveYearsList;
    }

    @Override
    public LeaveYears findByEmployeeIdAndYear(Integer employeeId, LocalDate leaveYear, Connection conn) {
        log.info("Finding leave year for employee ID: {} and year: {}", employeeId, leaveYear);
        String sql = "SELECT * FROM leaveyears WHERE employeeId = ? AND leaveYear = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, employeeId);
            stmt.setDate(2, Date.valueOf(leaveYear));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToLeaveYears(rs);
                }
            }
        } catch (SQLException e) {
            log.error("Error finding leave year by employee and year", e);
        }
        return null;
    }

    @Override
    public boolean updateValidLeaveDays(Integer leaveYearsId, Integer validLeaveDays, Connection conn) {
        String sql = "UPDATE leaveyears SET validLeaveDays = ? WHERE leaveYearsId = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, validLeaveDays);
            stmt.setInt(2, leaveYearsId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("Error updating valid leave days", e);
            return false;
        }
    }

    @Override
    public boolean createOrUpdateLeaveYear(LeaveYears leaveYears, Connection conn) {
        LeaveYears existing = findByEmployeeIdAndYear(
                leaveYears.getEmployeeId(), leaveYears.getLeaveYear(), conn);

        if (existing != null) {
            leaveYears.setLeaveYearsId(existing.getLeaveYearsId());
            return update(leaveYears, conn);
        } else {
            return insert(leaveYears, conn) != null;
        }
    }

    @Override
    public List<LeaveYears> findAllCurrentYear(Connection conn) {
        List<LeaveYears> leaveYearsList = new ArrayList<>();
        int currentYear = LocalDate.now().getYear();
        LocalDate startOfYear = LocalDate.of(currentYear, 1, 1);
        LocalDate endOfYear = LocalDate.of(currentYear, 12, 31);

        String sql = "SELECT * FROM leaveyears WHERE leaveYear BETWEEN ? AND ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(startOfYear));
            stmt.setDate(2, Date.valueOf(endOfYear));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    leaveYearsList.add(mapResultSetToLeaveYears(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error finding current year leave records", e);
        }
        return leaveYearsList;
    }

    private LeaveYears mapResultSetToLeaveYears(ResultSet rs) throws SQLException {
        LeaveYears leaveYears = new LeaveYears();
        leaveYears.setLeaveYearsId(rs.getInt("leaveYearsId"));
        leaveYears.setEmployeeId(rs.getInt("employeeId"));
        leaveYears.setLeaveYear(rs.getDate("leaveYear").toLocalDate());
        leaveYears.setValidLeaveDays(rs.getInt("validLeaveDays"));
        return leaveYears;
    }
}