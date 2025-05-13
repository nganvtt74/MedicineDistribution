package com.example.medicinedistribution.DAO.Interface;

import com.example.medicinedistribution.DTO.PayrollDTO;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;

public interface PayrollDAO {
    boolean insertPayroll(List<PayrollDTO> payrollDTOList, Connection conn);
    PayrollDTO getPayrollByEmployeeId(int employeeId, Connection conn);
    boolean getPayrollByMonthAndYear(int month, int year, Connection conn);
    BigDecimal getAvgNetIncome6Months(int employeeId, int month, int year, Connection conn);
    PayrollDTO getPayrollByEmployeeId(int employeeId, int month, int year, Connection conn);

    List<PayrollDTO> findByPeriod(Integer selectedMonth, Integer selectedYear, Connection connection);
}
