package com.example.medicinedistribution.DTO;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class PayrollDTO {
    private Integer payrollId;
    private Integer employeeId;
    private LocalDate payrollDate;
    private Integer actual_working_days;
    private Integer leave_days;
    private Integer late_days;
    private BigDecimal position_allowance;
    private BigDecimal other_allowance;
    private BigDecimal total_allowance;
    private BigDecimal bonus_total;
    private BigDecimal taxable_income;
    private BigDecimal social_insurance_salary;
    private BigDecimal insurance_social;
    private BigDecimal insurance_health;
    private BigDecimal insurance_accident;
    private BigDecimal total_insurance;
    private BigDecimal income_tax;
    private BigDecimal deductible_income;
    private BigDecimal penalty_amount;
    private BigDecimal net_income;
    private Integer created_by;

    private BigDecimal base_salary;
    private BigDecimal meal_allowance;
    private BigDecimal gas_allowance;
    private BigDecimal phone_allowance;
    private BigDecimal responsibility_allowance;
}
