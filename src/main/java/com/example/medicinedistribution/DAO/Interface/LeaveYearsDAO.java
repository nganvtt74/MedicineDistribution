package com.example.medicinedistribution.DAO.Interface;

import com.example.medicinedistribution.DTO.LeaveYears;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;

public interface LeaveYearsDAO extends BaseDAO<LeaveYears, Integer> {
    List<LeaveYears> findByEmployeeId(Integer employeeId, Connection conn);
    LeaveYears findByEmployeeIdAndYear(Integer employeeId, LocalDate leaveYear, Connection conn);
    boolean updateValidLeaveDays(Integer leaveYearsId, Integer validLeaveDays, Connection conn);
    boolean createOrUpdateLeaveYear(LeaveYears leaveYears, Connection conn);
    List<LeaveYears> findAllCurrentYear(Connection conn);
}