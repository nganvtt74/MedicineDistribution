package com.example.medicinedistribution.DAO.Interface;

import com.example.medicinedistribution.DTO.BonusDTO;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;

public interface BonusDAO extends BaseDAO<BonusDTO, Integer> {
    List<BonusDTO> findByEmployeeId(Integer employeeId, Connection conn);
    List<BonusDTO> findByDateRange(LocalDate startDate, LocalDate endDate, Connection conn);
}
