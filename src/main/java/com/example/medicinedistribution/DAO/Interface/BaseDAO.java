package com.example.medicinedistribution.DAO.Interface;

import com.example.medicinedistribution.DTO.RoleDTO;

import java.sql.Connection;
import java.util.List;

public interface BaseDAO<T, ID> {
    ID insert(T t, Connection conn);
    boolean update(T t, Connection conn);
    boolean delete(ID id, Connection conn);
    T findById(ID id, Connection conn);
    List<T> findAll(Connection conn);
}