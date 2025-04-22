package com.example.medicinedistribution.BUS.Interface;

import com.example.medicinedistribution.DTO.EmployeeDTO;
import com.example.medicinedistribution.DTO.PositionHistoryDTO;
import jakarta.validation.constraints.Email;

import java.util.List;

public interface EmployeeBUS extends BaseBUS<EmployeeDTO,Integer> {
    List<EmployeeDTO> getEmployeeWithoutAccount();

    boolean insertPersonalInfo(EmployeeDTO employee);

    EmployeeDTO findByEmail(@Email(message = "Email không hợp lệ") String email);

    boolean updatePersonalInfo(EmployeeDTO employee);

    boolean updateEmploymentInfo(EmployeeDTO employee);

    List<PositionHistoryDTO> findHistoryByEmployeeId(Integer employeeId);
}
