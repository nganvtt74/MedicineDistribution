package com.example.medicinedistribution.DTO;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentDTO {
    private int departmentId;  // PK
    private String departmentName;
}
