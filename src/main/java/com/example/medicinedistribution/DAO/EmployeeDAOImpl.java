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
        String sql = "INSERT INTO employee (firstName, lastName, birthday, gender, phone, email, hireDate, address, basic_salary, status, positionId) VALUES (?, ?, ?, ?, ?, ?, ?,null,null,null,null)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            Statement(employeeDTO, stmt);
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
                 " address = ? WHERE employeeId = ?";

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        Statement(employeeDTO, stmt);
        stmt.setInt(8, employeeDTO.getEmployeeId());
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
        stmt.setString(7, employeeDTO.getAddress());
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
                        .birthday(rs.getDate("birthday")==null? null : rs.getDate("birthday").toLocalDate())
                        .hireDate(rs.getDate("hireDate")==null? null : rs.getDate("hireDate").toLocalDate())
                        .status(rs.getInt("status"))
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
                        .birthday(rs.getDate("birthday")==null? null : rs.getDate("birthday").toLocalDate())
                        .hireDate(rs.getDate("hireDate")==null? null : rs.getDate("hireDate").toLocalDate())
                        .status(rs.getInt("status"))
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

    @Override
    public List<EmployeeDTO> getEmployeeWithoutAccount(Connection conn) {
        String sql = "SELECT * FROM employee WHERE employeeId NOT IN (SELECT employeeId FROM account)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.execute();
            List<EmployeeDTO> employees = new ArrayList<>();
            ResultSet rs = stmt.getResultSet();
            while (rs.next()) {
                employees.add(EmployeeDTO.builder()
                        .employeeId(rs.getInt("employeeId"))
                        .firstName(rs.getString("firstName"))
                        .lastName(rs.getString("lastName"))
                        .basicSalary(rs.getBigDecimal("basic_salary"))
                        .phone(rs.getString("phone"))
                        .birthday(rs.getDate("birthday").toLocalDate())
                        .hireDate(rs.getDate("hireDate").toLocalDate())
                        .status(rs.getInt("status"))
                        .positionId(rs.getInt("positionId"))
                        .address(rs.getString("address"))
                        .email(rs.getString("email"))
                        .gender(rs.getString("gender"))
                        .build());
            }
            return employees;
        } catch (SQLException e) {
            log.error("Error getting employees without accounts: {}", e.getMessage());
        }
        return new ArrayList<>(); // Return empty list instead of null when an exception occurs
    }

    @Override
    public boolean updateEmploymentInfo(EmployeeDTO employee, Connection conn) {
        String sql = "UPDATE employee SET positionId = ?, basic_salary = ? , hireDate = ? , status = ? WHERE employeeId = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, employee.getPositionId());
            stmt.setBigDecimal(2, employee.getBasicSalary());
            stmt.setDate(3, Date.valueOf(employee.getHireDate()));
            stmt.setInt(4, employee.getStatus());
            stmt.setInt(5, employee.getEmployeeId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("Error updating employment info: {}", e.getMessage());
        }
        return false;
    }
}
