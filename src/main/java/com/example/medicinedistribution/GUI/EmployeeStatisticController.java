package com.example.medicinedistribution.GUI;

import com.example.medicinedistribution.BUS.BUSFactory;
import com.example.medicinedistribution.BUS.Interface.EmployeeBUS;
import com.example.medicinedistribution.BUS.Interface.PayrollBUS;
import com.example.medicinedistribution.DTO.BonusDTO;
import com.example.medicinedistribution.DTO.EmployeeDTO;
import com.example.medicinedistribution.DTO.PayrollDTO;
import com.example.medicinedistribution.Util.CurrencyUtils;
import com.example.medicinedistribution.Util.ExportUtils;
import com.example.medicinedistribution.Util.NotificationUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import lombok.extern.slf4j.Slf4j;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.text.Text;

import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class EmployeeStatisticController {

    @FXML
    private ComboBox<Integer> cboMonth;

    @FXML
    private ComboBox<Integer> cboYear;

    @FXML
    private Button btnSearch;

    @FXML
    private Button btnRefresh;

    @FXML
    private Button btnExportReport;

    @FXML
    private Label lblCurrentEmployees;

    @FXML
    private Label lblNewEmployees;

    @FXML
    private Label lblTotalSalary;

    @FXML
    private BarChart<String, Number> barChartEmployees;

    @FXML
    private CategoryAxis xAxisTime;

    @FXML
    private NumberAxis yAxisCount;

    @FXML
    private PieChart pieChartStatus;

    @FXML
    private PieChart pieChartDepartment;

    @FXML private Label lblTotalBonus;
    @FXML private Label lblMaxBonus;
    @FXML private Label lblBonusEmployeeCount;
    @FXML private ComboBox<String> cboBonusDepartment;
    @FXML private ComboBox<String> cboBonusType;
    @FXML private TableView<BonusDTO> tblBonusDetails;
    @FXML private TableColumn<BonusDTO, Integer> colEmployeeId;
    @FXML private TableColumn<BonusDTO, String> colEmployeeName;
    @FXML private TableColumn<BonusDTO, String> colDepartment;
    @FXML private TableColumn<BonusDTO, String> colPosition;
    @FXML private TableColumn<BonusDTO, String> colBonusType;
    @FXML private TableColumn<BonusDTO, BigDecimal> colBonusAmount;
    @FXML private TableColumn<BonusDTO, LocalDate> colBonusDate;
    @FXML private TableColumn<BonusDTO, String> colBonusReason;

    private ObservableList<BonusDTO> bonusList = FXCollections.observableArrayList();


    private BUSFactory busFactory;
    private EmployeeBUS employeeBUS;
    private PayrollBUS payrollBUS;

    private List<PayrollDTO> payrollList = new ArrayList<>();
    private List<EmployeeDTO> employeeList = new ArrayList<>();
//    private List<BonusDTO> bonusList = new ArrayList<>();

    public EmployeeStatisticController(BUSFactory busFactory) {
        this.busFactory = busFactory;
        // Constructor
    }

    private void setupComboBoxes() {
        // Setup month combo box
        List<Integer> months = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            months.add(i);
        }
        cboMonth.setItems(FXCollections.observableArrayList(months));

        // Setup year combo box
        List<Integer> years = new ArrayList<>();
        int currentYear = LocalDate.now().getYear();
        for (int i = currentYear - 5; i <= currentYear; i++) {
            years.add(i);
        }
        cboYear.setItems(FXCollections.observableArrayList(years));

        // Set default values (current month and year)
        cboMonth.setValue(LocalDate.now().getMonthValue());
        cboYear.setValue(LocalDate.now().getYear());
    }

    public void initialize() {
        // Initialize BUS objects
        employeeBUS = busFactory.getEmployeeBUS();
        payrollBUS = busFactory.getPayrollBUS();
        disableChartAnimation();

        setupComboBoxes();
        setupButtons();

        // Load initial data with current month/year
        refreshData();
    }

    private void setupButtons() {
        btnSearch.setOnAction(this::handleSearchAction);
        btnRefresh.setOnAction(event -> refreshData());
        btnExportReport.setOnAction(this::handleExportAction);
    }

    private void refreshData() {
        cboMonth.setValue(LocalDate.now().getMonthValue());
        cboYear.setValue(LocalDate.now().getYear());
        loadData(LocalDate.now().getMonthValue(), LocalDate.now().getYear());
    }

    @FXML
    private void handleSearchAction(ActionEvent event) {
        Integer selectedMonth = cboMonth.getValue();
        Integer selectedYear = cboYear.getValue();

        if (selectedMonth == null || selectedYear == null) {
            NotificationUtil.showWarningNotification("Thông báo", "Vui lòng chọn đầy đủ tháng và năm");
            return;
        }

        loadData(selectedMonth, selectedYear);
    }

    private void loadData(int month, int year) {
        try {
            // Get all employees
            employeeList = employeeBUS.findAll();

            // Get first and last day of month
            YearMonth yearMonth = YearMonth.of(year, month);
            LocalDate startDate = yearMonth.atDay(1);
            LocalDate endDate = yearMonth.atEndOfMonth();

            // Get payroll data for the month
            payrollList = payrollBUS.findByPeriod(month, year);

//            bonusList = busFactory.getBonusBUS().getByMothYear(month, year);

            // Update UI elements
            updateEmployeeStatistics(startDate, endDate);
            updateSalaryStatistics();
            updateCharts(startDate, endDate);
            updateBonusStatistics();

        } catch (Exception e) {
            log.error("Error loading data", e);
            NotificationUtil.showErrorNotification("Lỗi", "Không thể tải dữ liệu: " + e.getMessage());
        }
    }

    private void updateEmployeeStatistics(LocalDate startDate, LocalDate endDate) {
        // Count active employees
        long currentEmployees = employeeList.stream()
                .filter(emp -> emp.getStatus() == 1) // Assuming 1 = active
                .count();

        // Count new employees in the period
        long newEmployees = employeeList.stream()
                .filter(emp -> emp.getHireDate() != null &&
                      !emp.getHireDate().isBefore(startDate) &&
                      !emp.getHireDate().isAfter(endDate))
                .count();

        // Update labels
        lblCurrentEmployees.setText(String.valueOf(currentEmployees));
        lblNewEmployees.setText(String.valueOf(newEmployees));
    }

    private void updateSalaryStatistics() {
        BigDecimal totalSalary = BigDecimal.ZERO;

        if (!payrollList.isEmpty()) {
            // Sum net income for all employees
            totalSalary = payrollList.stream()
                    .map(PayrollDTO::getNet_income)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        // Update total salary label with formatted currency
        lblTotalSalary.setText(CurrencyUtils.formatVND(totalSalary));
    }

    private void updateCharts(LocalDate startDate, LocalDate endDate) {
        updateEmployeeBarChart(startDate, endDate);
        updateStatusPieChart();
        updateDepartmentPieChart();
    }

    private void updateEmployeeBarChart(LocalDate startDate, LocalDate endDate) {
        barChartEmployees.getData().clear();
        int selectedMonth = cboMonth.getValue();
        int selectedYear = cboYear.getValue();
        YearMonth selectedYearMonth = YearMonth.of(selectedYear, selectedMonth);

        // Create series for the chart
        XYChart.Series<String, Number> currentSeries = new XYChart.Series<>();
        currentSeries.setName("Nhân viên hiện tại");

        XYChart.Series<String, Number> newSeries = new XYChart.Series<>();
        newSeries.setName("Nhân viên mới");

        XYChart.Series<String, Number> leftSeries = new XYChart.Series<>();
        leftSeries.setName("Nhân viên nghỉ việc");

        // Use fewer data points to prevent label overlap
        List<LocalDate> keyDates = generateKeyDates(startDate, endDate);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

        for (LocalDate date : keyDates) {
            String dateLabel = date.format(formatter);
            LocalDate currentDate = date;

            // Count current employees at this date
            long currentCount = employeeList.stream()
                    .filter(emp -> emp.getStatus() == 1 &&
                            (emp.getHireDate() == null ||
                                    !YearMonth.from(emp.getHireDate()).isAfter(selectedYearMonth)))
                    .count();

            // Count new employees for this period
            long newCount = employeeList.stream()
                    .filter(emp -> emp.getHireDate() != null &&
                            !emp.getHireDate().isBefore(startDate) &&
                            !emp.getHireDate().isAfter(currentDate))
                    .count();

            currentSeries.getData().add(new XYChart.Data<>(dateLabel, currentCount));
            newSeries.getData().add(new XYChart.Data<>(dateLabel, newCount));
            leftSeries.getData().add(new XYChart.Data<>(dateLabel, 0));
        }

        barChartEmployees.setCategoryGap(50); // Increase gap between categories
        barChartEmployees.getData().addAll(currentSeries, newSeries, leftSeries);

        // Rotate x-axis labels to prevent overlap
        xAxisTime.setTickLabelRotation(45);


        // Rotate labels and increase bottom padding for better visibility
        xAxisTime.setTickLabelRotation(45);
        barChartEmployees.setPadding(new Insets(10, 10, 30, 10)); // More padding at bottom

        // Add CSS for better label placement
        barChartEmployees.lookupAll(".axis-label")
                .forEach(node -> node.setStyle("-fx-font-size: 11px;"));
    }
    private List<LocalDate> generateKeyDates(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> keyDates = new ArrayList<>();
        keyDates.add(startDate); // First day

        // Add middle points - divide month into max 3-4 points
        int daysInMonth = endDate.getDayOfMonth();
        if (daysInMonth > 10) {
            keyDates.add(startDate.plusDays(daysInMonth / 3));
            keyDates.add(startDate.plusDays(2 * daysInMonth / 3));
        } else if (daysInMonth > 5) {
            keyDates.add(startDate.plusDays(daysInMonth / 2));
        }

        keyDates.add(endDate); // Last day
        return keyDates;
    }

    private void updateStatusPieChart() {
        pieChartStatus.getData().clear();

        // Count employees by status
        Map<Integer, Long> statusCounts = employeeList.stream()
                .collect(Collectors.groupingBy(EmployeeDTO::getStatus, Collectors.counting()));

        // Create pie chart data
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        Map<Integer, String> statusNames = new HashMap<>();
        statusNames.put(1, "Đang làm việc");
        statusNames.put(0, "Đã nghỉ việc");
        statusNames.put(2, "Nghỉ thai sản");
        statusNames.put(3, "Tạm nghỉ");

        for (Map.Entry<Integer, Long> entry : statusCounts.entrySet()) {
            String statusName = statusNames.getOrDefault(entry.getKey(), "Trạng thái " + entry.getKey());
            String label = statusName + " (" + entry.getValue() + ")";
            pieData.add(new PieChart.Data(label, entry.getValue()));
        }

        pieChartStatus.setData(pieData);
    }
    private void updateBonusStatistics() {
        bonusList = FXCollections.observableArrayList(busFactory.getBonusBUS().getByMothYear(
                cboMonth.getValue(), cboYear.getValue()));
        // Check if bonusList is null or empty
        if (bonusList.isEmpty()) {
            lblTotalBonus.setText("0 ₫");
            lblMaxBonus.setText("0 ₫");
            lblBonusEmployeeCount.setText("0");
            return;
        }

        // Calculate total bonus
        BigDecimal totalBonus = bonusList.stream()
                .map(BonusDTO::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Find max bonus
        Optional<BigDecimal> maxBonus = bonusList.stream()
                .map(BonusDTO::getAmount)
                .filter(Objects::nonNull)
                .max(BigDecimal::compareTo);

        // Count distinct employees receiving bonuses
        long employeeCount = bonusList.stream()
                .map(BonusDTO::getEmployee_id)
                .distinct()
                .count();

        // Format as currency
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        lblTotalBonus.setText(currencyFormat.format(totalBonus));
        lblMaxBonus.setText(currencyFormat.format(maxBonus.orElse(BigDecimal.ZERO)));
        lblBonusEmployeeCount.setText(String.valueOf(employeeCount));

        // Set up table columns
        colEmployeeId.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getEmployee_id()).asObject());
        colEmployeeName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmployee_name()));
        colDepartment.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDepartment_name()));
        colPosition.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPosition_name()));
        colBonusType.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBonus_type_name()));
        colBonusAmount.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getAmount()));
        colBonusDate.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDate()));

        // Format amount column to display currency
        colBonusAmount.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(currencyFormat.format(amount));
                }
            }
        });

        // Format date column
        colBonusDate.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                }
            }
        });

        // Update table content
        tblBonusDetails.setItems(bonusList);
    }
//    private void updateDepartmentPieChart() {
//        pieChartDepartment.getData().clear();
//
//        // Only count active employees
//        Map<Integer, Long> deptCounts = employeeList.stream()
//                .filter(emp -> emp.getStatus() == 1) // Active employees only
//                .collect(Collectors.groupingBy(EmployeeDTO::getDepartmentId, Collectors.counting()));
//
//        // Create pie chart data
//        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
//
//        // Get department names from business layer
//        for (Map.Entry<Integer, Long> entry : deptCounts.entrySet()) {
//            Integer deptId = entry.getKey();
//            Long count = entry.getValue();
//
//            // Get department name
//            String deptName = getDepartmentName(deptId);
//            String label = deptName + " (" + count + ")";
//            pieData.add(new PieChart.Data(label, count));
//        }
//        for (PieChart.Data data : pieChartDepartment.getData()) {
//            Tooltip tooltip = new Tooltip(data.getName());
//            Tooltip.install(data.getNode(), tooltip);
//        }
//
//        pieChartDepartment.setData(pieData);
//    }
private void updateDepartmentPieChart() {
    pieChartDepartment.getData().clear();

    // Set preferred size for better label distribution
    pieChartDepartment.setPrefSize(500, 400);

    // Chỉ đếm những nhân viên đang hoạt động
    Map<Integer, Long> deptCounts = employeeList.stream()
            .filter(emp -> emp.getStatus() == 1)
            .collect(Collectors.groupingBy(EmployeeDTO::getDepartmentId, Collectors.counting()));

    // Tạo dữ liệu cho PieChart
    ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

    // Lấy tên phòng ban từ Business Layer và tạo tooltip
    for (Map.Entry<Integer, Long> entry : deptCounts.entrySet()) {
        Integer deptId = entry.getKey();
        Long count = entry.getValue();

        String deptName = getDepartmentName(deptId);

        // Giới hạn ký tự của tên phòng ban nếu quá dài
        if (deptName.length() > 15) {
            deptName = deptName.substring(0, 15) + "...";
        }

        // Thêm vào PieChart
        PieChart.Data data = new PieChart.Data(deptName + " (" + count + ")", count);
        pieData.add(data);

        // Thêm Tooltip để hiển thị thông tin chi tiết
        Tooltip tooltip = new Tooltip(getDepartmentName(deptId) + ": " + count + " nhân viên");
        Tooltip.install(data.getNode(), tooltip);
    }

    pieChartDepartment.setData(pieData);
}




    private String getDepartmentName(Integer departmentId) {

        return busFactory.getDepartmentBUS().findById(departmentId).getDepartmentName();
    }

    @FXML
    private void handleExportAction(ActionEvent event) {
        Integer selectedMonth = cboMonth.getValue();
        Integer selectedYear = cboYear.getValue();

        if (selectedMonth == null || selectedYear == null) {
            NotificationUtil.showWarningNotification("Thông báo", "Vui lòng chọn đầy đủ tháng và năm");
            return;
        }

        try {
            // Export employee statistics report
            exportEmployeeStatistics(selectedMonth, selectedYear);
        } catch (Exception e) {
            log.error("Error exporting report", e);
            NotificationUtil.showErrorNotification("Lỗi",
                "Không thể xuất báo cáo: " + e.getMessage());
        }
    }

    private void exportEmployeeStatistics(int month, int year) throws Exception {
        // Get period dates
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        // Get totals for report
        int currentEmployees = Integer.parseInt(lblCurrentEmployees.getText());
        int newEmployees = Integer.parseInt(lblNewEmployees.getText());
        int leftEmployees = 0; // Calculate or get from business layer
        // số nhân viên nghỉ thai sản
        int maternityLeaveEmployees = (int) employeeList.stream()
                .filter(emp -> emp.getStatus() == 2)
                .count();

        BigDecimal totalSalary = BigDecimal.ZERO;
        BigDecimal totalDeductions = BigDecimal.ZERO;
        for (PayrollDTO payroll : payrollList) {
            if (payroll.getNet_income() != null) {
                totalSalary = totalSalary.add(payroll.getNet_income());
            }

            // Calculate total deductions (tax + insurance + penalties)
            BigDecimal taxAmount = payroll.getIncome_tax() != null ? payroll.getIncome_tax() : BigDecimal.ZERO;
            BigDecimal insuranceAmount = payroll.getTotal_insurance() != null ? payroll.getTotal_insurance() : BigDecimal.ZERO;
            BigDecimal penaltyAmount = payroll.getPenalty_amount() != null ? payroll.getPenalty_amount() : BigDecimal.ZERO;

            totalDeductions = totalDeductions.add(taxAmount).add(insuranceAmount).add(penaltyAmount);
        }
        ArrayList<BonusDTO> bonusList = new ArrayList<>();
        for (BonusDTO bonus : this.bonusList) {
            if (bonus.getDate() != null && !bonus.getDate().isBefore(startDate) && !bonus.getDate().isAfter(endDate)) {
                bonusList.add(bonus);
            }
        }

        // Call ExportUtils or similar utility to generate the report
        // This is a placeholder - you'll need to implement the actual export functionality
        ExportUtils.exportEmployeeStatistics(
            startDate,
            endDate,
            currentEmployees,
            newEmployees,
                maternityLeaveEmployees,
            totalSalary,
            totalDeductions,
            bonusList
        );
    }

    public void disableChartAnimation() {
        barChartEmployees.setAnimated(false);
        pieChartStatus.setAnimated(false);
        pieChartDepartment.setAnimated(false);

        // Use same settings as in SalesStatisticController
        pieChartStatus.setLabelLineLength(0);
        pieChartStatus.setLabelsVisible(false);
        pieChartStatus.setLegendVisible(true);

        pieChartDepartment.setLabelLineLength(0);
        pieChartDepartment.setLabelsVisible(false);
        pieChartDepartment.setLegendVisible(true);
    }

}