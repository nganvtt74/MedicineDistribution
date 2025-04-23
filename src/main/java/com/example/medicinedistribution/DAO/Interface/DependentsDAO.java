package com.example.medicinedistribution.DAO.Interface;

import com.example.medicinedistribution.DTO.DependentsDTO;

import java.sql.Connection;
import java.util.List;

public interface DependentsDAO{
    boolean insert(DependentsDTO dependentsDTO, Connection connection);
    boolean update(DependentsDTO dependentsDTO, Connection connection);
    boolean delete(Integer employeeId, Integer dependentNo, Connection connection);
    List<DependentsDTO> findByEmployeeId(Integer employeeId, Connection connection);
    DependentsDTO findById(Integer employeeId, Integer dependentNo, Connection connection);
    boolean deleteByEmployeeId(Integer employeeId, Connection connection);

    int countDependentByEmployeeId(Integer employeeId, Connection connection);
}
