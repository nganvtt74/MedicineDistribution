package com.example.medicinedistribution.BUS.Interface;

import com.example.medicinedistribution.DTO.InvoiceDTO;
import com.example.medicinedistribution.DTO.StatisticDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface InvoiceBUS extends BaseBUS<InvoiceDTO, Integer> {
// Add to InvoiceBUS interface
List<StatisticDTO> getRevenueSummary(LocalDate fromDate, LocalDate toDate, String viewType);
Map<String, BigDecimal> getRevenueByCategorySummary(LocalDate fromDate, LocalDate toDate);

    BigDecimal getDailySales(LocalDate now);
}
