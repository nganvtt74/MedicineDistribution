package com.example.medicinedistribution.DTO;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RolePermDTO {
    private Integer roleId;
    private String permissionCode;
}