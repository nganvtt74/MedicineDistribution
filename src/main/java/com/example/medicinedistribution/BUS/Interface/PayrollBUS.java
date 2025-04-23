package com.example.medicinedistribution.BUS.Interface;

import com.example.medicinedistribution.DTO.PayrollDTO;

import java.math.BigDecimal;
import java.util.List;

public interface PayrollBUS {
    boolean insertPayroll(List<PayrollDTO> payrollDTOList);
    PayrollDTO getPayrollByEmployeeId(int employeeId);
    boolean getPayrollByMonthAndYear(int month, int year);
    BigDecimal getAvgNetIncome6Months(int employeeId, int month, int year);


    PayrollDTO getPayrollByEmployeeId(int employeeId, int month, int year);
}
