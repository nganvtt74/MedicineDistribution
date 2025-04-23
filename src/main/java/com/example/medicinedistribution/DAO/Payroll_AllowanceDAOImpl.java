package com.example.medicinedistribution.DAO;

import com.example.medicinedistribution.DAO.Interface.Payroll_AllowanceDAO;
import com.example.medicinedistribution.DTO.Payroll_AllowanceDTO;
import lombok.extern.slf4j.Slf4j;


import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class Payroll_AllowanceDAOImpl implements Payroll_AllowanceDAO {

    /**
     * Add a new payroll allowance record to the database
     * @param payrollAllowance The payroll allowance to add
     * @param connection Database connection
     * @return true if successful, false otherwise
     */
    @Override
    public boolean add(Payroll_AllowanceDTO payrollAllowance, Connection connection) {
        String query = "INSERT INTO payroll_allowance (payroll_id, allowance_id, amount) VALUES (?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, payrollAllowance.getPayroll_id());
            statement.setInt(2, payrollAllowance.getAllowance_id());
            statement.setBigDecimal(3, payrollAllowance.getAmount());

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Add multiple payroll allowance records in a batch
     * @param payrollAllowances List of payroll allowances to add
     * @param connection Database connection
     * @return true if all records were added successfully, false otherwise
     */
    @Override
    public boolean addBatch(List<Payroll_AllowanceDTO> payrollAllowances, Connection connection) {
        String query = "INSERT INTO payroll_allowance (payroll_id, allowance_id, amount) VALUES (?, ?, ?)";
        boolean autoCommitState = false;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            // Store the current auto-commit state
            autoCommitState = connection.getAutoCommit();
            connection.setAutoCommit(false);

            for (Payroll_AllowanceDTO payrollAllowance : payrollAllowances) {
                statement.setInt(1, payrollAllowance.getPayroll_id());
                statement.setInt(2, payrollAllowance.getAllowance_id());
                statement.setBigDecimal(3, payrollAllowance.getAmount());
                statement.addBatch();
            }

            int[] results = statement.executeBatch();
            connection.commit();

            // Check if all operations were successful
            for (int result : results) {
                if (result <= 0) {
                    return false;
                }
            }
            return true;
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                // Restore the original auto-commit state
                connection.setAutoCommit(autoCommitState);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Get all allowances for a specific payroll ID
     * @param payrollId ID of the payroll to get allowances for
     * @param connection Database connection
     * @return List of payroll allowances
     */
    @Override
    public List<Payroll_AllowanceDTO> getByPayrollId(int payrollId, Connection connection) {
        List<Payroll_AllowanceDTO> payrollAllowances = new ArrayList<>();
        String query = "SELECT payroll_id, allowance_id, amount FROM payroll_allowance WHERE payroll_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, payrollId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Payroll_AllowanceDTO payrollAllowance = Payroll_AllowanceDTO.builder()
                            .allowance_id(resultSet.getInt("allowance_id"))
                            .payroll_id(resultSet.getInt("payroll_id"))
                            .amount(resultSet.getBigDecimal("amount"))
                            .build();

                    payrollAllowances.add(payrollAllowance);
                }
            }
        } catch (SQLException e) {
            log.error("Error fetching payroll allowances: {}", e.getMessage());
        }

        return payrollAllowances;
    }

    /**
     * Delete all allowances for a specific payroll ID
     * @param payrollId ID of the payroll to delete allowances for
     * @param connection Database connection
     * @return true if successful, false otherwise
     */
    @Override
    public boolean deleteByPayrollId(int payrollId, Connection connection) {
        String query = "DELETE FROM payroll_allowance WHERE payroll_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, payrollId);

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            log.error("Error deleting payroll allowances: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Update an existing payroll allowance record
     * @param payrollAllowance The payroll allowance with updated values
     * @param connection Database connection
     * @return true if successful, false otherwise
     */
    @Override
    public boolean update(Payroll_AllowanceDTO payrollAllowance, Connection connection) {
        String query = "UPDATE payroll_allowance SET amount = ? WHERE payroll_id = ? AND allowance_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setBigDecimal(1, payrollAllowance.getAmount());
            statement.setInt(2, payrollAllowance.getPayroll_id());
            statement.setInt(3, payrollAllowance.getAllowance_id());

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            log.error("Error updating payroll allowance: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean insertPayroll_Allowance(Integer payrollId, BigDecimal mealAllowance, BigDecimal gasAllowance, BigDecimal phoneAllowance, BigDecimal responsibilityAllowance, Connection connection) {
        String query = "INSERT INTO payroll_allowance (payroll_id, allowance_id, amount) VALUES (?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, payrollId);
            statement.setInt(2, 1); // Assuming 1 is the ID for meal allowance
            statement.setBigDecimal(3, mealAllowance);
            statement.executeUpdate();

            statement.setInt(2, 2); // Assuming 2 is the ID for gas allowance
            statement.setBigDecimal(3, gasAllowance);
            statement.executeUpdate();

            statement.setInt(2, 3); // Assuming 3 is the ID for phone allowance
            statement.setBigDecimal(3, phoneAllowance);
            statement.executeUpdate();

            statement.setInt(2, 4); // Assuming 4 is the ID for responsibility allowance
            statement.setBigDecimal(3, responsibilityAllowance);
            statement.executeUpdate();

            return true;
        } catch (SQLException e) {
            log.error("Error inserting payroll allowances: {}", e.getMessage());
            return false;
        }
    }
}