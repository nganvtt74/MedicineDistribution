package com.example.medicinedistribution.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {
    private Integer roleId;
    
    @NotBlank(message = "Tên vai trò không được để trống")
    @Size(max = 100, message = "Tên vai trò không được vượt quá 100 ký tự")
    private String roleName;
    
    @NotNull(message = "Trạng thái không được để trống")
    private Integer status;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Valid
    private List<PermissionDTO> permissions;
}
