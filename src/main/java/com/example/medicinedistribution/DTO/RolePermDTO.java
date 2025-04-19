package com.example.medicinedistribution.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RolePermDTO {
    @NotNull(message = "Mã vai trò không được để trống")
    private Integer roleId;
    
    @NotBlank(message = "Mã quyền không được để trống")
    private String permissionCode;
}
