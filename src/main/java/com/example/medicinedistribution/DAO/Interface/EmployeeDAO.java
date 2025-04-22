package com.example.medicinedistribution.DAO.Interface;

import com.example.medicinedistribution.DTO.EmployeeDTO;

import java.sql.Connection;
import java.util.List;

public interface EmployeeDAO extends BaseDAO<EmployeeDTO,Integer> {

    List<EmployeeDTO> getEmployeeWithoutAccount(Connection conn);

    boolean updateEmploymentInfo(EmployeeDTO employee, Connection conn);
}
