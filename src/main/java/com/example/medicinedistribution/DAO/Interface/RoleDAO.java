package com.example.medicinedistribution.DAO.Interface;

import com.example.medicinedistribution.DTO.RoleDTO;

import java.sql.Connection;

public interface RoleDAO extends BaseDAO<RoleDTO, Integer> {
    public RoleDTO findByName(String roleName, Connection connection);
}
