package com.example.medicinedistribution.DAO.Interface;

import com.example.medicinedistribution.DTO.PositionDTO;

import java.sql.Connection;
import java.util.List;

public interface PositionDAO extends BaseDAO<PositionDTO, Integer> {
    public List<PositionDTO> findByDepartmentId(Integer id, Connection conn); // Check if the departmentId exists

}
