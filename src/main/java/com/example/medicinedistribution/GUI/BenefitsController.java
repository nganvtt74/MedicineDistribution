package com.example.medicinedistribution.GUI;

import com.example.medicinedistribution.BUS.BUSFactory;
import com.example.medicinedistribution.BUS.Interface.AllowanceBUS;
import com.example.medicinedistribution.BUS.Interface.BonusBUS;
import com.example.medicinedistribution.BUS.Interface.BonusTypeBUS;
import com.example.medicinedistribution.DTO.AllowanceDTO;
import com.example.medicinedistribution.DTO.BonusDTO;
import com.example.medicinedistribution.DTO.BonusTypeDTO;
import com.example.medicinedistribution.Exception.DeleteFailedException;
import com.example.medicinedistribution.Exception.PermissionDeniedException;
import com.example.medicinedistribution.GUI.SubAction.AllowanceAction;
import com.example.medicinedistribution.GUI.SubAction.BonusAction;
import com.example.medicinedistribution.Util.NotificationUtil;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class BenefitsController {
    // AllowanceDTO controls
    @FXML private TextField txtSearchAllowance;
    @FXML private Button btnRefreshAllowance;
    @FXML private Button btnAddAllowance;
    @FXML private Button btnEditAllowance;
    @FXML private Button btnDeleteAllowance;
    @FXML private TableView<AllowanceDTO> tblAllowance;

    // BonusDTO controls
    @FXML private TextField txtSearchBonus;
    @FXML private Button btnRefreshBonus;
    @FXML private Button btnAddBonus;
    @FXML private Button btnEditBonus;
    @FXML private Button btnDeleteBonus;
    @FXML private TableView<BonusDTO> tblBonus;
    @FXML private ComboBox<Integer> cboYearFilter;
    @FXML private ComboBox<Integer> cboMonthFilter;

    // Other components
    private BUSFactory busFactory;
    private AllowanceBUS allowanceBUS;
    private BonusBUS bonusBUS;
    private BonusTypeBUS bonusTypeBUS;

    private List<AllowanceDTO> allowanceList;
    private List<BonusDTO> bonusList;
    private List<BonusTypeDTO> bonusTypeList;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public BenefitsController(BUSFactory busFactory) {
        this.busFactory = busFactory;
    }

    public void initialize() {
        // Initialize BUS components
        allowanceBUS = busFactory.getAllowanceBUS();
        bonusBUS = busFactory.getBonusBUS();
        bonusTypeBUS = busFactory.getBonusTypeBUS();

        // Setup data lists
        setupData();

        // Setup UI components
        setupAllowanceTable();
        setupBonusTable();
        setupAllowanceUI();
        setupBonusUI();

        // Load data
        loadAllowanceData();
        loadBonusData();
    }

    private void setupData() {
        allowanceList = new ArrayList<>(allowanceBUS.findAll());
        bonusList = new ArrayList<>(bonusBUS.findAll());
        bonusTypeList = new ArrayList<>(bonusTypeBUS.findAll());
    }

    private void setupAllowanceTable() {
        // Create columns for allowance table
        TableColumn<AllowanceDTO, Integer> colAllowanceId = new TableColumn<>("ID");
        colAllowanceId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colAllowanceId.setPrefWidth(70);

        TableColumn<AllowanceDTO, String> colAllowanceName = new TableColumn<>("Tên phụ cấp");
        colAllowanceName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colAllowanceName.setPrefWidth(200);

        TableColumn<AllowanceDTO, BigDecimal> colAmount = new TableColumn<>("Số tiền");
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colAmount.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getAmount()));
        colAmount.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("%,.0f VNĐ", amount));
                }
            }
        });
        colAmount.setPrefWidth(150);

        TableColumn<AllowanceDTO, Boolean> colInsuranceIncluded = new TableColumn<>("Đóng BH");
        colInsuranceIncluded.setCellValueFactory(cellData ->
                new SimpleBooleanProperty(cellData.getValue().getIs_insurance_included()));
        colInsuranceIncluded.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(value ? "Có" : "Không");
                }
            }
        });
        colInsuranceIncluded.setPrefWidth(100);

        // Add columns to table
        tblAllowance.getColumns().addAll(colAllowanceId, colAllowanceName, colAmount, colInsuranceIncluded);
    }

    private void setupBonusTable() {
        // Create columns for bonus table
        TableColumn<BonusDTO, Integer> colBonusId = new TableColumn<>("ID");
        colBonusId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colBonusId.setPrefWidth(70);

        TableColumn<BonusDTO, Integer> colEmployeeId = new TableColumn<>("Mã NV");
        colEmployeeId.setCellValueFactory(new PropertyValueFactory<>("employee_id"));
        colEmployeeId.setPrefWidth(80);

        TableColumn<BonusDTO, String> colEmployeeName = new TableColumn<>("Tên nhân viên");
        colEmployeeName.setCellValueFactory(cellData -> {
            // This would need to be implemented to look up employee name from employee_id
            // For now, just return placeholder
            return new SimpleStringProperty("Nhân viên " + cellData.getValue().getEmployee_id());
        });
        colEmployeeName.setPrefWidth(200);

        TableColumn<BonusDTO, String> colBonusType = new TableColumn<>("Loại thưởng");
        colBonusType.setCellValueFactory(cellData -> {
            Integer typeId = cellData.getValue().getBonus_type_id();
            String typeName = bonusTypeList.stream()
                    .filter(type -> type.getId().equals(typeId))
                    .findFirst()
                    .map(BonusTypeDTO::getName)
                    .orElse("N/A");
            return new SimpleStringProperty(typeName);
        });
        colBonusType.setPrefWidth(150);

        TableColumn<BonusDTO, BigDecimal> colBonusAmount = new TableColumn<>("Số tiền");
        colBonusAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colBonusAmount.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("%,.0f VNĐ", amount));
                }
            }
        });
        colBonusAmount.setPrefWidth(150);

        TableColumn<BonusDTO, LocalDate> colDate = new TableColumn<>("Ngày thưởng");
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colDate.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(dateFormatter));
                }
            }
        });
        colDate.setPrefWidth(120);

        // Add columns to table
        tblBonus.getColumns().addAll(colBonusId, colEmployeeId, colEmployeeName, colBonusType, colBonusAmount, colDate);
    }

    private void setupAllowanceUI() {
        btnAddAllowance.setOnAction(event -> {
            AllowanceAction.showDialog(busFactory, this, AllowanceAction.ActionType.ADD);
        });

        btnEditAllowance.setOnAction(event -> {
            AllowanceDTO selected = tblAllowance.getSelectionModel().getSelectedItem();
            if (selected == null) {
                NotificationUtil.showErrorNotification("Lỗi", "Vui lòng chọn một phụ cấp để chỉnh sửa.");
                return;
            }
            AllowanceAction.showDialog(busFactory, this, AllowanceAction.ActionType.EDIT, selected);
        });

        btnDeleteAllowance.setOnAction(event -> {
            if (!NotificationUtil.showConfirmation("Xác nhận", "Bạn có chắc chắn muốn xóa phụ cấp này?")) {
                return;
            }

            AllowanceDTO selected = tblAllowance.getSelectionModel().getSelectedItem();
            if (selected == null) {
                NotificationUtil.showErrorNotification("Lỗi", "Vui lòng chọn một phụ cấp để xóa.");
                return;
            }

            try {
                if (allowanceBUS.delete(selected.getId())) {
                    NotificationUtil.showSuccessNotification("Thành công", "Xóa phụ cấp thành công.");
                    refreshData();
                }
            } catch (PermissionDeniedException e) {
                NotificationUtil.showErrorNotification("Quyền truy cập", e.getMessage());
            } catch (DeleteFailedException e) {
                NotificationUtil.showErrorNotification("Lỗi", e.getMessage());
            } catch (Exception e) {
                NotificationUtil.showErrorNotification("Lỗi", "Có lỗi không xác định: " + e.getMessage());
            }
        });

        txtSearchAllowance.textProperty().addListener((observable, oldValue, newValue) -> {
            filterAllowances();
        });
        btnRefreshAllowance.setOnAction(event -> {
            refreshAllowanceData();
        });
    }

    private void setupBonusUI() {
        btnAddBonus.setOnAction(event -> {
            BonusAction.showDialog(busFactory, this, BonusAction.ActionType.ADD);
        });

        btnEditBonus.setOnAction(event -> {
            BonusDTO selected = tblBonus.getSelectionModel().getSelectedItem();
            if (selected == null) {
                NotificationUtil.showErrorNotification("Lỗi", "Vui lòng chọn một khoản thưởng để chỉnh sửa.");
                return;
            }
            BonusAction.showDialog(busFactory, this, BonusAction.ActionType.EDIT, selected);
        });

        btnDeleteBonus.setOnAction(event -> {
            if (!NotificationUtil.showConfirmation("Xác nhận", "Bạn có chắc chắn muốn xóa khoản thưởng này?")) {
                return;
            }

            BonusDTO selected = tblBonus.getSelectionModel().getSelectedItem();
            if (selected == null) {
                NotificationUtil.showErrorNotification("Lỗi", "Vui lòng chọn một khoản thưởng để xóa.");
                return;
            }

            try {
                if (bonusBUS.delete(selected.getId())) {
                    NotificationUtil.showSuccessNotification("Thành công", "Xóa khoản thưởng thành công.");
                    refreshData();
                }
            } catch (PermissionDeniedException e) {
                NotificationUtil.showErrorNotification("Quyền truy cập", e.getMessage());
            } catch (DeleteFailedException e) {
                NotificationUtil.showErrorNotification("Lỗi", e.getMessage());
            } catch (Exception e) {
                NotificationUtil.showErrorNotification("Lỗi", "Có lỗi không xác định: " + e.getMessage());
            }
        });
        txtSearchBonus.textProperty().addListener((observable, oldValue, newValue) -> {
            filterBonuses();
        });
        List<Integer> years = bonusList.stream()
                .map(bonus -> bonus.getDate().getYear())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        // Add current year if not already in the list
        int currentYear = LocalDate.now().getYear();
        if (!years.contains(currentYear)) {
            years.add(currentYear);
            Collections.sort(years);
        }
        cboYearFilter.getItems().add(null);
        cboYearFilter.getItems().addAll(FXCollections.observableArrayList(years));
        cboYearFilter.setPromptText("Năm");

        // Setup month filter
        cboMonthFilter.getItems().add(null);
        List<Integer> months = IntStream.rangeClosed(1, 12).boxed().collect(Collectors.toList());
        cboMonthFilter.getItems().addAll(FXCollections.observableArrayList(months));
        cboMonthFilter.setPromptText("Tháng");

        // Add listeners
        cboYearFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                filterBonusesByYearAndMonth(newValue, cboMonthFilter.getValue());
            } else {
                // If year is cleared, clear month selection too
                cboMonthFilter.setValue(null);
                loadBonusData(); // Reset to show all data
            }
        });

        cboMonthFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                // If year is not selected, default to current year
                Integer selectedYear = cboYearFilter.getValue();
                if (selectedYear == null) {
                    selectedYear = currentYear;
                    cboYearFilter.setValue(selectedYear);
                }
                filterBonusesByYearAndMonth(selectedYear, newValue);
            } else if (cboYearFilter.getValue() != null) {
                // If month is cleared but year is selected, filter by year only
                filterBonusesByYearAndMonth(cboYearFilter.getValue(), null);
            }
        });
        btnRefreshBonus.setOnAction(event -> {
            refreshBonusData();
        });

    }
    private void filterBonusesByYearAndMonth(Integer year, Integer month) {
        if (year == null) {
            loadBonusData();
            return;
        }

        List<BonusDTO> filteredList = new ArrayList<>();

        for (BonusDTO bonus : bonusList) {
            LocalDate bonusDate = bonus.getDate();
            if (bonusDate != null) {
                boolean matchesYear = bonusDate.getYear() == year;
                boolean matchesMonth = month == null || bonusDate.getMonthValue() == month;

                if (matchesYear && matchesMonth) {
                    filteredList.add(bonus);
                }
            }
        }

        tblBonus.setItems(FXCollections.observableArrayList(filteredList));
    }

    // Modify your existing filterBonuses method to respect year/month filters
    private void filterBonuses() {
        String searchText = txtSearchBonus.getText().toLowerCase();

        // First filter by year and month if selected
        List<BonusDTO> baseFilteredList = new ArrayList<>();
        Integer selectedYear = cboYearFilter.getValue();
        Integer selectedMonth = cboMonthFilter.getValue();

        for (BonusDTO bonus : bonusList) {
            LocalDate bonusDate = bonus.getDate();
            if (bonusDate != null) {
                boolean matchesYear = selectedYear == null || bonusDate.getYear() == selectedYear;
                boolean matchesMonth = selectedMonth == null || bonusDate.getMonthValue() == selectedMonth;

                if (matchesYear && matchesMonth) {
                    baseFilteredList.add(bonus);
                }
            } else if (selectedYear == null && selectedMonth == null) {
                // If no date and no filters, include the bonus
                baseFilteredList.add(bonus);
            }
        }

        // Then filter by search text
        List<BonusDTO> finalFilteredList = new ArrayList<>();

        for (BonusDTO bonus : baseFilteredList) {
            // Get bonus type name for searching
            String bonusTypeName = bonusTypeList.stream()
                    .filter(type -> type.getId().equals(bonus.getBonus_type_id()))
                    .findFirst()
                    .map(BonusTypeDTO::getName)
                    .orElse("");

            if (bonusTypeName.toLowerCase().contains(searchText) ||
                    bonus.getAmount().toString().contains(searchText) ||
                    String.valueOf(bonus.getEmployee_id()).contains(searchText) ||
                    (bonus.getDate() != null && bonus.getDate().format(dateFormatter).contains(searchText))) {
                finalFilteredList.add(bonus);
            }
        }

        tblBonus.setItems(FXCollections.observableArrayList(finalFilteredList));
    }

    public void refreshAllowanceData() {
        // Clear filters
        txtSearchAllowance.setText("");
        // Refresh data
        setupData();
        loadAllowanceData();
    }
    public void refreshBonusData() {
        // Clear filters
        cboYearFilter.setValue(null);
        cboYearFilter.setPromptText("Năm");
        cboMonthFilter.setValue(null);
        cboMonthFilter.setPromptText("Tháng");
        txtSearchBonus.setText("");
        // Refresh data
        setupData();
        loadBonusData();
    }

    // Update refreshData to maintain filters when data is refreshed
    public void refreshData() {
        // Save current filters
        Integer selectedYear = cboYearFilter.getValue();
        Integer selectedMonth = cboMonthFilter.getValue();

        setupData();
        loadAllowanceData();

        // Reload bonus data with filters applied
        if (selectedYear != null || selectedMonth != null) {
            filterBonusesByYearAndMonth(selectedYear, selectedMonth);
        } else {
            loadBonusData();
        }
    }


    private void loadAllowanceData() {
        tblAllowance.getItems().clear();
        tblAllowance.setItems(FXCollections.observableArrayList(allowanceList));
    }

    private void loadBonusData() {
        tblBonus.getItems().clear();
        tblBonus.setItems(FXCollections.observableArrayList(bonusList));
    }

    private void filterAllowances() {
        String searchText = txtSearchAllowance.getText().toLowerCase();
        List<AllowanceDTO> filteredList = new ArrayList<>();

        for (AllowanceDTO allowance : allowanceList) {
            if (allowance.getName().toLowerCase().contains(searchText) ||
                    allowance.getAmount().toString().contains(searchText)) {
                filteredList.add(allowance);
            }
        }

        tblAllowance.setItems(FXCollections.observableArrayList(filteredList));
    }

}