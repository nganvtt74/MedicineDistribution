package com.example.medicinedistribution.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionDTO {
    @NotBlank(message = "Mã quyền không được để trống")
    private String permissionCode;
    
    @NotBlank(message = "Tên quyền không được để trống")
    private String permName;
    
    private String parentPermissionCode;
    private String editableByPermissionCode;
    
    @NotNull(message = "Trạng thái không được để trống")
    private Integer status;

    private boolean isEditable;
    private boolean isChecked;
}
