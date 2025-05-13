package com.example.medicinedistribution.DAO;

import com.example.medicinedistribution.DAO.Interface.PayrollDAO;
import com.example.medicinedistribution.DTO.PayrollDTO;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class PayrollDAOImpl implements PayrollDAO {
@Override
public boolean insertPayroll(List<PayrollDTO> payrollDTOList, Connection conn) {
    String sql = "INSERT INTO payroll (employeeId, payrollDate, actual_working_days, leave_days, late_days," +
            " position_allowance, other_allowance, total_allowance, bonus_total, taxable_income, " +
            "social_insurance_salary, insurance_social, insurance_health, insurance_accident, " +
            "total_insurance, income_tax, deductible_income, penalty_amount, net_income, created_by,base_salary) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?) " +
            "ON DUPLICATE KEY UPDATE " +
            "actual_working_days = VALUES(actual_working_days), " +
            "leave_days = VALUES(leave_days), " +
            "late_days = VALUES(late_days), " +
            "position_allowance = VALUES(position_allowance), " +
            "other_allowance = VALUES(other_allowance), " +
            "total_allowance = VALUES(total_allowance), " +
            "bonus_total = VALUES(bonus_total), " +
            "taxable_income = VALUES(taxable_income), " +
            "social_insurance_salary = VALUES(social_insurance_salary), " +
            "insurance_social = VALUES(insurance_social), " +
            "insurance_health = VALUES(insurance_health), " +
            "insurance_accident = VALUES(insurance_accident), " +
            "total_insurance = VALUES(total_insurance), " +
            "income_tax = VALUES(income_tax), " +
            "deductible_income = VALUES(deductible_income), " +
            "penalty_amount = VALUES(penalty_amount), " +
            "net_income = VALUES(net_income), " +
            "created_by = VALUES(created_by), " +
            "base_salary = VALUES(base_salary)";

    try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        for (PayrollDTO payroll : payrollDTOList) {
            int i = 1;
            pstmt.setInt(i++, payroll.getEmployeeId());
            pstmt.setDate(i++, java.sql.Date.valueOf(payroll.getPayrollDate()));
            pstmt.setInt(i++, payroll.getActual_working_days());
            pstmt.setInt(i++, payroll.getLeave_days());
            pstmt.setInt(i++, payroll.getLate_days());
            pstmt.setBigDecimal(i++, payroll.getPosition_allowance());
            pstmt.setBigDecimal(i++, payroll.getOther_allowance());
            pstmt.setBigDecimal(i++, payroll.getTotal_allowance());
            pstmt.setBigDecimal(i++, payroll.getBonus_total());
            pstmt.setBigDecimal(i++, payroll.getTaxable_income());
            pstmt.setBigDecimal(i++, payroll.getSocial_insurance_salary());
            pstmt.setBigDecimal(i++, payroll.getInsurance_social());
            pstmt.setBigDecimal(i++, payroll.getInsurance_health());
            pstmt.setBigDecimal(i++, payroll.getInsurance_accident());
            pstmt.setBigDecimal(i++, payroll.getTotal_insurance());
            pstmt.setBigDecimal(i++, payroll.getIncome_tax());
            pstmt.setBigDecimal(i++, payroll.getDeductible_income());
            pstmt.setBigDecimal(i++, payroll.getPenalty_amount());
            pstmt.setBigDecimal(i++, payroll.getNet_income());
            pstmt.setInt(i++, payroll.getCreated_by());
            pstmt.setBigDecimal(i++, payroll.getBase_salary());
            pstmt.addBatch();
        }
        // Execute batch insert


        int[] results = pstmt.executeBatch();
        // Gán id cho payrollDTO
        ResultSet generatedKeys = pstmt.getGeneratedKeys();
        for (PayrollDTO payrollDTO : payrollDTOList) {
            if (generatedKeys.next()) {
                payrollDTO.setPayrollId(generatedKeys.getInt(1));
            }
        }
        return results.length == payrollDTOList.size();
    } catch (SQLException e) {
        log.error("Error while inserting payroll data", e);
        return false;
    }
}

@Override
public PayrollDTO getPayrollByEmployeeId(int employeeId, Connection conn) {
    String sql = "SELECT * FROM payroll WHERE employeeId = ? ORDER BY payrollDate DESC LIMIT 1";

    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setInt(1, employeeId);

        try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return PayrollDTO.builder()
                        .payrollId(rs.getInt("payrollId"))
                        .employeeId(rs.getInt("employeeId"))
                        .payrollDate(rs.getDate("payrollDate").toLocalDate())
                        .actual_working_days(rs.getInt("actual_working_days"))
                        .leave_days(rs.getInt("leave_days"))
                        .late_days(rs.getInt("late_days"))
                        .position_allowance(rs.getBigDecimal("position_allowance"))
                        .other_allowance(rs.getBigDecimal("other_allowance"))
                        .total_allowance(rs.getBigDecimal("total_allowance"))
                        .bonus_total(rs.getBigDecimal("bonus_total"))
                        .taxable_income(rs.getBigDecimal("taxable_income"))
                        .social_insurance_salary(rs.getBigDecimal("social_insurance_salary"))
                        .insurance_social(rs.getBigDecimal("insurance_social"))
                        .insurance_health(rs.getBigDecimal("insurance_health"))
                        .insurance_accident(rs.getBigDecimal("insurance_accident"))
                        .total_insurance(rs.getBigDecimal("total_insurance"))
                        .income_tax(rs.getBigDecimal("income_tax"))
                        .deductible_income(rs.getBigDecimal("deductible_income"))
                        .penalty_amount(rs.getBigDecimal("penalty_amount"))
                        .net_income(rs.getBigDecimal("net_income"))
                        .created_by(rs.getInt("created_by"))
                        .base_salary(rs.getBigDecimal("base_salary"))
                        .build();
            }
        }
    } catch (SQLException e) {
        log.error("Error while fetching payroll by employee ID", e);
    }
    return null;
}

@Override
public boolean getPayrollByMonthAndYear(int month, int year, Connection conn) {
    String sql = "SELECT COUNT(*) FROM payroll WHERE MONTH(payrollDate) = ? AND YEAR(payrollDate) = ?";

    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setInt(1, month);
        pstmt.setInt(2, year);

        try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                log.info("Payroll records found for {}/{}: {}", month, year, rs.getInt(1));
                return rs.getInt(1) > 0;
            }
        }
    } catch (SQLException e) {
        log.error("Error while checking payroll by month and year", e);
    }
    return false;
}

@Override
public BigDecimal getAvgNetIncome6Months(int employeeId, int month, int year, Connection conn) {
    String sql = "SELECT net_income, MONTH(payrollDate), YEAR(payrollDate) FROM payroll " +
                 "WHERE employeeId = ? AND payrollDate <= ? AND payrollDate > ? " +
                 "ORDER BY payrollDate DESC";

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        // Create date for the end of the specified month
        LocalDate endDate = LocalDate.of(year, month, 1)
                .plusMonths(1)
                .minusDays(1);

        // Create date for 6 months before the end date
        LocalDate startDate = endDate.minusMonths(6);

        stmt.setInt(1, employeeId);
        stmt.setDate(2, java.sql.Date.valueOf(endDate));
        stmt.setDate(3, java.sql.Date.valueOf(startDate));

        try (ResultSet rs = stmt.executeQuery()) {
            BigDecimal totalIncome = BigDecimal.ZERO;
            int monthCount = 0;
            // Track which months we've already counted
            Set<String> processedMonths = new HashSet<>();

            while (rs.next()) {
                // Get the month and year for this record
                int recordMonth = rs.getInt(2);
                int recordYear = rs.getInt(3);
                String monthYearKey = recordYear + "-" + recordMonth;

                // Only count each month once
                if (!processedMonths.contains(monthYearKey)) {
                    processedMonths.add(monthYearKey);
                    BigDecimal netIncome = rs.getBigDecimal(1);
                    if (netIncome != null) {
                        totalIncome = totalIncome.add(netIncome);
                        monthCount++;
                    }
                }
            }
//            log.info("Total income for 6 months before {}/{}: {}, Month count: {}",
//                    month, year, totalIncome, monthCount);

            // Return average based on actual number of months with data
            if (monthCount > 0) {
                return totalIncome.divide(BigDecimal.valueOf(monthCount), 2, RoundingMode.HALF_UP);
            }
        }
    } catch (SQLException e) {
        log.error("Error while calculating average net income", e);
    }
    return BigDecimal.ZERO;
}

    @Override
    public PayrollDTO getPayrollByEmployeeId(int employeeId, int month, int year, Connection conn) {

        String sql = "SELECT * FROM payroll WHERE employeeId = ? AND MONTH(payrollDate) = ? AND YEAR(payrollDate) = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, employeeId);
            pstmt.setInt(2, month);
            pstmt.setInt(3, year);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return PayrollDTO.builder()
                            .payrollId(rs.getInt("payrollId"))
                            .employeeId(rs.getInt("employeeId"))
                            .payrollDate(rs.getDate("payrollDate").toLocalDate())
                            .actual_working_days(rs.getInt("actual_working_days"))
                            .leave_days(rs.getInt("leave_days"))
                            .late_days(rs.getInt("late_days"))
                            .position_allowance(rs.getBigDecimal("position_allowance"))
                            .other_allowance(rs.getBigDecimal("other_allowance"))
                            .total_allowance(rs.getBigDecimal("total_allowance"))
                            .bonus_total(rs.getBigDecimal("bonus_total"))
                            .taxable_income(rs.getBigDecimal("taxable_income"))
                            .social_insurance_salary(rs.getBigDecimal("social_insurance_salary"))
                            .insurance_social(rs.getBigDecimal("insurance_social"))
                            .insurance_health(rs.getBigDecimal("insurance_health"))
                            .insurance_accident(rs.getBigDecimal("insurance_accident"))
                            .total_insurance(rs.getBigDecimal("total_insurance"))
                            .income_tax(rs.getBigDecimal("income_tax"))
                            .deductible_income(rs.getBigDecimal("deductible_income"))
                            .penalty_amount(rs.getBigDecimal("penalty_amount"))
                            .net_income(rs.getBigDecimal("net_income"))
                            .created_by(rs.getInt("created_by"))
                            .base_salary(rs.getBigDecimal("base_salary"))
                            .build();
                }
            }
        } catch (SQLException e) {
            log.error("Error while fetching payroll by employee ID, month and year", e);
        }
        return null;
    }

    @Override
    public List<PayrollDTO> findByPeriod(Integer selectedMonth, Integer selectedYear, Connection connection) {
        String sql = "SELECT * FROM payroll WHERE MONTH(payrollDate) = ? AND YEAR(payrollDate) = ?";
        List<PayrollDTO> payrollList = new ArrayList<>();

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, selectedMonth);
            pstmt.setInt(2, selectedYear);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    PayrollDTO payrollDTO = PayrollDTO.builder()
                            .payrollId(rs.getInt("payrollId"))
                            .employeeId(rs.getInt("employeeId"))
                            .payrollDate(rs.getDate("payrollDate").toLocalDate())
                            .actual_working_days(rs.getInt("actual_working_days"))
                            .leave_days(rs.getInt("leave_days"))
                            .late_days(rs.getInt("late_days"))
                            .position_allowance(rs.getBigDecimal("position_allowance"))
                            .other_allowance(rs.getBigDecimal("other_allowance"))
                            .total_allowance(rs.getBigDecimal("total_allowance"))
                            .bonus_total(rs.getBigDecimal("bonus_total"))
                            .taxable_income(rs.getBigDecimal("taxable_income"))
                            .social_insurance_salary(rs.getBigDecimal("social_insurance_salary"))
                            .insurance_social(rs.getBigDecimal("insurance_social"))
                            .insurance_health(rs.getBigDecimal("insurance_health"))
                            .insurance_accident(rs.getBigDecimal("insurance_accident"))
                            .total_insurance(rs.getBigDecimal("total_insurance"))
                            .income_tax(rs.getBigDecimal("income_tax"))
                            .deductible_income(rs.getBigDecimal("deductible_income"))
                            .penalty_amount(rs.getBigDecimal("penalty_amount"))
                            .net_income(rs.getBigDecimal("net_income"))
                            .created_by(rs.getInt("created_by"))
                            .base_salary(rs.getBigDecimal("base_salary"))
                            .build();
                    payrollList.add(payrollDTO);
                }
            }
        } catch (SQLException e) {
            log.error("Error while fetching payroll by period", e);
        }
        return payrollList;
    }
}
