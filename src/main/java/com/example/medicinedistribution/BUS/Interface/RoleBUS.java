package com.example.medicinedistribution.BUS.Interface;

import com.example.medicinedistribution.DTO.PermissionDTO;
import com.example.medicinedistribution.DTO.RoleDTO;

import java.util.List;

public interface RoleBUS extends BaseBUS<RoleDTO,Integer>{
    public RoleDTO getRoleForEdit(Integer roleId);

    List<RoleDTO> getRolesWithoutEditablePermissions();

    List<PermissionDTO> getNewPermissionsForEdit();
}
