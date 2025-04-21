package com.example.medicinedistribution.BUS.Interface;

import com.example.medicinedistribution.DTO.StatisticDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface StatisticsBUS {
    public List<StatisticDTO> getRevenueStatistics(LocalDate fromDate, LocalDate toDate, String viewType);
    public Map<String, BigDecimal> getRevenueByCategoryStatistics(LocalDate fromDate, LocalDate toDate);
    public List<StatisticDTO> getExpenseStatistics(LocalDate fromDate, LocalDate toDate, String viewType) ;
    public Map<String, BigDecimal> getExpenseByManufacturerStatistics(LocalDate fromDate, LocalDate toDate) ;
    public List<StatisticDTO> getProfitStatistics(LocalDate fromDate, LocalDate toDate, String viewType) ;

    BigDecimal getDailySales(LocalDate now);

    BigDecimal getSalesBetweenDates(LocalDate firstDayOfMonth, LocalDate now);

    Map<String, BigDecimal> getProductCategoryDistribution();
}
