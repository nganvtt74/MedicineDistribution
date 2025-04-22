package com.example.medicinedistribution.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DependentsDTO {
    private int employeeId;
    private int dependentNo;
    private String firstName;
    private String lastName;
    private LocalDate birthday;
    private String relationship;
}
