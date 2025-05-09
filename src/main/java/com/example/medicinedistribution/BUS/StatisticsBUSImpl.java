package com.example.medicinedistribution.BUS;

import com.example.medicinedistribution.BUS.Interface.GoodsReceiptBUS;
import com.example.medicinedistribution.BUS.Interface.InvoiceBUS;
import com.example.medicinedistribution.BUS.Interface.StatisticsBUS;
import com.example.medicinedistribution.DAO.Interface.GoodsReceiptDAO;
import com.example.medicinedistribution.DAO.Interface.InvoiceDAO;
import com.example.medicinedistribution.DAO.Interface.InvoiceDetailDAO;
import com.example.medicinedistribution.DTO.ProductStatisticDTO;
import com.example.medicinedistribution.DTO.StatisticDTO;
import com.example.medicinedistribution.DTO.UserSession;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static java.util.Arrays.stream;

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

    @Override
    public BigDecimal getDailySales(LocalDate now) {
        return invoiceBUS.getDailySales(now);
    }

    @Override
    public BigDecimal getSalesBetweenDates(LocalDate firstDayOfMonth, LocalDate now) {
        // Get sales between two dates
        List<StatisticDTO> statisticDTOList = invoiceBUS.getRevenueSummary(firstDayOfMonth, now, "DAILY");
        BigDecimal totalSales = BigDecimal.ZERO;
        for (StatisticDTO stat : statisticDTOList) {
            totalSales = totalSales.add(stat.getAmount());
        }
        return totalSales;
    }

    @Override
    public Map<String, BigDecimal> getProductCategoryDistribution() {
        // Get product category distribution
        Map<String, BigDecimal> categoryStats = invoiceBUS.getRevenueByCategorySummary(LocalDate.now().minusMonths(1), LocalDate.now());
        Map<String, BigDecimal> result = new HashMap<>();

        for (Map.Entry<String, BigDecimal> entry : categoryStats.entrySet()) {
            String category = entry.getKey();
            BigDecimal amount = entry.getValue();
            result.put(category, amount);
        }

        return result;
    }

@Override
public List<ProductStatisticDTO> getProductSalesStatistics(LocalDate fromDate, LocalDate toDate, String viewType) {
    // Get product sales statistics
    List<ProductStatisticDTO> productStats = invoiceBUS.getProductSalesSummary(fromDate, toDate, viewType);

    // Create a map to consolidate duplicate products by product ID
    Map<String, ProductStatisticDTO> consolidatedStats = new HashMap<>();

    // Consolidate duplicate products by summing their quantities
    for (ProductStatisticDTO stat : productStats) {
        String productId = String.valueOf(stat.getProductId());

        if (consolidatedStats.containsKey(productId)) {
            // Add quantity to existing entry
            ProductStatisticDTO existingStat = consolidatedStats.get(productId);
            existingStat.setQuantity(existingStat.getQuantity() + stat.getQuantity());
        } else {
            // Create new entry
            ProductStatisticDTO productStat = new ProductStatisticDTO(
                stat.getProductId(),
                stat.getProductName(),
                stat.getCategoryName(),
                stat.getQuantity()
            );
            consolidatedStats.put(productId, productStat);
        }
    }

    // Convert the map values to a list
    List<ProductStatisticDTO> result = new ArrayList<>(consolidatedStats.values());

    // Sort by quantity in descending order
    result.sort((a, b) -> b.getQuantity() - a.getQuantity());

    return result;
}

    @Override
    public Map<String, Integer> getProductSalesByCategory(LocalDate fromDate, LocalDate toDate) {
        // Get product sales grouped by category
        Map<String, Integer> productStats = invoiceBUS.getProductSalesByCategorySummary(fromDate, toDate);
        Map<String, Integer> result = new HashMap<>();

        for (Map.Entry<String, Integer> entry : productStats.entrySet()) {
            String category = entry.getKey();
            Integer quantity = entry.getValue();
            result.put(category, quantity);
        }

        return result;
    }

}
