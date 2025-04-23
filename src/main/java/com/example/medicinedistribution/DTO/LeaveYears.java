package com.example.medicinedistribution.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveYears {
    private Integer leaveYearsId;
    private Integer employeeId;
    private LocalDate leaveYear;
    private Integer validLeaveDays;
}
