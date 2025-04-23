package com.example.medicinedistribution.GUI;

import com.example.medicinedistribution.BUS.BUSFactory;
import com.example.medicinedistribution.BUS.Interface.AttendanceBUS;
import com.example.medicinedistribution.BUS.Interface.EmployeeBUS;
import com.example.medicinedistribution.DTO.AttendanceDTO;
import com.example.medicinedistribution.DTO.EmployeeDTO;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class AttendanceManagementController{

    @FXML
    private DatePicker datePicker;

    @FXML
    private ComboBox<StatusOption> cmbStatus;

    @FXML
    private TextField txtSearch;

    @FXML
    private Button btnSearch;

    @FXML
    private Button btnRefresh;

    @FXML
    private Button btnUpdateSelected;

    @FXML
    private Button btnUpdateAll;

    @FXML
    private ComboBox<StatusOption> cmbNewStatus;

    @FXML
    private TableView<AttendanceRecord> tblAttendance;

    @FXML
    private TableColumn<AttendanceRecord, Boolean> colSelect;

    @FXML
    private TableColumn<AttendanceRecord, Integer> colEmployeeId;

    @FXML
    private TableColumn<AttendanceRecord, String> colEmployeeName;

    @FXML
    private TableColumn<AttendanceRecord, String> colDate;

    @FXML
    private TableColumn<AttendanceRecord, String> colCheckInTime;

    @FXML
    private TableColumn<AttendanceRecord, String> colCheckOutTime;

    @FXML
    private TableColumn<AttendanceRecord, String> colStatus;

    @FXML
    private TableColumn<AttendanceRecord, AttendanceRecord> colEdit;

    @FXML
    private Label lblTotalCount;

    @FXML
    private Label lblOnTimeCount;

    @FXML
    private Label lblLateCount;

    @FXML
    private Label lblUnauthorizedCount;

    @FXML
    private Label lblAuthorizedCount;

    private final BUSFactory busFactory;
    private final AttendanceBUS attendanceBUS;
    private final EmployeeBUS employeeBUS;
    private ObservableList<AttendanceRecord> attendanceRecords;
    private FilteredList<AttendanceRecord> filteredRecords;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;

    // Status options for the dropdown lists
    private final ObservableList<StatusOption> statusOptions = FXCollections.observableArrayList(
            new StatusOption(0, "Tất cả"),
            new StatusOption(1, "Đúng giờ"),
            new StatusOption(2, "Đi trễ"),
            new StatusOption(3, "Vắng không phép"),
            new StatusOption(4, "Vắng có phép"),
            new StatusOption(5, "Ngoại lệ"),
            new StatusOption(6, "Nghỉ thai sản")
    );

    // Status options for updating records (no "All" option)
    private final ObservableList<StatusOption> updateStatusOptions = FXCollections.observableArrayList(
            new StatusOption(1, "Đúng giờ"),
            new StatusOption(2, "Đi trễ"),
            new StatusOption(3, "Vắng không phép"),
            new StatusOption(5, "Ngoại lệ")
    );
    private final ObservableList<StatusOption> updateSingleStatusOptions = FXCollections.observableArrayList(
            new StatusOption(1, "Đúng giờ"),
            new StatusOption(2, "Đi trễ"),
            new StatusOption(3, "Vắng không phép"),
            new StatusOption(4, "Vắng có phép"),
            new StatusOption(5, "Ngoại lệ"),
            new StatusOption(6, "Nghỉ thai sản")

    );


    public AttendanceManagementController(BUSFactory busFactory) {
        this.busFactory = busFactory;
        this.attendanceBUS = busFactory.getAttendanceBUS();
        this.employeeBUS = busFactory.getEmployeeBUS();
        this.attendanceRecords = FXCollections.observableArrayList();
    }

    public void initialize() {
        setupControls();
        setupColumns();
        setupListeners();
        loadData();
    }

    private void setupControls() {
        // Initialize date picker with current date
        datePicker.setValue(LocalDate.now());

        // Initialize combo boxes
        cmbStatus.setItems(statusOptions);
        cmbStatus.getSelectionModel().selectFirst();

        cmbNewStatus.setItems(updateStatusOptions);
        cmbNewStatus.getSelectionModel().selectFirst();
    }

private void setupColumns() {
    // Configure checkbox column
    colSelect.setCellValueFactory(param -> param.getValue().selectedProperty());
    colSelect.setCellFactory(CheckBoxTableCell.forTableColumn(colSelect));
    colSelect.setEditable(true);

    // Setup data columns
    colEmployeeId.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getEmployeeId()));
    colEmployeeName.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getEmployeeName()));
    colDate.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getDate()));
    colCheckInTime.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getCheckInTime()));
    colCheckOutTime.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getCheckOutTime()));
    colStatus.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getStatusText()));

    // Setup edit action column
    setupEditColumn();
    tblAttendance.setEditable(true);

    tblAttendance.setOnKeyPressed(event -> {
        if (event.getCode() == KeyCode.ENTER) {
            // Lấy các chỉ số hiện đang được chọn
            ObservableList<Integer> selectedIndices = tblAttendance.getSelectionModel().getSelectedIndices();

            if (!selectedIndices.isEmpty()) {
                // Lấy mục đầu tiên để xác định trạng thái mới
                AttendanceRecord firstRecord = tblAttendance.getItems().get(selectedIndices.get(0));
                boolean newState = !firstRecord.isSelected();

                // Chỉ cập nhật các mục có chỉ số trong danh sách selectedIndices
                for (Integer index : selectedIndices) {
                    if (index >= 0 && index < tblAttendance.getItems().size()) {
                        AttendanceRecord record = tblAttendance.getItems().get(index);
                        record.setSelected(newState);
                    }
                }

                tblAttendance.refresh();
            }
        }
    });
    }

    private void setupEditColumn() {
        Callback<TableColumn<AttendanceRecord, AttendanceRecord>, TableCell<AttendanceRecord, AttendanceRecord>> cellFactory =
                new Callback<>() {
                    @Override
                    public TableCell<AttendanceRecord, AttendanceRecord> call(TableColumn<AttendanceRecord, AttendanceRecord> param) {
                        return new TableCell<>() {
                            private final Button editButton = new Button("Sửa");

                            {
                                editButton.getStyleClass().add("primary-button");
                                editButton.setOnAction(event -> {
                                    AttendanceRecord record = getTableView().getItems().get(getIndex());
                                    showEditDialog(record);
                                });
                            }

                            @Override
                            protected void updateItem(AttendanceRecord record, boolean empty) {
                                super.updateItem(record, empty);
                                if (empty || record == null) {
                                    setGraphic(null);
                                } else {
                                    setGraphic(editButton);
                                }
                            }
                        };
                    }
                };

        colEdit.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue()));
        colEdit.setCellFactory(cellFactory);
    }

    private void setupListeners() {
        // Search button action
        btnSearch.setOnAction(event -> applyFilters());

        // Refresh button action
        btnRefresh.setOnAction(event -> {
            resetFilters();
            loadData();
        });

        // Update selected records action
        btnUpdateSelected.setOnAction(event -> updateSelectedRecords());

        // Update all records action
        btnUpdateAll.setOnAction(event -> updateAllRecords());

        // Search text field listener
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            if (filteredRecords != null) {
                applyTextFilter(newValue);
            }
        });

        // Date picker listener
        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue != null && !oldValue.equals(newValue)) {
                loadData();
            }
        });

        // Status filter listener
        cmbStatus.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue != null && !oldValue.equals(newValue)) {
                applyFilters();
            }
        });
    }

    private void loadData() {
        try {
            LocalDate selectedDate = datePicker.getValue();
            String dateStr = selectedDate.format(dateFormatter);

            // Get all attendance records for the selected date
            List<AttendanceDTO> attendances = attendanceBUS.getAttendanceByDate(dateStr);

            // Get employees to match with attendance records
            List<EmployeeDTO> employees = employeeBUS.findAll();

            // Create a map of employees by ID for easier lookup
            HashMap<Integer, EmployeeDTO> employeeMap = new HashMap<>();
            for (EmployeeDTO employee : employees) {
                employeeMap.put(employee.getEmployeeId(), employee);
            }

            // Clear and reload attendance records
            attendanceRecords.clear();

            // Statistics counters
            int onTime = 0;
            int late = 0;
            int unauthorizedAbsent = 0;
            int authorizedAbsent = 0;

            // Process attendance records
            for (AttendanceDTO attendance : attendances) {
                int employeeId = attendance.getEmployee_id();
                EmployeeDTO employee = employeeMap.get(employeeId);

                if (employee != null) {
                    AttendanceRecord record = new AttendanceRecord(
                            employeeId,
                            employee.getFullName(),
                            dateStr,
                            attendance.getCheck_in()==null ? null : attendance.getCheck_in().toString(),
                            attendance.getCheck_out()==null ? null : attendance.getCheck_out().toString(),
                            attendance.getStatus()
                    );

                    attendanceRecords.add(record);

                    // Update statistics
                    switch (attendance.getStatus()) {
                        case 1: onTime++; break;
                        case 2: late++; break;
                        case 3: unauthorizedAbsent++; break;
                        case 4: authorizedAbsent++; break;
                    }
                }
            }

            // Apply initial filtering
            filteredRecords = new FilteredList<>(attendanceRecords, p -> true);
            tblAttendance.setItems(filteredRecords);
            //set multi select
            tblAttendance.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

            // Update statistics labels
            updateStatistics(onTime, late, unauthorizedAbsent, authorizedAbsent);

        } catch (Exception e) {
            log.error("Error loading attendance records", e);
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải dữ liệu chấm công", e.getMessage());
        }
    }

    private void applyFilters() {
        // Get the selected status
        StatusOption selectedStatus = cmbStatus.getValue();
        int statusValue = selectedStatus != null ? selectedStatus.value() : 0;

        // Get the search text
        String searchText = txtSearch.getText();

        // Apply filters
        if (filteredRecords != null) {
            filteredRecords.setPredicate(record -> {
                // Status filter
                boolean statusMatch = statusValue == 0 || record.getStatus() == statusValue;

                // Text search filter
                boolean textMatch = searchText == null || searchText.isEmpty() ||
                        String.valueOf(record.getEmployeeId()).contains(searchText) ||
                        record.getEmployeeName().toLowerCase().contains(searchText.toLowerCase());

                return statusMatch && textMatch;
            });

            // Update statistics based on filtered records
            updateFilteredStatistics();
        }
    }

    private void applyTextFilter(String newValue) {
        filteredRecords.setPredicate(record -> {
            // Get the selected status
            StatusOption selectedStatus = cmbStatus.getValue();
            int statusValue = selectedStatus != null ? selectedStatus.value() : 0;

            // Status filter
            boolean statusMatch = statusValue == 0 || record.getStatus() == statusValue;

            // Text search filter
            boolean textMatch = newValue == null || newValue.isEmpty() ||
                    String.valueOf(record.getEmployeeId()).contains(newValue) ||
                    record.getEmployeeName().toLowerCase().contains(newValue.toLowerCase());

            return statusMatch && textMatch;
        });

        // Update statistics based on filtered records
        updateFilteredStatistics();
    }

    private void resetFilters() {
        txtSearch.clear();
        cmbStatus.getSelectionModel().selectFirst();
        datePicker.setValue(LocalDate.now());
    }

    private void updateSelectedRecords() {
        // Get the selected status
        StatusOption selectedStatus = cmbNewStatus.getValue();
        if (selectedStatus == null) {
            showAlert(Alert.AlertType.WARNING, "Thông báo", "Chọn trạng thái", "Vui lòng chọn trạng thái cần cập nhật");
            return;
        }

        // Get selected records
        List<AttendanceRecord> selectedRecords = attendanceRecords.stream()
                .filter(AttendanceRecord::isSelected)
                .collect(Collectors.toList());

        if (selectedRecords.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thông báo", "Chưa chọn bản ghi", "Vui lòng chọn ít nhất một bản ghi để cập nhật");
            return;
        }

        // Confirm update
        boolean confirmed = showConfirmation(
                "Xác nhận cập nhật",
                "Bạn có chắc muốn cập nhật " + selectedRecords.size() + " bản ghi thành \"" +
                selectedStatus.text() + "\"?");

        if (confirmed) {
            updateAttendanceStatus(selectedRecords, selectedStatus.value());
        }
    }

    private void updateAllRecords() {
        // Get the selected status
        StatusOption selectedStatus = cmbNewStatus.getValue();
        if (selectedStatus == null) {
            showAlert(Alert.AlertType.WARNING, "Thông báo", "Chọn trạng thái", "Vui lòng chọn trạng thái cần cập nhật");
            return;
        }

        // Confirm update all
        boolean confirmed = showConfirmation(
                "Xác nhận cập nhật tất cả",
                "Bạn có chắc muốn cập nhật TẤT CẢ bản ghi thành \"" +
                selectedStatus.text() + "\"?\nThao tác này sẽ ảnh hưởng đến tất cả nhân viên trong ngày đã chọn.");

        if (confirmed) {
            try {
                String dateStr = datePicker.getValue().format(dateFormatter);
                boolean success = attendanceBUS.updateAllAttendanceStatusInDate(dateStr, selectedStatus.value());

                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Thành công",
                            "Cập nhật thành công",
                            "Đã cập nhật tất cả bản ghi chấm công ngày " + dateStr);
                    loadData();
                }
            } catch (Exception e) {
                log.error("Error updating all attendance records", e);
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Cập nhật thất bại", e.getMessage());
            }
        }
    }

    private void updateAttendanceStatus(List<AttendanceRecord> records, int newStatus) {
        try {
            String dateStr = datePicker.getValue().format(dateFormatter);
            int successCount = 0;
            List<String> failedEmployees = new ArrayList<>();

            // Update each selected record
            for (AttendanceRecord record : records) {
                try {
                    boolean success = attendanceBUS.updateAttendanceStatus(
                            record.getEmployeeId(), dateStr, newStatus);

                    if (success) {
                        successCount++;
                    } else {
                        failedEmployees.add(record.getEmployeeName());
                    }
                } catch (Exception e) {
                    log.error("Error updating attendance for employee ID: " + record.getEmployeeId(), e);
                    failedEmployees.add(record.getEmployeeName());
                }
            }

            // Show result
            if (successCount == records.size()) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công",
                        "Cập nhật thành công",
                        "Đã cập nhật " + successCount + " bản ghi");
                loadData();
            } else {
                String message = "Đã cập nhật " + successCount + "/" + records.size() + " bản ghi.\n";
                if (!failedEmployees.isEmpty()) {
                    message += "Không thể cập nhật cho: " + String.join(", ", failedEmployees);
                }
                showAlert(Alert.AlertType.WARNING, "Kết quả", "Cập nhật không đầy đủ", message);
                loadData();
            }
        } catch (Exception e) {
            log.error("Error batch updating attendance records", e);
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Cập nhật thất bại", e.getMessage());
        }
    }
private void showEditDialog(AttendanceRecord record) {
    Dialog<Integer> dialog = new Dialog<>();
    dialog.setTitle("Sửa trạng thái chấm công");

    // Create custom header
    Label headerLabel = new Label("Cập nhật trạng thái cho " + record.getEmployeeName());
    headerLabel.getStyleClass().add("dialog-header");
    headerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

    // Set button types
    ButtonType saveButtonType = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
    ButtonType cancelButtonType = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
    dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);

    // Create layout container with styling
    VBox content = new VBox(12);
    content.setPadding(new Insets(20, 25, 15, 25));
    content.setAlignment(Pos.TOP_LEFT);
    content.getStyleClass().add("content-panel");

    // Add employee info in a styled container
    VBox infoBox = new VBox(8);
    infoBox.getStyleClass().add("info-panel");
    infoBox.setPadding(new Insets(15));

    Label employeeIdLabel = new Label("Mã nhân viên: " + record.getEmployeeId());
    employeeIdLabel.getStyleClass().add("info-label");

    Label dateLabel = new Label("Ngày: " + record.getDate());
    dateLabel.getStyleClass().add("info-label");

    Label checkInLabel = new Label("Giờ vào: " + record.getCheckInTime());
    checkInLabel.getStyleClass().add("info-label");

    infoBox.getChildren().addAll(employeeIdLabel, dateLabel, checkInLabel);

    // Create separator with styling
    Separator separator = new Separator();
    separator.getStyleClass().add("separator");
    separator.setPadding(new Insets(5, 0, 10, 0));

    // Create status ComboBox with label in a form layout
    Label statusLabel = new Label("Chọn trạng thái:");
    statusLabel.getStyleClass().add("form-label");

    ComboBox<StatusOption> statusComboBox = new ComboBox<>(updateSingleStatusOptions);
    statusComboBox.setPrefWidth(250);
    statusComboBox.getStyleClass().add("form-control");
    statusComboBox.getSelectionModel().select(
            updateSingleStatusOptions.stream()
                    .filter(option -> option.value() == record.getStatus())
                    .findFirst()
                    .orElse(updateSingleStatusOptions.getFirst())
    );

    VBox formGroup = new VBox(6);
    formGroup.getChildren().addAll(statusLabel, statusComboBox);

    // Add all components to layout
    content.getChildren().addAll(
            headerLabel,
            infoBox,
            separator,
            formGroup
    );

    // Set dialog content and apply styling
    DialogPane dialogPane = dialog.getDialogPane();
    dialogPane.setContent(content);
    dialogPane.getStyleClass().addAll("custom-dialog", "modern-dialog");
    dialogPane.getStylesheets().add(getClass().getResource("../../../../css/main-style.css").toExternalForm());

    // Style the buttons
    Button saveButton = (Button) dialogPane.lookupButton(saveButtonType);
    saveButton.getStyleClass().addAll("primary-button", "btn-save");
    saveButton.setDefaultButton(true);

    Button cancelButton = (Button) dialogPane.lookupButton(cancelButtonType);
    cancelButton.getStyleClass().addAll("secondary-button", "btn-cancel");

    // Convert result to integer status value
    dialog.setResultConverter(dialogButton -> {
        if (dialogButton == saveButtonType) {
            return statusComboBox.getValue().value();
        }
        return null;
    });

    // Show dialog and process result
    dialog.showAndWait().ifPresent(newStatus -> {
        try {
            String dateStr = record.getDate();
            boolean success = attendanceBUS.updateAttendanceStatus(
                    record.getEmployeeId(), dateStr, newStatus);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công",
                        "Cập nhật thành công",
                        "Đã cập nhật trạng thái chấm công cho " + record.getEmployeeName());
                loadData();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi",
                        "Cập nhật thất bại",
                        "Không thể cập nhật trạng thái chấm công");
            }
        } catch (Exception e) {
            log.error("Error updating attendance status", e);
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Cập nhật thất bại", e.getMessage());
        }
    });
}

    private void updateStatistics(int onTime, int late, int unauthorizedAbsent, int authorizedAbsent) {
        int total = onTime + late + unauthorizedAbsent + authorizedAbsent;
        lblTotalCount.setText(String.valueOf(total));
        lblOnTimeCount.setText(String.valueOf(onTime));
        lblLateCount.setText(String.valueOf(late));
        lblUnauthorizedCount.setText(String.valueOf(unauthorizedAbsent));
        lblAuthorizedCount.setText(String.valueOf(authorizedAbsent));
    }

    private void updateFilteredStatistics() {
        int onTime = 0;
        int late = 0;
        int unauthorizedAbsent = 0;
        int authorizedAbsent = 0;

        // Count based on filtered records
        for (AttendanceRecord record : filteredRecords) {
            switch (record.getStatus()) {
                case 1: onTime++; break;
                case 2: late++; break;
                case 3: unauthorizedAbsent++; break;
                case 4: authorizedAbsent++; break;
            }
        }

        // Update statistics
        updateStatistics(onTime, late, unauthorizedAbsent, authorizedAbsent);
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    // Helper classes for controller implementation

    public record StatusOption(int value, String text) {

    @Override
        public String toString() {
            return text;
        }
    }

    @Getter
    public static class AttendanceRecord {
        private final int employeeId;
        private final String employeeName;
        private final String date;
        private final String checkInTime;
        private final String checkOutTime;
        private final int status;
        private final SimpleBooleanProperty selected = new SimpleBooleanProperty(false);

        public AttendanceRecord(int employeeId, String employeeName, String date,
                               String checkInTime, String checkOutTime, int status) {
            this.employeeId = employeeId;
            this.employeeName = employeeName;
            this.date = date;
            this.checkInTime = checkInTime != null ? checkInTime : "";
            this.checkOutTime = checkOutTime != null ? checkOutTime : "";
            this.status = status;
        }

        public String getStatusText() {
            return switch (status) {
                case 0 -> "Chưa chấm công";
                case 1 -> "Đúng giờ";
                case 2 -> "Đi trễ";
                case 3 -> "Vắng không phép";
                case 4 -> "Vắng có phép";
                case 5 -> "Ngoại lệ";
                case 6 -> "Nghỉ thai sản";
                default -> "Không xác định";
            };
        }

        public boolean isSelected() {
            return selected.get();
        }

        public SimpleBooleanProperty selectedProperty() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected.set(selected);
        }
    }
}