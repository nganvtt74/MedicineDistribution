package com.example.medicinedistribution.BUS.Interface;

import com.example.medicinedistribution.DTO.RequestsDTO;

import java.time.LocalDate;
import java.util.List;

public interface RequestBUS extends BaseBUS<RequestsDTO, Integer> {
    List<RequestsDTO> findByFilters(LocalDate fromDate, LocalDate toDate, String status, Integer typeId);
    // Any additional business methods specific to Request operations
}