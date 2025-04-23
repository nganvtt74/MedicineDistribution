package com.example.medicinedistribution.DAO.Interface;

import com.example.medicinedistribution.DTO.Payroll_AllowanceDTO;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;

public interface Payroll_AllowanceDAO {
    boolean add(Payroll_AllowanceDTO payrollAllowance, Connection connection);
    boolean addBatch(List<Payroll_AllowanceDTO> payrollAllowances, Connection connection);
    List<Payroll_AllowanceDTO> getByPayrollId(int payrollId, Connection connection);
    boolean deleteByPayrollId(int payrollId, Connection connection);
    boolean update(Payroll_AllowanceDTO payrollAllowance, Connection connection);

    boolean insertPayroll_Allowance(Integer payrollId, BigDecimal mealAllowance, BigDecimal gasAllowance, BigDecimal phoneAllowance, BigDecimal responsibilityAllowance, Connection connection);
}
