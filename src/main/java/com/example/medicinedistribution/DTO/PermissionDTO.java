package com.example.medicinedistribution.DTO;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionDTO {
    private String permissionCode;
    private String permName;
    private String parentPermissionCode;
    private String editableByPermissionCode;
    private Boolean status;

    private boolean isEditable;
    private boolean isChecked;
}