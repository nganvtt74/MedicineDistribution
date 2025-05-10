package com.example.medicinedistribution.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PositionHistoryDTO {
    private Integer id;
    private LocalDate date;
    private String positionName;
    private String oldPositionName;
    private Integer employeeId;
    private BigDecimal salaryBefore;
    private BigDecimal salaryAfter;
}
