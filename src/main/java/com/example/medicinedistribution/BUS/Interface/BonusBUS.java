package com.example.medicinedistribution.BUS.Interface;

import com.example.medicinedistribution.DTO.BonusDTO;

import java.time.LocalDate;
import java.util.List;

public interface BonusBUS extends BaseBUS<BonusDTO, Integer> {
    List<BonusDTO> findByEmployeeId(Integer employeeId);
    List<BonusDTO> findByDateRange(LocalDate startDate, LocalDate endDate);

    List<BonusDTO> getByMothYear(int monthValue, int year);
}
