package com.example.medicinedistribution.DAO.Interface;

import com.example.medicinedistribution.DTO.PositionHistoryDTO;

import java.sql.Connection;
import java.util.List;

public interface PositionHistoryDAO extends BaseDAO<PositionHistoryDTO,Integer> {
    List<PositionHistoryDTO> findByEmployeeId(Integer employeeId, Connection conn);

}
