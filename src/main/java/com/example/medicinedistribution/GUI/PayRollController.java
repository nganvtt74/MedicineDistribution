package com.example.medicinedistribution.GUI;

import com.example.medicinedistribution.BUS.BUSFactory;
import com.example.medicinedistribution.DTO.*;
import com.example.medicinedistribution.GUI.Component.CurrencyTextField;
import com.example.medicinedistribution.Util.GenericTablePrinter;
import com.example.medicinedistribution.Util.JsonUtil;
import com.example.medicinedistribution.Util.NotificationUtil;
import com.example.medicinedistribution.Util.PayrollExportUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Slf4j
public class PayRollController {
    @FXML
    private Button btnClose;

    @FXML
    private Button btnExport;

    @FXML
    private Button btnFilter;

    @FXML
    private Button btnSave;

    @FXML
    private ComboBox<Integer> cbMonth;

    @FXML
    private ComboBox<Integer> cbYear;

    @FXML
    private TableColumn<EmployeeDTO, Integer> colActualWorkDays;

    @FXML
    private TableColumn<EmployeeDTO, BigDecimal> colAdditionalIncome;

    @FXML
    private TableColumn<EmployeeDTO, BigDecimal> colBaseSalary;

    @FXML
    private TableColumn<EmployeeDTO, BigDecimal> colBonus;

    @FXML
    private TableColumn<EmployeeDTO, String> colDepartment;

    @FXML
    private TableColumn<EmployeeDTO, String> colFullName;

    @FXML
    private TableColumn<EmployeeDTO, BigDecimal> colGasAllowance;

    @FXML
    private TableColumn<EmployeeDTO, BigDecimal> colHealthInsurance;

    @FXML
    private TableColumn<EmployeeDTO,BigDecimal > colIncomeTax;

    @FXML
    private TableColumn<EmployeeDTO, BigDecimal> colInsuranceSalary;

    @FXML
    private TableColumn<EmployeeDTO, Integer> colLateArrival;

    @FXML
    private TableColumn<EmployeeDTO, BigDecimal> colMealAllowance;

    @FXML
    private TableColumn<EmployeeDTO, BigDecimal> colNetSalary;

    @FXML
    private TableColumn<EmployeeDTO, Integer> colNo;

    @FXML
    private TableColumn<EmployeeDTO, BigDecimal> colOtherAllowance;

    @FXML
    private TableColumn<EmployeeDTO, BigDecimal> colPenalty;

    @FXML
    private TableColumn<EmployeeDTO, BigDecimal> colPhoneAllowance;

    @FXML
    private TableColumn<EmployeeDTO, String> colPosition;

    @FXML
    private TableColumn<EmployeeDTO, BigDecimal> colPositionAllowance;

    @FXML
    private TableColumn<EmployeeDTO, BigDecimal> colResponsibilityAllowance;

    @FXML
    private TableColumn<EmployeeDTO,BigDecimal> colSocialInsurance;

    @FXML
    private TableColumn<EmployeeDTO, BigDecimal> colTotalAllowance;

    @FXML
    private TableColumn<EmployeeDTO, BigDecimal> colTotalDeduction;

    @FXML
    private TableColumn<EmployeeDTO, BigDecimal> colTotalIncome;

    @FXML
    private TableColumn<EmployeeDTO, BigDecimal> colTotalInsurance;

    @FXML
    private TableColumn<EmployeeDTO, Integer> colUnauthorizedLeave;

    @FXML
    private TableColumn<EmployeeDTO, BigDecimal> colUnemploymentInsurance;

    @FXML
    private TableView<EmployeeDTO> tblSalary;

    private SystemConfig currentConfig;

    private LocalDate globalStartDate;
    private LocalDate globalEndDate;


    private static final String CONFIG_FILE = "src/main/resources/config/system_config.json";


    private BUSFactory busFactory;
    private List<EmployeeDTO> employeeList;
    private HashMap<Integer,DepartmentDTO> departmentMap = new HashMap<>();
    private HashMap<Integer,PositionDTO> positionMap = new HashMap<>();
    private List<AttendanceDTO> attendanceList = new ArrayList<>();
    private HashMap<String,AllowanceDTO> allowanceMap = new HashMap<>();
    private List<BonusDTO> bonusList = new ArrayList<>();
    private List<TaxRateDTO> taxRates = new ArrayList<>();
    private HashMap<String,InsuranceDTO> insurances =  new HashMap<>();

    public PayRollController(BUSFactory busFactory) {
        this.busFactory = busFactory;
    }


    @FXML
    public void initialize() {
        employeeList = busFactory.getEmployeeBUS().findAll();

        loadConfig();
        loadInsuranceConfig();
        loadTaxConfig();
        LocalDate[] salaryPeriod = getSalaryPeriod(4, 2025); // May 2023
        globalStartDate = salaryPeriod[0];
        globalEndDate = salaryPeriod[1];
        log.info("Salary period: {} to {}", globalStartDate, globalEndDate);
        // Populate the table with employee data
        loadData();
        setupColumns();
        loadTable();
        setupComboBoxes();
        setupFilterListeners();
        btnSave.setOnAction(event -> {
            if (NotificationUtil.showConfirmation("Xác nhận", "Bạn có chắc chắn muốn lưu bảng lương không?")) {
                if (NotificationUtil.showConfirmation("Xác nhận", "Bạn có chắc chắn muốn lưu bảng lương không?")) {
                    if (NotificationUtil.showConfirmation("Xác nhận","Sau khi lưu bảng lương, bạn không thể sửa lại bảng lương này. Bạn có chắc chắn muốn lưu không?")) {
                        savePayrollData();
                    }
                }
            }
        });
        btnExport.setOnAction(event -> {
            Integer selectedMonth = cbMonth.getValue();
            Integer selectedYear = cbYear.getValue();

            if (selectedMonth == null || selectedYear == null) {
                NotificationUtil.showErrorNotification("Error", "Please select month and year");
                return;
            }

            PayrollExportUtils.exportPayroll(tblSalary, selectedMonth, selectedYear);
        });

    }

    private void loadTaxConfig() {
        try {
            JsonNode taxConfig = JsonUtil.readJsonFromResource("/config/tax_rate.json");
            taxRates.clear();

            for (JsonNode taxNode : taxConfig) {
                TaxRateDTO taxRate = TaxRateDTO.builder()
                        .min(taxNode.get("min").asLong())
                        .max(taxNode.get("max").isNull() ? Long.MAX_VALUE : taxNode.get("max").asLong())
                        .taxRate(taxNode.get("tax_rate").asInt())
                        .fixedDeduction(taxNode.get("fixed_deduction").asLong())
                        .build();
                taxRates.add(taxRate);
            }

            log.info("Loaded {} tax rate brackets", taxRates.size());
        } catch (Exception e) {
            log.error("Error loading tax configuration: {}", e.getMessage());
            NotificationUtil.showErrorNotification("Lỗi", "Không thể tải cấu hình thuế: " + e.getMessage());
        }
    }

    /**
     * Loads insurance configuration data from the insurance.json file
     */
    private void loadInsuranceConfig() {
        try {

            JsonNode insuranceConfig = JsonUtil.readJsonFromResource("/config/insurance.json");
            insurances.clear();

            for (JsonNode insuranceNode : insuranceConfig) {
                InsuranceDTO insurance = InsuranceDTO.builder()
                        .insuranceId(insuranceNode.get("insurance_id").asText())
                        .percentage(insuranceNode.get("percentage").asDouble())
                        .build();
                insurances.put(insurance.getInsuranceId(), insurance);
            }

            log.info("Loaded {} insurance types", insurances.size());
        } catch (Exception e) {
            log.error("Error loading insurance configuration: {}", e.getMessage());
            NotificationUtil.showErrorNotification("Lỗi", "Không thể tải cấu hình bảo hiểm: " + e.getMessage());
        }
    }

    public void loadData(){

        if (departmentMap != null) {
            departmentMap.clear();
        }
        if (positionMap != null) {
            positionMap.clear();
        }
        if (attendanceList != null) {
            attendanceList.clear();
        }
        if (employeeList != null) {
            employeeList.clear();
        }
        if (allowanceMap != null) {
            allowanceMap.clear();
        }

        // Load data from the database
        employeeList = busFactory.getEmployeeBUS().findAll();
        for(DepartmentDTO department : busFactory.getDepartmentBUS().findAll()){
            departmentMap.put(department.getDepartmentId(), department);
        }
        for(PositionDTO position : busFactory.getPositionBUS().findAll()){
            positionMap.put(position.getPositionId(), position);
        }
        attendanceList = busFactory.getAttendanceBUS().getAllAttendanceInMonth(LocalDate.now().getMonthValue(), LocalDate.now().getYear());
        for(AllowanceDTO allowance : busFactory.getAllowanceBUS().findAll()){
            allowanceMap.put(allowance.getName(), allowance);
        }
        for (EmployeeDTO employee : employeeList) {
            employee.setDependentCount(
                    busFactory.getDependentsBUS().countDependentByEmployeeId(employee.getEmployeeId()));
        }
        bonusList = busFactory.getBonusBUS().findByDateRange(globalStartDate, globalEndDate);
   }

    public void setupColumns() {
        // Set up the columns in the table view
        // Tự đánh số tăng dần cho cột No
        colNo.setCellValueFactory(cellData -> {
            int index = tblSalary.getItems().indexOf(cellData.getValue());
            return new SimpleIntegerProperty(index + 1).asObject();
        });
        colFullName.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFullName()));

        colDepartment.setCellValueFactory(cellData ->
                new SimpleStringProperty(departmentMap.get(positionMap.get(cellData.getValue().getPositionId()).getDepartmentId()).getDepartmentName()));

        colPosition.setCellValueFactory(cellData ->
                new SimpleStringProperty(positionMap.get(cellData.getValue().getPositionId()).getPositionName()));

        colBaseSalary.setCellValueFactory(cellData -> {
            if (isHaveStatus6(cellData.getValue().getEmployeeId())) {
                return new SimpleObjectProperty<>(BigDecimal.ZERO);
            }
            return new SimpleObjectProperty<>(cellData.getValue().getBasicSalary());
        });
        colBaseSalary.setCellFactory(column ->createCurrencyCell());

        colActualWorkDays.setCellValueFactory(cellData ->{
            if (isHaveStatus6(cellData.getValue().getEmployeeId())) {
                return new SimpleObjectProperty<>(0);
            }
            return new SimpleObjectProperty<>(countActualWorkDays(cellData.getValue().getEmployeeId()));
        });

        colLateArrival.setCellValueFactory(cellData -> {
            if (isHaveStatus6(cellData.getValue().getEmployeeId())) {
                return new SimpleObjectProperty<>(0);
            }
            return new SimpleObjectProperty<>(countStatus(2, cellData.getValue().getEmployeeId()));
        });

        colUnauthorizedLeave.setCellValueFactory(cellData -> {
            if (isHaveStatus6(cellData.getValue().getEmployeeId())) {
                return new SimpleObjectProperty<>(0);
            }
            return new SimpleObjectProperty<>(countStatus(3, cellData.getValue().getEmployeeId()));
        });

        colPositionAllowance.setCellValueFactory(cellData -> {
            if (isHaveStatus6(cellData.getValue().getEmployeeId())) {
                return new SimpleObjectProperty<>(BigDecimal.ZERO);
            }
            return new SimpleObjectProperty<>(positionMap.get(cellData.getValue().getPositionId()).getAllowance());
        });
        colPositionAllowance.setCellFactory(column -> createCurrencyCell());

        colMealAllowance.setCellValueFactory(cellData -> {
            if (isHaveStatus6(cellData.getValue().getEmployeeId())) {
                return new SimpleObjectProperty<>(BigDecimal.ZERO);
            }
            int employeeId = cellData.getValue().getEmployeeId();
            if (modifiedValues.containsKey(employeeId) &&
                    modifiedValues.get(employeeId).containsKey("mealAllowance")) {
                return new SimpleObjectProperty<>(modifiedValues.get(employeeId).get("mealAllowance"));
            }
            return new SimpleObjectProperty<>(allowanceMap.get("Ăn trưa").getAmount());
        });
        colMealAllowance.setCellFactory(column -> createTextFieldCurrencyCell());

        colGasAllowance.setCellValueFactory(cellData -> {
            if (isHaveStatus6(cellData.getValue().getEmployeeId())) {
                return new SimpleObjectProperty<>(BigDecimal.ZERO);
            }
            int employeeId = cellData.getValue().getEmployeeId();
            if (modifiedValues.containsKey(employeeId) &&
                    modifiedValues.get(employeeId).containsKey("gasAllowance")) {
                return new SimpleObjectProperty<>(modifiedValues.get(employeeId).get("gasAllowance"));
            }
            return new SimpleObjectProperty<>(allowanceMap.get("Xăng xe").getAmount());
        });
        colGasAllowance.setCellFactory(column -> createTextFieldCurrencyCell());

        colPhoneAllowance.setCellValueFactory(cellData -> {
            if (isHaveStatus6(cellData.getValue().getEmployeeId())) {
                return new SimpleObjectProperty<>(BigDecimal.ZERO);
            }
            int employeeId = cellData.getValue().getEmployeeId();
            if (modifiedValues.containsKey(employeeId) &&
                    modifiedValues.get(employeeId).containsKey("phoneAllowance")) {
                return new SimpleObjectProperty<>(modifiedValues.get(employeeId).get("phoneAllowance"));
            }
            return new SimpleObjectProperty<>(allowanceMap.get("Điện thoại").getAmount());
        });

        colPhoneAllowance.setCellFactory(column -> createTextFieldCurrencyCell());
        colOtherAllowance.setCellValueFactory(cellData -> {
            if (isHaveStatus6(cellData.getValue().getEmployeeId())) {
                return new SimpleObjectProperty<>(calculateMaternitySalary(cellData.getValue().getEmployeeId()));
            }
            return new SimpleObjectProperty<>(BigDecimal.ZERO);
        });

        colResponsibilityAllowance.setCellValueFactory(cellData -> {
            if (isHaveStatus6(cellData.getValue().getEmployeeId())) {
                return new SimpleObjectProperty<>(BigDecimal.ZERO);
            }
            int employeeId = cellData.getValue().getEmployeeId();
            if (modifiedValues.containsKey(employeeId) &&
                    modifiedValues.get(employeeId).containsKey("responsibilityAllowance")) {
                return new SimpleObjectProperty<>(modifiedValues.get(employeeId).get("responsibilityAllowance"));
            }
            return new SimpleObjectProperty<>(allowanceMap.get("Trách nhiệm").getAmount());
        });
        colResponsibilityAllowance.setCellFactory(column -> createTextFieldCurrencyCell());

        colOtherAllowance.setCellFactory(column -> createCurrencyCell());
        colTotalAllowance.setCellValueFactory(cellData -> {
            BigDecimal total = BigDecimal.ZERO;

            // Get the employee's row index
            int rowIndex = tblSalary.getItems().indexOf(cellData.getValue());

            // Get values from previously calculated columns
            BigDecimal positionAllowance = colPositionAllowance.getCellData(rowIndex);
            BigDecimal mealAllowance = colMealAllowance.getCellData(rowIndex);
            BigDecimal gasAllowance = colGasAllowance.getCellData(rowIndex);
            BigDecimal phoneAllowance = colPhoneAllowance.getCellData(rowIndex);
            BigDecimal responsibilityAllowance = colResponsibilityAllowance.getCellData(rowIndex);
            BigDecimal otherAllowance = colOtherAllowance.getCellData(rowIndex);

            // Sum all allowances
            total = total
                .add(positionAllowance != null ? positionAllowance : BigDecimal.ZERO)
                .add(mealAllowance != null ? mealAllowance : BigDecimal.ZERO)
                .add(gasAllowance != null ? gasAllowance : BigDecimal.ZERO)
                .add(phoneAllowance != null ? phoneAllowance : BigDecimal.ZERO)
                .add(responsibilityAllowance != null ? responsibilityAllowance : BigDecimal.ZERO)
                .add(otherAllowance != null ? otherAllowance : BigDecimal.ZERO);

            return new SimpleObjectProperty<>(total);
        });
        colTotalAllowance.setCellFactory(column -> createCurrencyCell());

        colBonus.setCellValueFactory(cellData -> {
            if (isHaveStatus6(cellData.getValue().getEmployeeId())) {
                return new SimpleObjectProperty<>(BigDecimal.ZERO);
            }
            return new SimpleObjectProperty<>(calculateBonus(cellData.getValue().getEmployeeId()));
        });
        colBonus.setCellFactory(column -> createCurrencyCell());
        colTotalIncome.setCellValueFactory(cellData -> {
            BigDecimal total = BigDecimal.ZERO;

            // Get the employee's row index
            int rowIndex = tblSalary.getItems().indexOf(cellData.getValue());

            // Get values from previously calculated columns
            BigDecimal baseSalary = colBaseSalary.getCellData(rowIndex);
            BigDecimal totalAllowance = colTotalAllowance.getCellData(rowIndex);
            BigDecimal bonus = colBonus.getCellData(rowIndex);

            // Sum all income
            total = total
                .add(baseSalary != null ? baseSalary : BigDecimal.ZERO)
                .add(totalAllowance != null ? totalAllowance : BigDecimal.ZERO)
                .add(bonus != null ? bonus : BigDecimal.ZERO);

            return new SimpleObjectProperty<>(total);
        });
        colTotalIncome.setCellFactory(column -> createCurrencyCell());

        colInsuranceSalary.setCellValueFactory(cellData -> {
            BigDecimal total = BigDecimal.ZERO;
            // Get the employee's row index
            int rowIndex = tblSalary.getItems().indexOf(cellData.getValue());
            // Get values from previously calculated columns
            BigDecimal baseSalary = colBaseSalary.getCellData(rowIndex);
            BigDecimal PositionAllowance = colPositionAllowance.getCellData(rowIndex);
            BigDecimal ResponsibilityAllowance = colResponsibilityAllowance.getCellData(rowIndex);
            // Sum all income
            total = total
                .add(baseSalary != null ? baseSalary : BigDecimal.ZERO)
                .add(PositionAllowance != null ? PositionAllowance : BigDecimal.ZERO)
                .add(ResponsibilityAllowance != null ? ResponsibilityAllowance : BigDecimal.ZERO);
            return new SimpleObjectProperty<>(total);
        });
        colInsuranceSalary.setCellFactory(column -> createCurrencyCell());
        colSocialInsurance.setCellValueFactory(cellData -> {
            if (isHaveStatus6(cellData.getValue().getEmployeeId())) {
                return new SimpleObjectProperty<>(BigDecimal.ZERO);
            }

            BigDecimal insuranceSalary = colInsuranceSalary.getCellData(tblSalary.getItems().indexOf(cellData.getValue()));
            double percentage = insurances.get("BHXH").getPercentage();

            // Convert percentage to decimal for calculation (8% → 0.08)
            BigDecimal insuranceAmount = insuranceSalary.multiply(BigDecimal.valueOf(percentage / 100.0));

            return new SimpleObjectProperty<>(insuranceAmount);
        });
        colSocialInsurance.setCellFactory(column -> createCurrencyCell());
        colHealthInsurance.setCellValueFactory(cellData -> {
            if (isHaveStatus6(cellData.getValue().getEmployeeId())) {
                return new SimpleObjectProperty<>(BigDecimal.ZERO);
            }

            BigDecimal insuranceSalary = colInsuranceSalary.getCellData(tblSalary.getItems().indexOf(cellData.getValue()));
            double percentage = insurances.get("BHYT").getPercentage();

            // Convert percentage to decimal for calculation (1.5% → 0.015)
            BigDecimal insuranceAmount = insuranceSalary.multiply(BigDecimal.valueOf(percentage / 100.0));

            return new SimpleObjectProperty<>(insuranceAmount);
        });
        colHealthInsurance.setCellFactory(column -> createCurrencyCell());
        colUnemploymentInsurance.setCellValueFactory(cellData -> {
            if (isHaveStatus6(cellData.getValue().getEmployeeId())) {
                return new SimpleObjectProperty<>(BigDecimal.ZERO);
            }

            BigDecimal insuranceSalary = colInsuranceSalary.getCellData(tblSalary.getItems().indexOf(cellData.getValue()));
            double percentage = insurances.get("BHTN").getPercentage();

            // Convert percentage to decimal for calculation (1% → 0.01)
            BigDecimal insuranceAmount = insuranceSalary.multiply(BigDecimal.valueOf(percentage / 100.0));

            return new SimpleObjectProperty<>(insuranceAmount);
        });
        colUnemploymentInsurance.setCellFactory(column -> createCurrencyCell());
        colTotalInsurance.setCellValueFactory(cellData -> {
            if (isHaveStatus6(cellData.getValue().getEmployeeId())) {
                return new SimpleObjectProperty<>(BigDecimal.ZERO);
            }
            BigDecimal total = BigDecimal.ZERO;

            // Get the employee's row index
            int rowIndex = tblSalary.getItems().indexOf(cellData.getValue());

            // Get values from previously calculated columns
            BigDecimal socialInsurance = colSocialInsurance.getCellData(rowIndex);
            BigDecimal healthInsurance = colHealthInsurance.getCellData(rowIndex);
            BigDecimal unemploymentInsurance = colUnemploymentInsurance.getCellData(rowIndex);

            // Sum all insurance
            total = total
                .add(socialInsurance != null ? socialInsurance : BigDecimal.ZERO)
                .add(healthInsurance != null ? healthInsurance : BigDecimal.ZERO)
                .add(unemploymentInsurance != null ? unemploymentInsurance : BigDecimal.ZERO);

            return new SimpleObjectProperty<>(total);
        });
        colTotalInsurance.setCellFactory(column -> createCurrencyCell());
        colIncomeTax.setCellValueFactory(cellData -> {
            // Nếu nhân viên đang nghỉ thai sản (status 6), không tính thuế thu nhập
            if (isHaveStatus6(cellData.getValue().getEmployeeId())) {
                return new SimpleObjectProperty<>(BigDecimal.ZERO);
            }

            // Lấy vị trí hàng của nhân viên này trong bảng
            int rowIndex = tblSalary.getItems().indexOf(cellData.getValue());

            // Lấy giá trị thu nhập tổng và bảo hiểm đã được tính từ các cột trước
            BigDecimal totalIncome = colTotalIncome.getCellData(rowIndex);
            BigDecimal totalInsurance = colTotalInsurance.getCellData(rowIndex);

            // Lấy thông tin về số người phụ thuộc để tính giảm trừ gia cảnh
            EmployeeDTO employee = cellData.getValue();
            int dependentCount = employee.getDependentCount() != null ? employee.getDependentCount() : 0;

            // Tính các khoản giảm trừ theo luật thuế
            BigDecimal personalDeduction = BigDecimal.valueOf(11000000); // Giảm trừ cá nhân: 11 triệu VNĐ/tháng
            BigDecimal dependentDeduction = BigDecimal.valueOf(4400000).multiply(BigDecimal.valueOf(dependentCount)); // 4.4 triệu VNĐ/người phụ thuộc/tháng
            BigDecimal insuranceDeduction = totalInsurance != null ? totalInsurance : BigDecimal.ZERO; // Giảm trừ bảo hiểm đã đóng

            // Tổng các khoản giảm trừ
            BigDecimal totalDeductions = personalDeduction.add(dependentDeduction).add(insuranceDeduction);

            // Tính thu nhập chịu thuế = thu nhập tổng - tổng giảm trừ
            BigDecimal taxableIncome = totalIncome.subtract(totalDeductions);

            // Đảm bảo thu nhập chịu thuế không âm
            if (taxableIncome.compareTo(BigDecimal.ZERO) < 0) {
                taxableIncome = BigDecimal.ZERO;
            }

            // Tính thuế dựa trên các bậc thuế suất từ cấu hình
            BigDecimal tax = BigDecimal.ZERO;
            for (TaxRateDTO taxRate : taxRates) {
                // Xác định xem thu nhập thuộc bậc thuế nào
                if (taxableIncome.compareTo(BigDecimal.valueOf(taxRate.getMin())) >= 0 &&
                        taxableIncome.compareTo(BigDecimal.valueOf(taxRate.getMax())) <= 0) {
                    // Áp dụng công thức: Thuế = Thu nhập chịu thuế x Thuế suất - Số tiền giảm trừ cố định
                    tax = taxableIncome.multiply(BigDecimal.valueOf(taxRate.getTaxRate() / 100.0))
                            .subtract(BigDecimal.valueOf(taxRate.getFixedDeduction()));
                    break;
                }
            }

            // Đảm bảo thuế không âm
            if (tax.compareTo(BigDecimal.ZERO) < 0) {
                tax = BigDecimal.ZERO;
            }

            // Làm tròn số thuế đến 500 VNĐ (thông lệ tại Việt Nam)
            long amountInLong = tax.longValue();
            long remainder = amountInLong % 500;
            if (remainder < 250) {
                amountInLong = amountInLong - remainder; // Làm tròn xuống
            } else {
                amountInLong = amountInLong + (500 - remainder); // Làm tròn lên
            }

            // Trả về số thuế cuối cùng
            return new SimpleObjectProperty<>(BigDecimal.valueOf(amountInLong));
        });
        colIncomeTax.setCellFactory(column -> createCurrencyCell());
        colPenalty.setCellValueFactory(cellData -> {
            if (isHaveStatus6(cellData.getValue().getEmployeeId())) {
                return new SimpleObjectProperty<>(BigDecimal.ZERO);
            }
            BigDecimal total = BigDecimal.ZERO;

            // Get the employee's row index
            int rowIndex = tblSalary.getItems().indexOf(cellData.getValue());

            // Get values from previously calculated columns
            int lateArrivalCount = colLateArrival.getCellData(rowIndex);
            int unauthorizedLeaveCount = colUnauthorizedLeave.getCellData(rowIndex);

            // Calculate penalties
            total = total
                    .add(BigDecimal.valueOf(lateArrivalCount).multiply(BigDecimal.valueOf(currentConfig.getLateArrivalPenalty())))
                    .add(BigDecimal.valueOf(unauthorizedLeaveCount).multiply(BigDecimal.valueOf(currentConfig.getUnauthorizedLeavePenalty())));

            return new SimpleObjectProperty<>(total);
        });
        colPenalty.setCellFactory(column -> createCurrencyCell());
        colTotalDeduction.setCellValueFactory(cellData -> {
            if (isHaveStatus6(cellData.getValue().getEmployeeId())) {
                return new SimpleObjectProperty<>(BigDecimal.ZERO);
            }
            BigDecimal total = BigDecimal.ZERO;

            // Get the employee's row index
            int rowIndex = tblSalary.getItems().indexOf(cellData.getValue());

            // Get values from previously calculated columns
            BigDecimal incomeTax = colIncomeTax.getCellData(rowIndex);
            BigDecimal totalInsurance = colTotalInsurance.getCellData(rowIndex);
            BigDecimal penalty = colPenalty.getCellData(rowIndex);

            // Sum all deductions
            total = total
                    .add(incomeTax != null ? incomeTax : BigDecimal.ZERO)
                    .add(totalInsurance != null ? totalInsurance : BigDecimal.ZERO)
                    .add(penalty != null ? penalty : BigDecimal.ZERO);

            return new SimpleObjectProperty<>(total);
        });
        colTotalDeduction.setCellFactory(column -> createCurrencyCell());
        colNetSalary.setCellValueFactory(cellData -> {
            BigDecimal total = BigDecimal.ZERO;

            // Get the employee's row index
            int rowIndex = tblSalary.getItems().indexOf(cellData.getValue());

            // Get values from previously calculated columns
            BigDecimal totalIncome = colTotalIncome.getCellData(rowIndex);
            BigDecimal totalDeduction = colTotalDeduction.getCellData(rowIndex);

            // Calculate net salary
            total = totalIncome.subtract(totalDeduction != null ? totalDeduction : BigDecimal.ZERO);

            return new SimpleObjectProperty<>(total);
        });
        colNetSalary.setCellFactory(column -> createCurrencyCell());
    }



    private BigDecimal calculateMaternitySalary(Integer employeeId) {
        // Calculate the maternity salary based on the employee's ID
        return busFactory.getPayrollBUS().getAvgNetIncome6Months(employeeId,globalStartDate.getMonthValue(), globalStartDate.getYear());
    }

    public BigDecimal calculateBonus(int employeeId) {
        BigDecimal bonus = BigDecimal.ZERO;
        for (BonusDTO bonusDTO : bonusList) {
            if (bonusDTO.getEmployee_id() == employeeId) {
                bonus = bonus.add(bonusDTO.getAmount());
            }
        }
        return bonus;
    }

    public void loadTable() {
        // Load the data into the table view
        tblSalary.getItems().clear();
        tblSalary.getItems().addAll(employeeList);
    }

    public int countStatus(int status, int employeeId) {
        int count = 0;
        for (AttendanceDTO attendance : attendanceList) {
            if (attendance.getStatus() == status && attendance.getEmployee_id() == employeeId) {
                count++;
            }
        }
        return count;
    }
    public int countActualWorkDays(int employeeId) {
        int count = 0;
        for (AttendanceDTO attendance : attendanceList) {
            if ((attendance.getStatus() == 1 || attendance.getStatus() == 2 ||
                    attendance.getStatus() == 4 ||attendance.getStatus() == 5 ||
                    attendance.getStatus() == 6 )&& attendance.getEmployee_id() == employeeId) {
                count++;
            }
        }
        return count;
    }

    public boolean isHaveStatus6(int employeeId) {
        for (AttendanceDTO attendance : attendanceList) {
            if (attendance.getStatus() == 6 && attendance.getEmployee_id() == employeeId) {
                return true;
            }
        }
        return false;
    }

        private Map<Integer, Map<String, BigDecimal>> modifiedValues = new HashMap<>();

    private TableCell<EmployeeDTO, BigDecimal> createTextFieldCurrencyCell() {
        return new TableCell<>() {
            private final TextField textField = new CurrencyTextField();

            {
                // Cấu hình TextField
                textField.setOnAction(e -> commitChanges());
                textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
                    if (!newVal) commitChanges();
                });

                // Chỉ hiển thị TextField khi được double-click
                setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && !isEmpty()) {
                        startEdit();
                    }
                });
            }

            private void commitChanges() {
                try {
                    String text = textField.getText().replaceAll("[,.]", "");
                    BigDecimal value = new BigDecimal(text);

                    // Lưu giá trị
                    EmployeeDTO employee = getTableView().getItems().get(getIndex());
                    int employeeId = employee.getEmployeeId();
                    modifiedValues.putIfAbsent(employeeId, new HashMap<>());
                    Map<String, BigDecimal> employeeValues = modifiedValues.get(employeeId);
                    // Lưu giá trị dựa vào cột
                    // ...
                    TableColumn<EmployeeDTO, BigDecimal> column = getTableColumn();
                    if (column == colMealAllowance) {
                        employeeValues.put("mealAllowance", value);
                    } else if (column == colGasAllowance) {
                        employeeValues.put("gasAllowance", value);
                    } else if (column == colPhoneAllowance) {
                        employeeValues.put("phoneAllowance", value);
                    } else if (column == colResponsibilityAllowance) {
                        employeeValues.put("responsibilityAllowance", value);
                    }

                    // Complete editing
                    commitEdit(value);
                    // Kết thúc chỉnh sửa và làm mới bảng
                    cancelEdit();
                    getTableView().refresh();
                } catch (NumberFormatException ex) {
                    // Xử lý lỗi
                }
            }

            @Override
            public void startEdit() {
                super.startEdit();
                setText(null);
                textField.setText(String.format("%,d", getItem().longValue()));
                setGraphic(textField);
                textField.requestFocus();
            }

            @Override
            public void cancelEdit() {
                super.cancelEdit();
                updateItem(getItem(), false);
            }

            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    if (isEditing()) {
                        setText(null);
                        setGraphic(textField);
                    } else {
                        // Khi không trong chế độ edit, chỉ hiển thị text
                        setText(String.format("%,d", item.longValue()));
                        setGraphic(null);
                    }
                }
            }
        };
    }
    private TableCell<EmployeeDTO, BigDecimal> createCurrencyCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    // Format with thousands separators, no currency symbol
                    setText(String.format("%,d", item.longValue()));
                }
            }
        };
    }
    private void loadConfig() {
        try {
            File file = new File(CONFIG_FILE);
            if (!file.exists()) {
                currentConfig = new SystemConfig();
                return;
            }

            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(file));

            Gson gson = new Gson();
            currentConfig = gson.fromJson(jsonObject.toString(), SystemConfig.class);
        } catch (Exception e) {
            log.error("Error loading config: ", e);
            NotificationUtil.showErrorNotification("Lỗi", "Không thể tải cài đặt hệ thống: " + e.getMessage());
            currentConfig = new SystemConfig();
        }
    }

    /**
     * Gets the salary period date range for a given month and year based on system configuration
     * @param month Month (1-12)
     * @param year Year (e.g. 2023)
     * @return A LocalDate array where index 0 is start date and index 1 is end date (inclusive)
     */
private LocalDate[] getSalaryPeriod(int month, int year) {
    // Ensure configuration is loaded
    if (currentConfig == null) {
        loadConfig();
    }

    int salaryStartDay = currentConfig.getSalaryStartDay();
    int salaryEndDay = currentConfig.getSalaryEndDay();

    // Calculate start date (day of specified month)
    LocalDate startDate = LocalDate.of(year, month, salaryStartDay);

    // Calculate end date (day of next month)
    // Determine next month and year
    int endMonth = month < 12 ? month + 1 : 1;
    int endYear = month < 12 ? year : year + 1;

    LocalDate endDate = LocalDate.of(endYear, endMonth, salaryEndDay);

    return new LocalDate[]{startDate, endDate};
}
    private void setupComboBoxes() {
        // Setup Month ComboBox (1-12)
        ObservableList<Integer> months = FXCollections.observableArrayList();
        int currentMonth = LocalDate.now().getMonthValue();
        int currentYear = LocalDate.now().getYear();

        // Populate available months based on data
        for (int i = 1; i <= 12; i++) {
            // Skip future months in current year
            if (i > currentMonth && cbYear.getValue() != null && cbYear.getValue() == currentYear) {
                continue;
            }

            // Check if there's attendance data for this month
            if (hasAttendanceData(i, currentYear)) {
                months.add(i);
            }
        }

        cbMonth.setItems(months);

        // Select current month if available, otherwise the last available month
        if (!months.isEmpty()) {
            if (months.contains(currentMonth)) {
                cbMonth.setValue(currentMonth);
            } else {
                cbMonth.setValue(months.get(months.size() - 1));
            }
        }

        // Setup Year ComboBox (only years with data, up to current year)
        ObservableList<Integer> years = FXCollections.observableArrayList();
        for (int i = currentYear - 3; i <= currentYear; i++) {
            if (hasAttendanceDataForYear(i)) {
                years.add(i);
            }
        }

        cbYear.setItems(years);
        if (!years.isEmpty() && years.contains(currentYear)) {
            cbYear.setValue(currentYear);
        } else if (!years.isEmpty()) {
            cbYear.setValue(years.get(years.size() - 1));
        }

        // Add listener to year combobox to update months when year changes
        cbYear.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateAvailableMonths(newVal);
            }
        });

        // Update the save button state
        updateSaveButtonState();
    }

    private void updateAvailableMonths(int year) {
        int currentMonth = LocalDate.now().getMonthValue();
        int currentYear = LocalDate.now().getYear();

        ObservableList<Integer> availableMonths = FXCollections.observableArrayList();

        for (int i = 1; i <= 12; i++) {
            // Skip future months in current year
            if (year == currentYear && i > currentMonth) {
                continue;
            }

            // Check if there's attendance data for this month and year
            if (hasAttendanceData(i, year)) {
                availableMonths.add(i);
            }
        }

        // Update months combobox
        cbMonth.setItems(availableMonths);

        // Select first available month
        if (!availableMonths.isEmpty()) {
            cbMonth.setValue(availableMonths.get(0));
        }

        // Update save button state
        updateSaveButtonState();
    }

    private boolean hasAttendanceData(int month, int year) {
        // Check if there's attendance data for this month and year
        try {
            LocalDate startDate = LocalDate.of(year, month, 1);
            LocalDate endDate = startDate.plusMonths(1).minusDays(1);

            // Get a count of attendance records for this period
            List<AttendanceDTO> attendances = busFactory.getAttendanceBUS().getAllAttendanceBetweenDates(startDate, endDate);
            return attendances != null && !attendances.isEmpty();
        } catch (Exception e) {
            log.error("Error checking attendance data for {}/{}: {}", month, year, e.getMessage());
            return false;
        }
    }

    private boolean hasAttendanceDataForYear(int year) {
        // Check if there's any attendance data for any month in this year
        for (int month = 1; month <= 12; month++) {
            if (hasAttendanceData(month, year)) {
                return true;
            }
        }
        return false;
    }

    private void updateSaveButtonState() {
        Integer selectedMonth = cbMonth.getValue();
        Integer selectedYear = cbYear.getValue();

        if (selectedMonth == null || selectedYear == null) {
            btnSave.setDisable(true);
            return;
        }

//        btnSave.setDisable(false);

        LocalDate[] salaryPeriod = getSalaryPeriod(selectedMonth, selectedYear);
        LocalDate endDate = salaryPeriod[1];

        // Disable save button if salary period hasn't ended yet
        boolean periodHasEnded = LocalDate.now().isAfter(endDate);

        // Check if payroll already exists for this period
        boolean payrollExists = busFactory.getPayrollBUS().getPayrollByMonthAndYear(selectedMonth, selectedYear);

        // Enable button only if period has ended and no payroll exists yet (or editing is allowed)
        btnSave.setDisable(!periodHasEnded || payrollExists);

        // Update button text based on whether payroll exists
        btnSave.setText(payrollExists ? "Cập nhật bảng lương" : "Tạo bảng lương");

        // Show tooltip explaining why button is disabled
        if (!periodHasEnded) {
            btnSave.setTooltip(new Tooltip("Không thể tạo bảng lương khi chưa hết chu kỳ lương"));
        } else if (payrollExists) {
            btnSave.setTooltip(new Tooltip("Bảng lương đã tồn tại cho kỳ lương này"));
        } else {
            btnSave.setTooltip(null);
        }
    }

    private void setupFilterListeners() {
        btnFilter.setOnAction(event -> filterPayrollData());
    }

    @FXML
    private void filterPayrollData() {
        try {
            Integer selectedMonth = (Integer) cbMonth.getValue();
            Integer selectedYear = (Integer) cbYear.getValue();

            if (selectedMonth == null || selectedYear == null) {
                NotificationUtil.showErrorNotification("Lỗi", "Vui lòng chọn tháng và năm");
                return;
            }

            log.info("Filtering payroll data for month {} and year {}", selectedMonth, selectedYear);

            // Get date range for selected period
            LocalDate[] salaryPeriod = getSalaryPeriod(selectedMonth, selectedYear);
            globalStartDate = salaryPeriod[0];
            globalEndDate = salaryPeriod[1];

            log.info("Salary period: {} to {}", globalStartDate, globalEndDate);

            // Check if payroll data exists for this period
            List<PayrollDTO> existingPayrolls = null;
//                    busFactory.getPayrollBUS().findByPeriod(selectedMonth, selectedYear);

            if (existingPayrolls != null && !existingPayrolls.isEmpty()) {
                log.info("Found existing payroll data for period {}/{}, loading from database",
                        selectedMonth, selectedYear);
                loadExistingPayrollData(existingPayrolls);
            } else {
                log.info("No existing payroll data for period {}/{}, calculating new payroll",
                        selectedMonth, selectedYear);
                loadNewPayrollData(selectedMonth, selectedYear);
            }
            updateSaveButtonState();
            // Update the table
            tblSalary.refresh();

        } catch (Exception e) {
            log.error("Error filtering payroll data", e);
            NotificationUtil.showErrorNotification("Lỗi", "Không thể lọc dữ liệu: " + e.getMessage());
        }
    }

    private void loadExistingPayrollData(List<PayrollDTO> payrolls) {
        // Create a map to quickly lookup payrolls by employee ID
        Map<Integer, PayrollDTO> payrollMap = new HashMap<>();
        for (PayrollDTO payroll : payrolls) {
            payrollMap.put(payroll.getEmployeeId(), payroll);
        }

        // Clear modified values
        modifiedValues.clear();

        // Load attendance data for this period
        attendanceList = new ArrayList<>();
                busFactory.getAttendanceBUS().getAllAttendanceBetweenDates(globalStartDate, globalEndDate);

        // Load bonus data for this period
        bonusList = busFactory.getBonusBUS().findByDateRange(globalStartDate, globalEndDate);

        // Update employee data with payroll values
        for (EmployeeDTO employee : employeeList) {
            PayrollDTO payroll = payrollMap.get(employee.getEmployeeId());
            if (payroll != null) {
                // Store payroll values in the modifiedValues map for each column that can be modified
                Map<String, BigDecimal> employeeModifiedValues = new HashMap<>();

                // Add all relevant values from the payroll
                employeeModifiedValues.put("mealAllowance", payroll.getOther_allowance());
                employeeModifiedValues.put("gasAllowance", payroll.getOther_allowance());
                employeeModifiedValues.put("phoneAllowance", payroll.getOther_allowance());
                employeeModifiedValues.put("responsibilityAllowance", payroll.getOther_allowance());
                // Add any other columns that can be modified

                // Store in the global modified values map
                modifiedValues.put(employee.getEmployeeId(), employeeModifiedValues);

                // Set dependent count
                employee.setDependentCount(busFactory.getDependentsBUS().countDependentByEmployeeId(employee.getEmployeeId()));
            }
        }

        // Refresh the table
        loadTable();
    }

    private void loadNewPayrollData(int month, int year) {
        // Clear any existing modified values
        modifiedValues.clear();

        // Load all necessary data for the new period
        loadData();

        // Filter attendance data for the specific period
        attendanceList = busFactory.getAttendanceBUS().getAllAttendanceBetweenDates(
                globalStartDate, globalEndDate);

        // Load bonus data for this period
        bonusList = busFactory.getBonusBUS().findByDateRange(globalStartDate, globalEndDate);

        // Update dependent counts
        for (EmployeeDTO employee : employeeList) {
            employee.setDependentCount(
                    busFactory.getDependentsBUS().countDependentByEmployeeId(employee.getEmployeeId()));
        }

        // Refresh the table
        loadTable();
    }

    @FXML
    private void savePayrollData() {
        try {
            List<PayrollDTO> payrollList = new ArrayList<>();
            int currentUserId = busFactory.getUserSession().getEmployee().getEmployeeId(); // Assuming you have a session manager
            LocalDate payrollDate = LocalDate.now(); // Or use a specific date for the payroll

            // Loop through all employees in the table
            for (EmployeeDTO employee : tblSalary.getItems()) {
                int rowIndex = tblSalary.getItems().indexOf(employee);

                // Extract all values from table columns
                BigDecimal baseSalary = colBaseSalary.getCellData(rowIndex);
                Integer actualWorkingDays = colActualWorkDays.getCellData(rowIndex);
                Integer lateDays = colLateArrival.getCellData(rowIndex);
                Integer leaveDays = colUnauthorizedLeave.getCellData(rowIndex);
                BigDecimal positionAllowance = colPositionAllowance.getCellData(rowIndex);
                BigDecimal otherAllowance = colOtherAllowance.getCellData(rowIndex);
                BigDecimal totalAllowance = colTotalAllowance.getCellData(rowIndex);
                BigDecimal bonusTotal = colBonus.getCellData(rowIndex);
                BigDecimal totalIncome = colTotalIncome.getCellData(rowIndex);
                BigDecimal socialInsuranceSalary = colInsuranceSalary.getCellData(rowIndex);
                BigDecimal socialInsurance = colSocialInsurance.getCellData(rowIndex);
                BigDecimal healthInsurance = colHealthInsurance.getCellData(rowIndex);
                BigDecimal unemploymentInsurance = colUnemploymentInsurance.getCellData(rowIndex);
                BigDecimal totalInsurance = colTotalInsurance.getCellData(rowIndex);
                BigDecimal incomeTax = colIncomeTax.getCellData(rowIndex);
                BigDecimal penaltyAmount = colPenalty.getCellData(rowIndex);
                BigDecimal totalDeduction = colTotalDeduction.getCellData(rowIndex);
                BigDecimal netIncome = colNetSalary.getCellData(rowIndex);
                BigDecimal mealAllowance = colMealAllowance.getCellData(rowIndex);
                BigDecimal gasAllowance = colGasAllowance.getCellData(rowIndex);
                BigDecimal phoneAllowance = colPhoneAllowance.getCellData(rowIndex);
                BigDecimal responsibilityAllowance = colResponsibilityAllowance.getCellData(rowIndex);



                // Get the month and year from the selected period
                Integer month = cbMonth.getValue();
                Integer year = cbYear.getValue();

                // Create Payroll object
                PayrollDTO payroll = PayrollDTO.builder()
                    .employeeId(employee.getEmployeeId())
                    .payrollDate(LocalDate.of(year, month, 1)) // Set to first day of month
                    .actual_working_days(actualWorkingDays)
                    .leave_days(leaveDays)
                    .late_days(lateDays)
                    .position_allowance(positionAllowance)
                    .other_allowance(otherAllowance)
                    .total_allowance(totalAllowance)
                    .bonus_total(bonusTotal)
                    .taxable_income(totalIncome.subtract(totalDeduction))
                    .social_insurance_salary(socialInsuranceSalary)
                    .insurance_social(socialInsurance)
                    .insurance_health(healthInsurance)
                    .insurance_accident(unemploymentInsurance)
                    .total_insurance(totalInsurance)
                    .income_tax(incomeTax)
                    .deductible_income(totalDeduction)
                    .penalty_amount(penaltyAmount)
                    .net_income(netIncome)
                    .created_by(currentUserId)
                    .meal_allowance(mealAllowance)
                    .gas_allowance(gasAllowance)
                    .phone_allowance(phoneAllowance)
                    .responsibility_allowance(responsibilityAllowance)
                        .base_salary(baseSalary)
                    .build();

                payrollList.add(payroll);
            }

            // Save to database using BUS layer
            boolean success = busFactory.getPayrollBUS().insertPayroll(payrollList);

            if (success) {
                NotificationUtil.showSuccessNotification("Thành công",
                        "Đã lưu dữ liệu lương thành công cho " + payrollList.size() + " nhân viên.");
            } else {
                NotificationUtil.showErrorNotification("Lỗi",
                        "Không thể lưu dữ liệu lương. Vui lòng kiểm tra lại.");
            }
            GenericTablePrinter.printTable(payrollList);
        } catch (Exception e) {
            log.error("Error saving payroll data: ", e);
            NotificationUtil.showErrorNotification("Lỗi",
                    "Không thể lưu dữ liệu lương: " + e.getMessage());
        }
    }

}

