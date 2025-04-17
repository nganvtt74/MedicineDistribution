package com.example.medicinedistribution.BUS.Interface;

import com.example.medicinedistribution.DTO.RoleDTO;

public interface RoleBUS extends BaseBUS<RoleDTO,Integer>{
    public RoleDTO getRoleForEdit(Integer roleId);
}
