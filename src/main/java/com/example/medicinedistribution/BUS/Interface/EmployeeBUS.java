package com.example.medicinedistribution.BUS.Interface;

import com.example.medicinedistribution.DTO.EmployeeDTO;

import java.util.List;

public interface EmployeeBUS extends BaseBUS<EmployeeDTO,Integer> {
    List<EmployeeDTO> getEmployeeWithoutAccount();
}
