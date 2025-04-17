package com.example.medicinedistribution.DTO;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PositionDTO {
    private int positionId;  // PK
    private String positionName;
    private int departmentId;  // FK
}
