package com.example.medicinedistribution.DAO;

import com.example.medicinedistribution.DAO.Interface.EmployeeDAO;
import com.example.medicinedistribution.DTO.EmployeeDTO;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class EmployeeDAOImpl implements EmployeeDAO {

    @Override
    public Integer insert(EmployeeDTO employeeDTO, Connection conn) {
        String sql = "INSERT INTO employee (firstName, lastName, birthday, gender, phone, email, hireDate, address" +
                ", basic_salary, status, positionId, accountId) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            Statement(employeeDTO, stmt);
            stmt.setInt(11, employeeDTO.getPositionId());
            stmt.setInt(12, employeeDTO.getAccountId());
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
public boolean update(EmployeeDTO employeeDTO, Connection conn) {
    String sql = "UPDATE employee SET firstName = ?, lastName = ?, birthday = ?, gender = ?, phone = ?, email = ?, " +
                 "hireDate = ?, address = ?, basic_salary = ?, status = ?,  accountId = ? WHERE employeeId = ?";

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        Statement(employeeDTO, stmt);
        stmt.setInt(11, employeeDTO.getAccountId());
        stmt.setInt(12, employeeDTO.getEmployeeId());
        return stmt.executeUpdate() > 0;
    } catch (SQLException e) {
        log.error(e.getMessage());
    }
    return false;
}

    private void Statement(EmployeeDTO employeeDTO, PreparedStatement stmt) throws SQLException {
        stmt.setString(1, employeeDTO.getFirstName());
        stmt.setString(2, employeeDTO.getLastName());
        stmt.setDate(3, Date.valueOf(employeeDTO.getBirthday()));
        stmt.setString(4, employeeDTO.getGender());
        stmt.setString(5, employeeDTO.getPhone());
        stmt.setString(6, employeeDTO.getEmail());
        stmt.setDate(7, Date.valueOf(employeeDTO.getHireDate()));
        stmt.setString(8, employeeDTO.getAddress());
        stmt.setBigDecimal(9, employeeDTO.getBasicSalary());
        stmt.setString(10, employeeDTO.getStatus());
    }

    @Override
    public boolean delete(Integer integer, Connection conn) {
        String sql = "DELETE FROM employee WHERE employeeId = ?";
        try(PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setInt(1, integer);
            return stmt.executeUpdate() > 0;
        }catch (SQLException e) {
            log.error(e.getMessage());
        }
        return false;
    }


    @Override
    public EmployeeDTO findById(Integer integer, Connection conn) {
        String sql = "SELECT * FROM employee WHERE employeeId = ?";
        try(PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setInt(1, integer);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return EmployeeDTO.builder()
                        .employeeId(rs.getInt("employeeId"))
                        .firstName(rs.getString("firstName"))
                        .lastName(rs.getString("lastName"))
                        .basicSalary(rs.getBigDecimal("basic_salary"))
                        .phone(rs.getString("phone"))
                        .birthday(rs.getDate("birthday").toLocalDate())
                        .hireDate(rs.getDate("hireDate").toLocalDate())
                        .status(rs.getString("status"))
                        .accountId(rs.getInt("accountId"))
                        .positionId(rs.getInt("positionId"))
                        .address(rs.getString("address"))
                        .email(rs.getString("email"))
                        .gender(rs.getString("gender"))
                        .build();
            } else {
                log.error("No employee found with ID: {}", integer);
                return null;
            }
        }catch (SQLException e) {
            log.error("Error finding employee by ID: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public List<EmployeeDTO> findAll(Connection conn) {
        String sql = "SELECT * FROM employee";
        try(PreparedStatement stmt = conn.prepareStatement(sql)){
            List<EmployeeDTO> employees = new ArrayList<>();
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                employees.add(EmployeeDTO.builder()
                        .employeeId(rs.getInt("employeeId"))
                        .firstName(rs.getString("firstName"))
                        .lastName(rs.getString("lastName"))
                        .basicSalary(rs.getBigDecimal("basic_salary"))
                        .phone(rs.getString("phone"))
                        .birthday(rs.getDate("birthday").toLocalDate())
                        .hireDate(rs.getDate("hireDate").toLocalDate())
                        .status(rs.getString("status"))
                        .accountId(rs.getInt("accountId"))
                        .positionId(rs.getInt("positionId"))
                        .address(rs.getString("address"))
                        .email(rs.getString("email"))
                        .gender(rs.getString("gender"))
                        .build());
            }
            return employees;
        }catch (SQLException e) {
            log.error(e.getMessage());
        }
        return null;
    }
}
