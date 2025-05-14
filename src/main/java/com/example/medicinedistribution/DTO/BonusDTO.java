package com.example.medicinedistribution.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BonusDTO {
    private Integer id;
    private Integer employee_id;
    private String employee_name;
    private String position_name;
    private String department_name;
    private Integer bonus_type_id;
    private String bonus_type_name;
    private BigDecimal amount;
    private LocalDate date;

}
