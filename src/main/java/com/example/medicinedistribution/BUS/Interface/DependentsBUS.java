package com.example.medicinedistribution.BUS.Interface;

import com.example.medicinedistribution.DTO.DependentsDTO;

import java.sql.Connection;
import java.util.List;

public interface DependentsBUS {
    boolean insert(DependentsDTO dependentsDTO);
    boolean update(DependentsDTO dependentsDTO);
    boolean delete(Integer employeeId, Integer dependentNo);
    List<DependentsDTO> findByEmployeeId(Integer employeeId);
    DependentsDTO findById(Integer employeeId, Integer dependentNo);
    boolean deleteByEmployeeId(Integer employeeId);
}
