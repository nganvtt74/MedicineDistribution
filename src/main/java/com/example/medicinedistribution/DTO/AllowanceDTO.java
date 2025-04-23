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
public class AllowanceDTO {
    private Integer id;
    private String name;
    private BigDecimal amount;
    private Boolean is_insurance_included;
}
