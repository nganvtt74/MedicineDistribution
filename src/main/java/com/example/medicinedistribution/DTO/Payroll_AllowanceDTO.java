package com.example.medicinedistribution.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payroll_AllowanceDTO {
    private Integer payroll_id;
    private Integer allowance_id;
    private BigDecimal amount;
}
