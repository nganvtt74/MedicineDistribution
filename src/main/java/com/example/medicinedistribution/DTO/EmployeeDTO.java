package com.example.medicinedistribution.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDTO {
    private int employeeId;  // PK
    private String firstName;
    private String lastName;
    private LocalDate birthday;
    private String gender;
    private String phone;
    private String email;
    private LocalDate hireDate;
    private String address;
    private BigDecimal basicSalary;  // Sử dụng BigDecimal cho tiền lương
    private String status;
    private int positionId;  // FK
    private int accountId;  // FK
}
