package com.example.medicinedistribution.DAO;

import com.example.medicinedistribution.DAO.Interface.DependentsDAO;
import com.example.medicinedistribution.DTO.DependentsDTO;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DependentsDAOImpl implements DependentsDAO {

    @Override
    public boolean insert(DependentsDTO dependentsDTO, Connection connection) {
        String sql = "INSERT INTO Dependents (EmployeeID, dependentsNo, FirstName, LastName, Birthday, Relationship) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, dependentsDTO.getEmployeeId());
            statement.setInt(2, getNextDependentNo(dependentsDTO.getEmployeeId(), connection));
            statement.setString(3, dependentsDTO.getFirstName());
            statement.setString(4, dependentsDTO.getLastName());
            statement.setDate(5, dependentsDTO.getBirthday() != null ?
                    Date.valueOf(dependentsDTO.getBirthday()) : null);
            statement.setString(6, dependentsDTO.getRelationship());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("Error inserting dependent: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean update(DependentsDTO dependentsDTO, Connection connection) {
            String sql = "UPDATE Dependents SET firstName = ?, lastName = ?, birthday = ?, " +
                    "relationship = ? WHERE employeeId = ? AND dependentsNo = ?";

        log.info("Updating dependent: {}", sql);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, dependentsDTO.getFirstName());
            statement.setString(2, dependentsDTO.getLastName());
            statement.setDate(3, dependentsDTO.getBirthday() != null ?
                    Date.valueOf(dependentsDTO.getBirthday()) : null);
            statement.setString(4, dependentsDTO.getRelationship());
            statement.setInt(5, dependentsDTO.getEmployeeId());
            statement.setInt(6, dependentsDTO.getDependentNo());
            log.info("Executing update statement: {}", statement);
            boolean result = statement.executeUpdate() > 0;
            log.info("Update result: {}", result);
            return result;
        } catch (SQLException e) {
            log.error("Error updating dependent: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean delete(Integer employeeId, Integer dependentNo, Connection connection) {
        String sql = "DELETE FROM Dependents WHERE EmployeeID = ? AND dependentsNo = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, employeeId);
            statement.setInt(2, dependentNo);

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("Error deleting dependent: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public List<DependentsDTO> findByEmployeeId(Integer employeeId, Connection connection) {
        String sql = "SELECT EmployeeID, dependentsNo, FirstName, LastName, Birthday, Relationship " +
                "FROM Dependents WHERE EmployeeID = ? ORDER BY dependentsNo";
        List<DependentsDTO> dependentsList = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, employeeId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    DependentsDTO dependent = mapResultSetToDTO(resultSet);
                    dependentsList.add(dependent);
                }
            }
        } catch (SQLException e) {
            log.error("Error finding dependents by employee ID: {}", e.getMessage(), e);
        }

        return dependentsList;
    }

    /**
     * Helper method to map a ResultSet row to a DependentsDTO object
     */
    private DependentsDTO mapResultSetToDTO(ResultSet rs) throws SQLException {
        DependentsDTO dto = new DependentsDTO();
        dto.setEmployeeId(rs.getInt("employeeId"));
        dto.setDependentNo(rs.getInt("dependentsNo"));
        dto.setFirstName(rs.getString("firstName"));
        dto.setLastName(rs.getString("lastName"));

        Date birthday = rs.getDate("birthday");
        if (birthday != null) {
            dto.setBirthday(birthday.toLocalDate());
        }

        dto.setRelationship(rs.getString("relationship"));

        return dto;
    }

    /**
     * Helper method to determine the next available dependent number for an employee
     */
    private int getNextDependentNo(int employeeId, Connection connection) throws SQLException {
        String sql = "SELECT COALESCE(MAX(dependentsNo), 0) + 1 AS NextNo FROM Dependents WHERE EmployeeID = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, employeeId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("NextNo");
                }
                return 1; // Default to 1 if no dependents exist
            }
        }
    }

    /**
     * Find dependent by composite key
     */
    public DependentsDTO findById(Integer employeeId, Integer dependentNo, Connection connection) {
        String sql = "SELECT EmployeeID, dependentsNo, FirstName, LastName, Birthday, Relationship " +
                "FROM Dependents WHERE EmployeeID = ? AND dependentsNo = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, employeeId);
            statement.setInt(2, dependentNo);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToDTO(resultSet);
                }
            }
        } catch (SQLException e) {
            log.error("Error finding dependent by ID: {}", e.getMessage(), e);
        }

        return null;
    }

    /**
     * Delete all dependents for an employee
     */
    public boolean deleteByEmployeeId(Integer employeeId, Connection connection) {
        String sql = "DELETE FROM Dependents WHERE EmployeeID = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, employeeId);

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("Error deleting all dependents for employee: {}", e.getMessage(), e);
            return false;
        }
    }
}