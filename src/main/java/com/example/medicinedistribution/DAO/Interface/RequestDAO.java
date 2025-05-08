package com.example.medicinedistribution.DAO.Interface;

import com.example.medicinedistribution.DTO.RequestsDTO;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;

public interface RequestDAO extends BaseDAO<RequestsDTO, Integer> {
    List<RequestsDTO> findByFilters(LocalDate fromDate, LocalDate toDate, String status, Integer typeId, Connection conn);
    // Any additional methods specific to Request operations
}