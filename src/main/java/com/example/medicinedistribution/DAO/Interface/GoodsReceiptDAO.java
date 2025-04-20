package com.example.medicinedistribution.DAO.Interface;

import com.example.medicinedistribution.DTO.GoodsReceiptDTO;
import com.example.medicinedistribution.DTO.StatisticDTO;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface GoodsReceiptDAO extends BaseDAO<GoodsReceiptDTO,Integer> {
    List<StatisticDTO> getExpenseStatistics(LocalDate fromDate, LocalDate toDate, String groupBy, String viewType , Connection connection);

    Map<String, BigDecimal> getExpenseByManufacturerStatistics(LocalDate fromDate, LocalDate toDate, Connection connection);
}
