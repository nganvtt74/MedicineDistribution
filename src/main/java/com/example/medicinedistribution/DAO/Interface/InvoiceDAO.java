package com.example.medicinedistribution.DAO.Interface;

import com.example.medicinedistribution.DTO.InvoiceDTO;
import com.example.medicinedistribution.DTO.ProductStatisticDTO;
import com.example.medicinedistribution.DTO.StatisticDTO;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface InvoiceDAO extends BaseDAO<InvoiceDTO, Integer> {

    Map<String, BigDecimal> getRevenueByCategoryStatistics(LocalDate fromDate, LocalDate toDate, Connection connection);

    List<StatisticDTO> getRevenueStatistics(LocalDate fromDate, LocalDate toDate, String groupBy, String viewType, Connection connection);

    BigDecimal getDailySales(LocalDate now, Connection connection);

    List<ProductStatisticDTO> getProductSalesStatistics(LocalDate fromDate, LocalDate toDate, String groupBy, String viewType, Connection connection);

    Map<String, Integer> getProductSalesByCategoryStatistics(LocalDate fromDate, LocalDate toDate, Connection connection);

    Integer getNextInvoiceId(Connection connection);
}
