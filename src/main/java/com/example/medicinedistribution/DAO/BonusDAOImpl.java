// Implementation for BonusDAOImpl
package com.example.medicinedistribution.DAO;

import com.example.medicinedistribution.DAO.Interface.BonusDAO;
import com.example.medicinedistribution.DTO.BonusDTO;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class BonusDAOImpl implements BonusDAO {

    @Override
    public Integer insert(BonusDTO bonus, Connection conn) {
        String sql = "INSERT INTO bonus (employee_id, bonus_type_id, amount, date) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, bonus.getEmployee_id());
            stmt.setInt(2, bonus.getBonus_type_id());
            stmt.setBigDecimal(3, bonus.getAmount());
            stmt.setDate(4, Date.valueOf(bonus.getDate()));

            if (stmt.executeUpdate() > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return null;
        } catch (SQLException e) {
            log.error("Error inserting bonus: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public boolean update(BonusDTO bonus, Connection conn) {
        String sql = "UPDATE bonus SET employee_id = ?, bonus_type_id = ?, amount = ?, date = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bonus.getEmployee_id());
            stmt.setInt(2, bonus.getBonus_type_id());
            stmt.setBigDecimal(3, bonus.getAmount());
            stmt.setDate(4, Date.valueOf(bonus.getDate()));
            stmt.setInt(5, bonus.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("Error updating bonus: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(Integer id, Connection conn) {
        String sql = "DELETE FROM bonus WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("Error deleting bonus: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public BonusDTO findById(Integer id, Connection conn) {
        String sql = "SELECT b.*, e.firstName, e.lastName " +
                "FROM bonus b " +
                "JOIN employee e ON b.employee_id = e.employeeId";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToBonus(rs);
            }
        } catch (SQLException e) {
            log.error("Error finding bonus by ID: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public List<BonusDTO> findAll(Connection conn) {
        String sql = "SELECT b.*, e.firstName, e.lastName " +
                "FROM bonus b " +
                "JOIN employee e ON b.employee_id = e.employeeId";
        List<BonusDTO> bonusList = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                bonusList.add(mapResultSetToBonus(rs));
            }
        } catch (SQLException e) {
            log.error("Error finding all bonuses: {}", e.getMessage());
        }

        return bonusList;
    }

    @Override
    public List<BonusDTO> findByEmployeeId(Integer employeeId, Connection conn) {
        String sql = "SELECT * FROM bonus WHERE employee_id = ?";
        List<BonusDTO> bonusList = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, employeeId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                bonusList.add(mapResultSetToBonus(rs));
            }
        } catch (SQLException e) {
            log.error("Error finding bonuses by employee ID: {}", e.getMessage());
        }

        return bonusList;
    }

    @Override
    public List<BonusDTO> findByDateRange(LocalDate startDate, LocalDate endDate, Connection conn) {
        String sql = "SELECT * FROM bonus WHERE date BETWEEN ? AND ?";
        List<BonusDTO> bonusList = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                bonusList.add(mapResultSetToBonus(rs));
            }
        } catch (SQLException e) {
            log.error("Error finding bonuses by date range: {}", e.getMessage());
        }

        return bonusList;
    }

    @Override
    public List<BonusDTO> getByMothYear(int monthValue, int year, Connection conn) {
        String sql = "SELECT b.*, e.firstName, e.lastName, p.positionName as position_name, " +
                "d.departmentName as department_name, bt.name as bonus_type_name " +
                "FROM bonus b " +
                "JOIN employee e ON b.employee_id = e.employeeId " +
                "JOIN position p ON e.positionId = p.positionId " +
                "JOIN department d ON p.departmentId = d.departmentId " +
                "JOIN bonus_type bt ON b.bonus_type_id = bt.id " +
                "WHERE MONTH(b.date) = ? AND YEAR(b.date) = ?";

        List<BonusDTO> bonusList = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, monthValue);
            stmt.setInt(2, year);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                bonusList.add(mapResultSetToBonusFull(rs));
            }
        } catch (SQLException e) {
            log.error("Error finding bonuses by month and year: {}", e.getMessage());
        }

        return bonusList;
    }

    private BonusDTO mapResultSetToBonus(ResultSet rs) throws SQLException {
        BonusDTO bonus = new BonusDTO();
        bonus.setId(rs.getInt("id"));
        bonus.setEmployee_id(rs.getInt("employee_id"));
        bonus.setBonus_type_id(rs.getInt("bonus_type_id"));
        bonus.setAmount(rs.getBigDecimal("amount"));
        bonus.setDate(rs.getDate("date").toLocalDate());
        String firstName = rs.getString("firstName");
        String lastName = rs.getString("lastName");
        String fullName = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
        bonus.setEmployee_name(fullName.trim());
        return bonus;
    }

    private BonusDTO mapResultSetToBonusFull(ResultSet rs) throws SQLException {
        BonusDTO bonus = new BonusDTO();
        bonus.setId(rs.getInt("id"));
        bonus.setEmployee_id(rs.getInt("employee_id"));
        bonus.setBonus_type_id(rs.getInt("bonus_type_id"));
        bonus.setAmount(rs.getBigDecimal("amount"));
        bonus.setDate(rs.getDate("date").toLocalDate());

        // Get employee name
        String firstName = rs.getString("firstName");
        String lastName = rs.getString("lastName");
        String fullName = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
        bonus.setEmployee_name(fullName.trim());

        // Get position, department and bonus type information
        bonus.setPosition_name(rs.getString("position_name"));
        bonus.setDepartment_name(rs.getString("department_name"));
        bonus.setBonus_type_name(rs.getString("bonus_type_name"));

        return bonus;
    }

}