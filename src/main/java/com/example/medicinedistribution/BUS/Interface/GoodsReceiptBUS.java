package com.example.medicinedistribution.BUS.Interface;

import com.example.medicinedistribution.DTO.GoodsReceiptDTO;
import com.example.medicinedistribution.DTO.StatisticDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface GoodsReceiptBUS extends BaseBUS<GoodsReceiptDTO, Integer> {
    // Add to GoodsReceiptBUS interface
    List<StatisticDTO> getExpenseSummary(LocalDate fromDate, LocalDate toDate, String viewType);
    Map<String, BigDecimal> getExpenseByManufacturerSummary(LocalDate fromDate, LocalDate toDate);
}
