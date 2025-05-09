package com.example.medicinedistribution.GUI;

import com.example.medicinedistribution.BUS.BUSFactory;
import com.example.medicinedistribution.DTO.ProductStatisticDTO;
import com.example.medicinedistribution.DTO.StatisticDTO;
import com.example.medicinedistribution.BUS.Interface.StatisticsBUS;
import com.example.medicinedistribution.Util.NotificationUtil;
import com.example.medicinedistribution.Util.CurrencyUtils;
import com.example.medicinedistribution.Util.ExportUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class SalesStatisticController implements Initializable {
    // Common services
    private final StatisticsBUS statisticBUS ;
    private final BUSFactory busFactory;

    public SalesStatisticController(BUSFactory busFactory) {
        this.busFactory = busFactory;
        this.statisticBUS = busFactory.getStatisticsBUS();
    }

    // Time period options
    private final ObservableList<String> viewOptions = FXCollections.observableArrayList(
            "Ngày", "Tuần", "Tháng", "Quý", "Năm"
    );

    // Date formatter
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ======== REVENUE TAB COMPONENTS ========
    @FXML private DatePicker dpRevenueFromDate;
    @FXML private DatePicker dpRevenueToDate;
    @FXML private ComboBox<String> cboRevenueView;
    @FXML private Button btnRevenueFilter;
    @FXML private Button btnRevenueExport;

    @FXML private LineChart<String, Number> chartRevenue;
    @FXML private CategoryAxis xAxisRevenue;
    @FXML private NumberAxis yAxisRevenue;
    @FXML private PieChart pieChartRevenue;

    @FXML private Label lblTotalRevenue;
    @FXML private Label lblTotalInvoices;
    @FXML private Label lblAverageRevenue;

    @FXML private TableView<StatisticDTO> tblRevenueDetails;
    @FXML private TableColumn<StatisticDTO, String> colRevenueDate;
    @FXML private TableColumn<StatisticDTO, String> colRevenueAmount;
    @FXML private TableColumn<StatisticDTO, Integer> colRevenueInvoices;

    // ======== EXPENSE TAB COMPONENTS ========
    @FXML private DatePicker dpExpenseFromDate;
    @FXML private DatePicker dpExpenseToDate;
    @FXML private ComboBox<String> cboExpenseView;
    @FXML private Button btnExpenseFilter;
    @FXML private Button btnExpenseExport;

    @FXML private BarChart<String, Number> chartExpense;
    @FXML private CategoryAxis xAxisExpense;
    @FXML private NumberAxis yAxisExpense;
    @FXML private PieChart pieChartExpense;

    @FXML private Label lblTotalExpense;
    @FXML private Label lblTotalReceipts;
    @FXML private Label lblAverageExpense;

    @FXML private TableView<StatisticDTO> tblExpenseDetails;
    @FXML private TableColumn<StatisticDTO, String> colExpenseDate;
    @FXML private TableColumn<StatisticDTO, String> colExpenseAmount;
    @FXML private TableColumn<StatisticDTO, Integer> colExpenseReceipts;

    // ======== PROFIT TAB COMPONENTS ========
    @FXML private DatePicker dpProfitFromDate;
    @FXML private DatePicker dpProfitToDate;
    @FXML private ComboBox<String> cboProfitView;
    @FXML private Button btnProfitFilter;
    @FXML private Button btnProfitExport;

    @FXML private LineChart<String, Number> chartProfit;
    @FXML private CategoryAxis xAxisProfit;
    @FXML private NumberAxis yAxisProfit;
    @FXML private BarChart<String, Number> chartCompare;

    @FXML private Label lblProfitTotalRevenue;
    @FXML private Label lblProfitTotalExpense;
    @FXML private Label lblTotalProfit;

    @FXML private TableView<StatisticDTO> tblProfitDetails;
    @FXML private TableColumn<StatisticDTO, String> colProfitDate;
    @FXML private TableColumn<StatisticDTO, String> colProfitRevenue;
    @FXML private TableColumn<StatisticDTO, String> colProfitExpense;
    @FXML private TableColumn<StatisticDTO, String> colProfitAmount;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        removeAnimation();
        initializeControls();
        setupEventHandlers();

        // Set default date range (last 30 days)
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);

        setDateRanges(startDate, endDate);

        // Load initial data
        filterRevenueData();
        filterExpenseData();
        filterProfitData();
        filterProductData();
    }

    private void removeAnimation() {
        // Remove animation from charts
        chartRevenue.setAnimated(false);
        chartExpense.setAnimated(false);
        chartProfit.setAnimated(false);
        chartCompare.setAnimated(false);

        // Remove animation from pie charts
        pieChartRevenue.setAnimated(false);
        pieChartExpense.setAnimated(false);

        // Remove animation from table views
        tblRevenueDetails.setPlaceholder(new Label("Không có dữ liệu"));
        tblExpenseDetails.setPlaceholder(new Label("Không có dữ liệu"));
        tblProfitDetails.setPlaceholder(new Label("Không có dữ liệu"));

        pieChartExpense.setLabelLineLength(0);
        pieChartRevenue.setLabelLineLength(0);
        pieChartExpense.setLabelsVisible(false);
        pieChartRevenue.setLabelsVisible(false);

// Make sure legends are visible
        pieChartExpense.setLegendVisible(true);
        pieChartRevenue.setLegendVisible(true);

        cboProductView.setItems(FXCollections.observableArrayList(
                "Ngày", "Tuần", "Tháng", "Quý", "Năm"
        ));
        cboProductView.getSelectionModel().select(2); // Default to monthly view

        // Remove animations for better performance
        chartProductSales.setAnimated(false);
        pieChartProduct.setAnimated(false);
        pieChartProduct.setLabelLineLength(0);
        pieChartProduct.setLabelsVisible(false);
        pieChartProduct.setLegendVisible(true);


    }

    private void initializeControls() {
        // Initialize ComboBoxes
        cboRevenueView.setItems(viewOptions);
        cboRevenueView.getSelectionModel().select(2); // Select "Month" by default

        cboExpenseView.setItems(viewOptions);
        cboExpenseView.getSelectionModel().select(2);

        cboProfitView.setItems(viewOptions);
        cboProfitView.getSelectionModel().select(2);

        // Setup table columns for revenue
        colRevenueDate.setCellValueFactory(new PropertyValueFactory<>("period"));
        colRevenueAmount.setCellValueFactory(cellData ->
                new SimpleStringProperty(CurrencyUtils.formatVND(cellData.getValue().getAmount())));
        colRevenueInvoices.setCellValueFactory(new PropertyValueFactory<>("count"));

        // Setup table columns for expense
        colExpenseDate.setCellValueFactory(new PropertyValueFactory<>("period"));
        colExpenseAmount.setCellValueFactory(cellData ->
                new SimpleStringProperty(CurrencyUtils.formatVND(cellData.getValue().getAmount())));
        colExpenseReceipts.setCellValueFactory(new PropertyValueFactory<>("count"));

        // Setup table columns for profit
        colProfitDate.setCellValueFactory(new PropertyValueFactory<>("period"));
        colProfitRevenue.setCellValueFactory(cellData ->
                new SimpleStringProperty(CurrencyUtils.formatVND(cellData.getValue().getRevenue())));
        colProfitExpense.setCellValueFactory(cellData ->
                new SimpleStringProperty(CurrencyUtils.formatVND(cellData.getValue().getExpense())));
        colProfitAmount.setCellValueFactory(cellData ->
                new SimpleStringProperty(CurrencyUtils.formatVND(cellData.getValue().getProfit())));

        colProductId.setCellValueFactory(new PropertyValueFactory<>("productId"));
        colProductName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colProductCategory.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        colProductQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        tblProductDetails.setPlaceholder(new Label("Không có dữ liệu"));
    }

    private void setupEventHandlers() {
        // Revenue tab event handlers
        btnRevenueFilter.setOnAction(this::handleRevenueFilter);
        btnRevenueExport.setOnAction(this::handleRevenueExport);

        // Expense tab event handlers
        btnExpenseFilter.setOnAction(this::handleExpenseFilter);
        btnExpenseExport.setOnAction(this::handleExpenseExport);

        // Profit tab event handlers
        btnProfitFilter.setOnAction(this::handleProfitFilter);
        btnProfitExport.setOnAction(this::handleProfitExport);

        btnProductFilter.setOnAction(e -> filterProductData());
        btnProductExport.setOnAction(e -> exportProductData());
    }

    private void setDateRanges(LocalDate startDate, LocalDate endDate) {
        // Set date ranges for all tabs
        dpRevenueFromDate.setValue(startDate);
        dpRevenueToDate.setValue(endDate);

        dpExpenseFromDate.setValue(startDate);
        dpExpenseToDate.setValue(endDate);

        dpProfitFromDate.setValue(startDate);
        dpProfitToDate.setValue(endDate);

        dpProductFromDate.setValue(startDate);
        dpProductToDate.setValue(endDate);
    }

    // ======== REVENUE TAB HANDLERS ========
    private void handleRevenueFilter(ActionEvent event) {
        filterRevenueData();
    }

    private void handleRevenueExport(ActionEvent event) {
        try {
            List<StatisticDTO> revenueDetails = tblRevenueDetails.getItems();
            BigDecimal totalRevenue = CurrencyUtils.parseCurrencyToNumber(lblTotalRevenue.getText());
            int totalInvoices = Integer.parseInt(lblTotalInvoices.getText().replaceAll("[^0-9]", ""));

            ExportUtils.exportRevenueStatistics(
                    revenueDetails,
                    dpRevenueFromDate.getValue(),
                    dpRevenueToDate.getValue(),
                    totalRevenue,
                    totalInvoices
            );
            NotificationUtil.showSuccessNotification("Xuất báo cáo thành công",
                    "Báo cáo doanh thu đã được xuất thành công");
        } catch (Exception e) {
            NotificationUtil.showErrorNotification("Lỗi xuất báo cáo",
                    "Đã xảy ra lỗi khi xuất báo cáo doanh thu: " + e.getMessage());
        }
    }

    private void filterRevenueData() {
        LocalDate fromDate = dpRevenueFromDate.getValue();
        LocalDate toDate = dpRevenueToDate.getValue();
        String viewType = cboRevenueView.getValue();

        if (fromDate == null || toDate == null || viewType == null) {
            NotificationUtil.showErrorNotification("Thiếu thông tin", "Vui lòng chọn đầy đủ thông tin để lọc");
            return;
        }

        if (fromDate.isAfter(toDate)) {
            NotificationUtil.showErrorNotification("Ngày không hợp lệ", "Ngày bắt đầu phải trước ngày kết thúc");
            return;
        }

        try {
            // Get revenue statistics data
            List<StatisticDTO> revenueStats = statisticBUS.getRevenueStatistics(fromDate, toDate, viewType);
            tblRevenueDetails.setItems(FXCollections.observableArrayList(revenueStats));

            // Update line chart
            populateRevenueLineChart(revenueStats);

            // Update pie chart with category data
            Map<String, BigDecimal> categoryRevenue = statisticBUS.getRevenueByCategoryStatistics(fromDate, toDate);
            populateRevenuePieChart(categoryRevenue);

            // Update summary labels
            BigDecimal totalRevenue = BigDecimal.ZERO;
            int totalInvoices = 0;

            for (StatisticDTO stat : revenueStats) {
                totalRevenue = totalRevenue.add(stat.getAmount());
                totalInvoices += stat.getCount();
            }

            lblTotalRevenue.setText(CurrencyUtils.formatVND(totalRevenue));
            lblTotalInvoices.setText(String.valueOf(totalInvoices));

            if (totalInvoices > 0) {
                BigDecimal averageRevenue = totalRevenue.divide(BigDecimal.valueOf(totalInvoices), 0, BigDecimal.ROUND_HALF_UP);
                lblAverageRevenue.setText(CurrencyUtils.formatVND(averageRevenue));
            } else {
                lblAverageRevenue.setText("0₫");
            }

        } catch (Exception e) {
            NotificationUtil.showErrorNotification("Lỗi truy vấn dữ liệu",
                    "Đã xảy ra lỗi khi lấy dữ liệu doanh thu: " + e.getMessage());
        }
    }

    private void populateRevenueLineChart(List<StatisticDTO> revenueStats) {
        chartRevenue.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Doanh thu");

        for (StatisticDTO stat : revenueStats) {
            series.getData().add(new XYChart.Data<>(stat.getPeriod(), stat.getAmount()));
        }

        chartRevenue.getData().add(series);
    }

    private void populateRevenuePieChart(Map<String, BigDecimal> categoryRevenue) {
        pieChartRevenue.getData().clear();

        for (Map.Entry<String, BigDecimal> entry : categoryRevenue.entrySet()) {
            PieChart.Data slice = new PieChart.Data(entry.getKey(), entry.getValue().doubleValue());
            pieChartRevenue.getData().add(slice);
        }
    }

    // ======== EXPENSE TAB HANDLERS ========
    private void handleExpenseFilter(ActionEvent event) {
        filterExpenseData();
    }

    private void handleExpenseExport(ActionEvent event) {
        try {
            // Export expense statistics
            List<StatisticDTO> expenseDetails = tblExpenseDetails.getItems();
            BigDecimal totalExpense = CurrencyUtils.parseCurrencyToNumber(lblTotalExpense.getText());
            int totalReceipts = Integer.parseInt(lblTotalReceipts.getText().replaceAll("[^0-9]", ""));
            // Export the data
             ExportUtils.exportExpenseStatistics(expenseDetails,
                     dpExpenseFromDate.getValue(),
                     dpExpenseToDate.getValue(),
                     totalExpense,
                     totalReceipts);

//            ExportUtils.exportExpenseStatistics(tblExpenseDetails.getItems(),
//                    dpExpenseFromDate.getValue(),
//                    dpExpenseToDate.getValue(),
//                    new BigDecimal(lblTotalExpense.getText().replace("₫", "").replace(",", "")),
//                    Integer.parseInt(lblTotalReceipts.getText()));

            NotificationUtil.showSuccessNotification("Xuất báo cáo thành công",
                    "Báo cáo chi phí đã được xuất thành công");
        } catch (Exception e) {
            NotificationUtil.showErrorNotification("Lỗi xuất báo cáo",
                    "Đã xảy ra lỗi khi xuất báo cáo chi phí: " + e.getMessage());
        }
    }

    private void filterExpenseData() {
        LocalDate fromDate = dpExpenseFromDate.getValue();
        LocalDate toDate = dpExpenseToDate.getValue();
        String viewType = cboExpenseView.getValue();

        if (fromDate == null || toDate == null || viewType == null) {
            NotificationUtil.showErrorNotification("Thiếu thông tin", "Vui lòng chọn đầy đủ thông tin để lọc");
            return;
        }

        if (fromDate.isAfter(toDate)) {
            NotificationUtil.showErrorNotification("Ngày không hợp lệ", "Ngày bắt đầu phải trước ngày kết thúc");
            return;
        }

        try {
            // Get expense statistics data
            List<StatisticDTO> expenseStats = statisticBUS.getExpenseStatistics(fromDate, toDate, viewType);
            tblExpenseDetails.setItems(FXCollections.observableArrayList(expenseStats));

            // Update bar chart
            populateExpenseBarChart(expenseStats);

            // Update pie chart with manufacturer data
            Map<String, BigDecimal> manufacturerExpense = statisticBUS.getExpenseByManufacturerStatistics(fromDate, toDate);
            populateExpensePieChart(manufacturerExpense);

            // Update summary labels
            BigDecimal totalExpense = BigDecimal.ZERO;
            int totalReceipts = 0;

            for (StatisticDTO stat : expenseStats) {
                totalExpense = totalExpense.add(stat.getAmount());
                totalReceipts += stat.getCount();
            }

            lblTotalExpense.setText(CurrencyUtils.formatVND(totalExpense));
            lblTotalReceipts.setText(String.valueOf(totalReceipts));

            if (totalReceipts > 0) {
                BigDecimal averageExpense = totalExpense.divide(BigDecimal.valueOf(totalReceipts), 0, BigDecimal.ROUND_HALF_UP);
                lblAverageExpense.setText(CurrencyUtils.formatVND(averageExpense));
            } else {
                lblAverageExpense.setText("0₫");
            }

        } catch (Exception e) {
            NotificationUtil.showErrorNotification("Lỗi truy vấn dữ liệu",
                    "Đã xảy ra lỗi khi lấy dữ liệu chi phí: " + e.getMessage());
        }
    }

    private void populateExpenseBarChart(List<StatisticDTO> expenseStats) {
        chartExpense.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Chi phí");

        for (StatisticDTO stat : expenseStats) {
            series.getData().add(new XYChart.Data<>(stat.getPeriod(), stat.getAmount()));
        }

        chartExpense.getData().add(series);
    }

    private void populateExpensePieChart(Map<String, BigDecimal> manufacturerExpense) {
        pieChartExpense.getData().clear();

        for (Map.Entry<String, BigDecimal> entry : manufacturerExpense.entrySet()) {
            PieChart.Data slice = new PieChart.Data(entry.getKey(), entry.getValue().doubleValue());
            pieChartExpense.getData().add(slice);
        }
    }

    // ======== PROFIT TAB HANDLERS ========
    private void handleProfitFilter(ActionEvent event) {
        filterProfitData();
    }

    private void handleProfitExport(ActionEvent event) {
        try {
            // Export profit statistics
            List<StatisticDTO> profitDetails = tblProfitDetails.getItems();
            BigDecimal totalRevenue = CurrencyUtils.parseCurrencyToNumber(lblProfitTotalRevenue.getText());
            BigDecimal totalExpense = CurrencyUtils.parseCurrencyToNumber(lblProfitTotalExpense.getText());
            BigDecimal totalProfit = CurrencyUtils.parseCurrencyToNumber(lblTotalProfit.getText());
            ExportUtils.exportProfitStatistics(profitDetails,
                    dpProfitFromDate.getValue(),
                    dpProfitToDate.getValue(),
                    totalRevenue,
                    totalExpense,
                    totalProfit);
            NotificationUtil.showSuccessNotification("Xuất báo cáo thành công",
                    "Báo cáo lợi nhuận đã được xuất thành công");
        } catch (Exception e) {
            NotificationUtil.showErrorNotification("Lỗi xuất báo cáo",
                    "Đã xảy ra lỗi khi xuất báo cáo lợi nhuận: " + e.getMessage());
        }
    }

    private void filterProfitData() {
        LocalDate fromDate = dpProfitFromDate.getValue();
        LocalDate toDate = dpProfitToDate.getValue();
        String viewType = cboProfitView.getValue();

        if (fromDate == null || toDate == null || viewType == null) {
            NotificationUtil.showErrorNotification("Thiếu thông tin", "Vui lòng chọn đầy đủ thông tin để lọc");
            return;
        }

        if (fromDate.isAfter(toDate)) {
            NotificationUtil.showErrorNotification("Ngày không hợp lệ", "Ngày bắt đầu phải trước ngày kết thúc");
            return;
        }

        try {
            // Get profit statistics data
            List<StatisticDTO> profitStats = statisticBUS.getProfitStatistics(fromDate, toDate, viewType);
            tblProfitDetails.setItems(FXCollections.observableArrayList(profitStats));

            // Update line chart
            populateProfitLineChart(profitStats);

            // Update comparison bar chart
            populateComparisonBarChart(profitStats);

            // Update summary labels
            BigDecimal totalRevenue = BigDecimal.ZERO;
            BigDecimal totalExpense = BigDecimal.ZERO;
            BigDecimal totalProfit = BigDecimal.ZERO;

            for (StatisticDTO stat : profitStats) {
                totalRevenue = totalRevenue.add(stat.getRevenue());
                totalExpense = totalExpense.add(stat.getExpense());
                totalProfit = totalProfit.add(stat.getProfit());
            }

            lblProfitTotalRevenue.setText(CurrencyUtils.formatVND(totalRevenue));
            lblProfitTotalExpense.setText(CurrencyUtils.formatVND(totalExpense));
            lblTotalProfit.setText(CurrencyUtils.formatVND(totalProfit));

        } catch (Exception e) {
            NotificationUtil.showErrorNotification("Lỗi truy vấn dữ liệu",
                    "Đã xảy ra lỗi khi lấy dữ liệu lợi nhuận: " + e.getMessage());
        }
    }

    private void populateProfitLineChart(List<StatisticDTO> profitStats) {
        chartProfit.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Lợi nhuận");

        for (StatisticDTO stat : profitStats) {
            series.getData().add(new XYChart.Data<>(stat.getPeriod(), stat.getProfit()));
        }

        chartProfit.getData().add(series);
    }

    private void populateComparisonBarChart(List<StatisticDTO> profitStats) {
        chartCompare.getData().clear();

        XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
        revenueSeries.setName("Doanh thu");

        XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
        expenseSeries.setName("Chi phí");

        for (StatisticDTO stat : profitStats) {
            revenueSeries.getData().add(new XYChart.Data<>(stat.getPeriod(), stat.getRevenue()));
            expenseSeries.getData().add(new XYChart.Data<>(stat.getPeriod(), stat.getExpense()));
        }

        chartCompare.getData().addAll(revenueSeries, expenseSeries);
    }
        @FXML private BarChart<String, Number> chartProductSales;
        @FXML private CategoryAxis xAxisProduct;
        @FXML private NumberAxis yAxisProduct;
        @FXML private PieChart pieChartProduct;

        // Filter controls
        @FXML private DatePicker dpProductFromDate;
        @FXML private DatePicker dpProductToDate;
        @FXML private ComboBox<String> cboProductView;
        @FXML private Button btnProductFilter;
        @FXML private Button btnProductExport;

        // Summary labels
        @FXML private Label lblTotalQuantity;
        @FXML private Label lblTotalProducts;
        @FXML private Label lblAverageQuantity;

        // Table components
        @FXML private TableView<ProductStatisticDTO> tblProductDetails;
        @FXML private TableColumn<ProductStatisticDTO, String> colProductId;
        @FXML private TableColumn<ProductStatisticDTO, String> colProductName;
        @FXML private TableColumn<ProductStatisticDTO, String> colProductCategory;
        @FXML private TableColumn<ProductStatisticDTO, Integer> colProductQuantity;

    private void filterProductData() {
        LocalDate fromDate = dpProductFromDate.getValue();
        LocalDate toDate = dpProductToDate.getValue();
        String viewType = cboProductView.getValue();

        if (fromDate == null || toDate == null || viewType == null) {
            NotificationUtil.showErrorNotification(
                    "Thiếu thông tin",
                    "Vui lòng chọn đầy đủ thông tin để lọc"
            );
            return;
        }

        if (fromDate.isAfter(toDate)) {
            NotificationUtil.showErrorNotification(
                    "Ngày không hợp lệ",
                    "Ngày bắt đầu phải trước ngày kết thúc"
            );
            return;
        }

        try {
            // Get product sales statistics
            List<ProductStatisticDTO> productStats = statisticBUS.getProductSalesStatistics(
                    fromDate, toDate, viewType
            );
            tblProductDetails.setItems(FXCollections.observableArrayList(productStats));

            // Update bar chart
            updateBarChart(productStats);

            // Update pie chart
            Map<String, Integer> categoryStats = statisticBUS.getProductSalesByCategory(
                    fromDate, toDate
            );
            updatePieChart(categoryStats);

            // Update summary
            updateSummary(productStats);

        } catch (Exception e) {
            NotificationUtil.showErrorNotification(
                    "Lỗi truy vấn dữ liệu",
                    "Đã xảy ra lỗi khi lấy dữ liệu sản phẩm: " + e.getMessage()
            );
        }
    }

    private void updateBarChart(List<ProductStatisticDTO> stats) {
        chartProductSales.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Số lượng bán");

        // Show top 10 products by quantity
        stats.stream()
                .sorted((a, b) -> b.getQuantity() - a.getQuantity())
                .limit(10)
                .forEach(stat -> series.getData().add(
                        new XYChart.Data<>(stat.getProductName(), stat.getQuantity())
                ));

        chartProductSales.getData().add(series);
    }

    private void updatePieChart(Map<String, Integer> categoryStats) {
        pieChartProduct.getData().clear();
        categoryStats.forEach((category, quantity) ->
                pieChartProduct.getData().add(new PieChart.Data(category, quantity))
        );
    }

    private void updateSummary(List<ProductStatisticDTO> stats) {
        int totalQuantity = stats.stream()
                .mapToInt(ProductStatisticDTO::getQuantity)
                .sum();

        int totalProducts = stats.size();

        double averageQuantity = totalProducts > 0 ?
                (double) totalQuantity / totalProducts : 0;

        lblTotalQuantity.setText(String.format("%,d", totalQuantity));
        lblTotalProducts.setText(String.format("%,d", totalProducts));
        lblAverageQuantity.setText(String.format("%.1f", averageQuantity));
    }

    private void exportProductData() {
        try {
            // Export product sales statistics
            List<ProductStatisticDTO> productStats = tblProductDetails.getItems();

            if (productStats.isEmpty()) {
                NotificationUtil.showErrorNotification(
                        "Không có dữ liệu",
                        "Không có dữ liệu để xuất báo cáo"
                );
                return;
            }
            // Export the data
            ExportUtils.exportProductSalesStatistics(
                    productStats,
                    dpProductFromDate.getValue(),
                    dpProductToDate.getValue(),
                    Integer.parseInt(lblTotalQuantity.getText().replace(",", "")),
                    Integer.parseInt(lblTotalProducts.getText().replace(",", "")),
                    Double.parseDouble(lblAverageQuantity.getText())
            );

            NotificationUtil.showSuccessNotification(
                    "Xuất báo cáo thành công",
                    "Báo cáo thống kê sản phẩm đã được xuất thành công"
            );
        } catch (Exception e) {
            NotificationUtil.showErrorNotification(
                    "Lỗi xuất báo cáo",
                    "Đã xảy ra lỗi khi xuất báo cáo sản phẩm: " + e.getMessage()
            );
        }
    }
}