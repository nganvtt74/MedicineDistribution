package com.example.medicinedistribution.GUI;

import com.example.medicinedistribution.BUS.BUSFactory;
import com.example.medicinedistribution.BUS.Interface.AttendanceBUS;
import com.example.medicinedistribution.BUS.Interface.EmployeeBUS;
import com.example.medicinedistribution.DTO.AttendanceDTO;
import com.example.medicinedistribution.DTO.EmployeeDTO;
import com.example.medicinedistribution.Exception.UpdateFailedException;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

@Slf4j
public class EmployeeAttendanceController {

    @FXML
    private TextField txtSearch;

    @FXML
    private Button btnRefresh;

    @FXML
    private Button btnCheckIn;

    @FXML
    private Button btnCheckOut;

    @FXML
    private Label lblCurrentDate;

    @FXML
    private Label lblAttendanceMode;

    @FXML
    private TableView<EmployeeAttendanceData> tblEmployees;

    @FXML
    private TableColumn<EmployeeAttendanceData, Integer> colEmployeeId;

    @FXML
    private TableColumn<EmployeeAttendanceData, String> colEmployeeName;

    @FXML
    private TableColumn<EmployeeAttendanceData, String> colPosition;

    @FXML
    private TableColumn<EmployeeAttendanceData, String> colDepartment;

    @FXML
    private TableColumn<EmployeeAttendanceData, String> colCheckInTime;

    @FXML
    private TableColumn<EmployeeAttendanceData, String> colCheckOutTime;

    @FXML
    private TableColumn<EmployeeAttendanceData, String> colStatus;

    @FXML
    private TableColumn<EmployeeAttendanceData, EmployeeAttendanceData> colAction;

    @FXML
    private Label lblOnTimeCount;

    @FXML
    private Label lblLateCount;

    @FXML
    private Label lblAbsentCount;

    private BUSFactory busFactory;
    private AttendanceBUS attendanceBUS;
    private EmployeeBUS employeeBUS;
    private ObservableList<EmployeeAttendanceData> employeeAttendanceList;
    private FilteredList<EmployeeAttendanceData> filteredList;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private boolean isCheckInMode = true;
    private final LocalDate today = LocalDate.now();
    private final String currentDate = today.format(dateFormatter);

    public EmployeeAttendanceController(BUSFactory busFactory) {
        this.busFactory = busFactory;
        this.attendanceBUS = busFactory.getAttendanceBUS();
        this.employeeBUS = busFactory.getEmployeeBUS();
        this.employeeAttendanceList = FXCollections.observableArrayList();
    }

    public void initialize() {
        setupTableColumns();
        setupActionButtons();
        setupSearchFilter();
        setupAttendanceMode();

        // Initialize date display
        lblCurrentDate.setText(today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        // Load data
        loadData();
    }

    private void setupTableColumns() {
        colEmployeeId.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        colEmployeeName.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
        colPosition.setCellValueFactory(new PropertyValueFactory<>("position"));
        colCheckInTime.setCellValueFactory(new PropertyValueFactory<>("checkInTime"));
        colCheckOutTime.setCellValueFactory(new PropertyValueFactory<>("checkOutTime"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Setup action column with context-aware button
        setupActionColumn();
    }

    private void setupActionColumn() {
        Callback<TableColumn<EmployeeAttendanceData, EmployeeAttendanceData>, TableCell<EmployeeAttendanceData, EmployeeAttendanceData>> cellFactory =
            new Callback<>() {
                @Override
                public TableCell<EmployeeAttendanceData, EmployeeAttendanceData> call(TableColumn<EmployeeAttendanceData, EmployeeAttendanceData> param) {
                    return new TableCell<>() {
                        private final Button actionButton = new Button();

                        {
                            actionButton.setOnAction(event -> {
                                EmployeeAttendanceData data = getTableView().getItems().get(getIndex());
                                performAttendanceAction(data);
                            });
                        }

                        @Override
                        protected void updateItem(EmployeeAttendanceData data, boolean empty) {
                            super.updateItem(data, empty);

                            if (empty || data == null) {
                                setGraphic(null);
                                return;
                            }

                            // Configure button based on attendance state
                            if (data.getCheckInTime() == null || data.getCheckInTime().isEmpty()) {
                                actionButton.setText("Vào");
                                actionButton.getStyleClass().setAll("success-button");
                                actionButton.setDisable(!isCheckInMode);
                            } else if (data.getCheckOutTime() == null || data.getCheckOutTime().isEmpty() || data.getCheckOutTime().equals("null")) {
                                actionButton.setText("Ra");
                                actionButton.getStyleClass().setAll("warning-button");
                                actionButton.setDisable(isCheckInMode);
                            } else {
                                actionButton.setText("Đã chấm");
                                actionButton.getStyleClass().setAll("disabled-button");
                                actionButton.setDisable(true);
                            }

                            setGraphic(actionButton);
                        }
                    };
                }
            };

        colAction.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue()));
        colAction.setCellFactory(cellFactory);
    }

    private void setupActionButtons() {
        btnRefresh.setOnAction(event -> loadData());

        btnCheckIn.setOnAction(event -> {
            isCheckInMode = true;
            updateAttendanceMode();
            refreshTable();
        });

        btnCheckOut.setOnAction(event -> {
            isCheckInMode = false;
            updateAttendanceMode();
            refreshTable();
        });
    }

    private void setupSearchFilter() {
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            if (filteredList != null) {
                filteredList.setPredicate(employee -> {
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }

                    String lowerCaseFilter = newValue.toLowerCase();
                    if (employee.getEmployeeName().toLowerCase().contains(lowerCaseFilter)) {
                        return true;
                    } else if (String.valueOf(employee.getEmployeeId()).contains(lowerCaseFilter)) {
                        return true;
                    } else if (employee.getPosition().toLowerCase().contains(lowerCaseFilter)) {
                        return true;
                    } else if (employee.getDepartment().toLowerCase().contains(lowerCaseFilter)) {
                        return true;
                    }
                    return false;
                });
            }
        });
    }

    private void setupAttendanceMode() {
        updateAttendanceMode();
    }

    private void updateAttendanceMode() {
        if (isCheckInMode) {
            lblAttendanceMode.setText("CHẤM CÔNG VÀO");
            lblAttendanceMode.setStyle("-fx-font-weight: bold; -fx-background-color: -success; -fx-text-fill: white; -fx-background-radius: 5px;");
            btnCheckIn.setDisable(true);
            btnCheckOut.setDisable(false);
        } else {
            lblAttendanceMode.setText("CHẤM CÔNG RA");
            lblAttendanceMode.setStyle("-fx-font-weight: bold; -fx-background-color: -warning; -fx-text-fill: white; -fx-background-radius: 5px;");
            btnCheckIn.setDisable(false);
            btnCheckOut.setDisable(true);
        }
    }

    private void loadData() {
        try {
            List<EmployeeDTO> employees = employeeBUS.findAll();
            employeeAttendanceList.clear();

            int onTimeCount = 0;
            int lateCount = 0;
            int absentCount = 0;

            for (EmployeeDTO employee : employees) {
                AttendanceDTO attendance = attendanceBUS.getAttendance(employee.getEmployeeId(), currentDate);

                EmployeeAttendanceData data = new EmployeeAttendanceData();
                data.setEmployeeId(employee.getEmployeeId());
                data.setEmployeeName(employee.getFullName());
                data.setPosition(employee.getPositionName());

                // Set attendance data if exists
                if (attendance != null) {
                    data.setCheckInTime(String.valueOf(attendance.getCheck_in()));
                    data.setCheckOutTime(String.valueOf(attendance.getCheck_out()));
                    data.setStatus(getStatusText(attendance.getStatus()));

                    // Count statistics
                    switch (attendance.getStatus()) {
                        case 1: onTimeCount++; break;
                        case 2: lateCount++; break;
                        case 3:
                        case 4: absentCount++; break;
                    }
                } else {
                    data.setStatus("Chưa chấm công");
                    absentCount++;
                }

                employeeAttendanceList.add(data);
            }

            // Update statistics
            lblOnTimeCount.setText(String.valueOf(onTimeCount));
            lblLateCount.setText(String.valueOf(lateCount));
            lblAbsentCount.setText(String.valueOf(absentCount));

            // Apply filter
            filteredList = new FilteredList<>(employeeAttendanceList, p -> true);
            tblEmployees.setItems(filteredList);

        } catch (Exception e) {
            log.error("Error loading employee attendance data", e);
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải dữ liệu chấm công", e.getMessage());
        }
    }

    private String getStatusText(int status) {
        switch (status) {
            case 1: return "Đúng giờ";
            case 2: return "Đi trễ";
            case 3: return "Vắng không phép";
            case 4: return "Vắng có phép";
            default: return "Chưa chấm công";
        }
    }

    private void performAttendanceAction(EmployeeAttendanceData data) {
        try {
            boolean success = false;

            if (isCheckInMode) {
                // Check in
                success = attendanceBUS.checkInAttendance(data.getEmployeeId(), currentDate);
            } else {
                // Check out
                success = attendanceBUS.checkOutAttendance(data.getEmployeeId(), currentDate);
            }

            if (success) {
                loadData(); // Refresh data
            }
        } catch (UpdateFailedException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể chấm công", e.getMessage());
        } catch (Exception e) {
            log.error("Error performing attendance action", e);
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể chấm công", e.getMessage());
        }
    }

    private void refreshTable() {
        setupTableColumns();
        tblEmployees.refresh();
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Data class for the table view
    @Setter
    @Getter
    public static class EmployeeAttendanceData {
        // Getters and setters
        private int employeeId;
        private String employeeName;
        private String position;
        private String department;
        private String checkInTime;
        private String checkOutTime;
        private String status;

    }
}