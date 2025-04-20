package com.example.medicinedistribution.DAO.Interface;

import com.example.medicinedistribution.DTO.EmployeeDTO;

import java.sql.Connection;

public interface EmployeeDAO extends BaseDAO<EmployeeDTO,Integer> {
    EmployeeDTO findByAccountId(Integer accountId, Connection conn);

}
