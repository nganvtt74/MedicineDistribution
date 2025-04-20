package com.example.medicinedistribution.BUS;

import com.example.medicinedistribution.BUS.Interface.GoodsReceiptBUS;
import com.example.medicinedistribution.BUS.Interface.InvoiceBUS;
import com.example.medicinedistribution.BUS.Interface.StatisticsBUS;
import com.example.medicinedistribution.DAO.Interface.GoodsReceiptDAO;
import com.example.medicinedistribution.DAO.Interface.InvoiceDAO;
import com.example.medicinedistribution.DAO.Interface.InvoiceDetailDAO;
import com.example.medicinedistribution.DTO.StatisticDTO;
import com.example.medicinedistribution.DTO.UserSession;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

public class StatisticsBUSImpl implements StatisticsBUS {
    private final InvoiceBUS invoiceBUS;
    private final GoodsReceiptBUS goodsReceiptBUS;
    private final UserSession userSession;

    public StatisticsBUSImpl(InvoiceBUS invoiceBUS, GoodsReceiptBUS goodsReceiptBUS, UserSession userSession) {
        this.invoiceBUS = invoiceBUS;
        this.goodsReceiptBUS = goodsReceiptBUS;
        this.userSession = userSession;
    }


@Override
public List<StatisticDTO> getRevenueStatistics(LocalDate fromDate, LocalDate toDate, String viewType) {
    // Get revenue statistics from invoice data
    return invoiceBUS.getRevenueSummary(fromDate, toDate, viewType);
}

@Override
public Map<String, BigDecimal> getRevenueByCategoryStatistics(LocalDate fromDate, LocalDate toDate) {
    // Get revenue grouped by product category
    return invoiceBUS.getRevenueByCategorySummary(fromDate, toDate);
}

@Override
public List<StatisticDTO> getExpenseStatistics(LocalDate fromDate, LocalDate toDate, String viewType) {
    // Get expense statistics from goods receipt data
    return goodsReceiptBUS.getExpenseSummary(fromDate, toDate, viewType);
}

@Override
public Map<String, BigDecimal> getExpenseByManufacturerStatistics(LocalDate fromDate, LocalDate toDate) {
    // Get expenses grouped by manufacturer
    return goodsReceiptBUS.getExpenseByManufacturerSummary(fromDate, toDate);
}

@Override
public List<StatisticDTO> getProfitStatistics(LocalDate fromDate, LocalDate toDate, String viewType) {
    // Get revenue data
    List<StatisticDTO> revenueStats = invoiceBUS.getRevenueSummary(fromDate, toDate, viewType);
    // Get expense data
    List<StatisticDTO> expenseStats = goodsReceiptBUS.getExpenseSummary(fromDate, toDate, viewType);

    // Map to combine revenue and expense data by period
    Map<String, StatisticDTO> profitMap = new HashMap<>();

    // Initialize profit map with revenue data
    for (StatisticDTO revStat : revenueStats) {
        StatisticDTO profitStat = new StatisticDTO();
        profitStat.setPeriod(revStat.getPeriod());
        profitStat.setRevenue(revStat.getAmount());
        profitStat.setExpense(BigDecimal.ZERO);
        profitMap.put(revStat.getPeriod(), profitStat);
    }

    // Add expense data to the map
    for (StatisticDTO expStat : expenseStats) {
        if (profitMap.containsKey(expStat.getPeriod())) {
            // If period exists in map, update expense
            profitMap.get(expStat.getPeriod()).setExpense(expStat.getAmount());
        } else {
            // If period doesn't exist, add new entry
            StatisticDTO profitStat = new StatisticDTO();
            profitStat.setPeriod(expStat.getPeriod());
            profitStat.setRevenue(BigDecimal.ZERO);
            profitStat.setExpense(expStat.getAmount());
            profitMap.put(expStat.getPeriod(), profitStat);
        }
    }

    // Convert map to list and sort by period
    List<StatisticDTO> profitStats = new ArrayList<>(profitMap.values());
    profitStats.sort(Comparator.comparing(StatisticDTO::getPeriod));

    return profitStats;
}
}
