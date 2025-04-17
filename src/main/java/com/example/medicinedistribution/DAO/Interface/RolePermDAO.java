package com.example.medicinedistribution.DAO.Interface;

import com.example.medicinedistribution.DTO.PermissionDTO;
import com.example.medicinedistribution.DTO.RolePermDTO;

import java.sql.Connection;
import java.util.List;

public interface RolePermDAO extends BaseDAO<RolePermDTO, Integer> {
    public List<PermissionDTO> findByRoleId(Integer roleId, Connection conn);

}
