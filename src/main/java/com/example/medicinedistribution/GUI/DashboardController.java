package com.example.medicinedistribution.GUI;

import com.example.medicinedistribution.BUS.BUSFactory;
import com.example.medicinedistribution.BUS.Interface.*;
import com.example.medicinedistribution.DTO.StatisticDTO;
import com.example.medicinedistribution.Util.CurrencyUtils;
import javafx.fxml.FXML;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
public class DashboardController {
    @FXML private Label lblTodaySales;
    @FXML private Label lblMonthSales;
    @FXML private Label lblInventoryValue;
    @FXML private Label lblPendingOrders;
    @FXML private Label lblTodayDate;
    @FXML private Label lblTopSellingProduct;
    @FXML private Label lblRecentActivity;

    @FXML private AreaChart<String, Number> chartRecentSales;
    @FXML private PieChart chartProductCategories;

    private final BUSFactory busFactory;
    private StatisticsBUS statisticBUS;
    private ProductBUS productBUS;
    private InvoiceBUS invoiceBUS;

    public DashboardController(BUSFactory busFactory) {
        this.busFactory = busFactory;
    }

    @FXML
    public void initialize() {
        statisticBUS = busFactory.getStatisticsBUS();
        productBUS = busFactory.getProductBUS();
        invoiceBUS = busFactory.getInvoiceBUS();

        loadDashboardData();
    }

    private void loadDashboardData() {
        try {
            // Display today's date
            lblTodayDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

            // Get today's sales
            BigDecimal todaySales = statisticBUS.getDailySales(LocalDate.now());
            lblTodaySales.setText(CurrencyUtils.formatVND(todaySales));

            // Get month's sales
            LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
            BigDecimal monthSales = statisticBUS.getSalesBetweenDates(firstDayOfMonth, LocalDate.now());
            lblMonthSales.setText(CurrencyUtils.formatVND(monthSales));

            // Get inventory value
            BigDecimal inventoryValue = productBUS.getTotalInventoryValue();
            lblInventoryValue.setText(CurrencyUtils.formatVND(inventoryValue));
            // Load chart data
            loadSalesChart();
            loadProductCategoriesChart();

        } catch (Exception e) {
            log.error("Error loading dashboard data", e);
        }
    }

    private void loadSalesChart() {
        chartRecentSales.getData().clear();

        try {
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(6);

            List<StatisticDTO> salesStats = statisticBUS.getRevenueStatistics(startDate, endDate, "Ngày");

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Doanh thu");

            for (StatisticDTO stat : salesStats) {
                series.getData().add(new XYChart.Data<>(stat.getPeriod(), stat.getAmount()));
            }

            chartRecentSales.getData().add(series);
        } catch (Exception e) {
            log.error("Error loading sales chart", e);
        }
    }

    private void loadProductCategoriesChart() {
        chartProductCategories.getData().clear();

        try {
            Map<String, BigDecimal> categoryStats = statisticBUS.getProductCategoryDistribution();

            for (Map.Entry<String, BigDecimal> entry : categoryStats.entrySet()) {
                PieChart.Data slice = new PieChart.Data(entry.getKey(), entry.getValue().doubleValue());
                chartProductCategories.getData().add(slice);
            }
        } catch (Exception e) {
            log.error("Error loading category chart", e);
        }
    }
}