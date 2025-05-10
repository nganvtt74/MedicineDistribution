package com.example.medicinedistribution.GUI;

import com.example.medicinedistribution.BUS.BUSFactory;
import com.example.medicinedistribution.BUS.Interface.EmployeeBUS;
import com.example.medicinedistribution.BUS.Interface.PositionBUS;
import com.example.medicinedistribution.DTO.EmployeeDTO;
import com.example.medicinedistribution.DTO.PositionDTO;
import com.example.medicinedistribution.Exception.DeleteFailedException;
import com.example.medicinedistribution.Exception.PermissionDeniedException;
//import com.example.medicinedistribution.GUI.SubAction.ActionType;
//import com.example.medicinedistribution.GUI.SubAction.EmployeeAction;
import com.example.medicinedistribution.GUI.SubAction.EmployeeAction;
import com.example.medicinedistribution.GUI.SubAction.SubAction;
import com.example.medicinedistribution.Util.NotificationUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EmployeeController {
    @FXML
    private TableView<EmployeeDTO> tblEmployees;

    @FXML
    private TableColumn<EmployeeDTO, Integer> colEmployeeId;

    @FXML
    private TableColumn<EmployeeDTO, String> colFirstName;

    @FXML
    private TableColumn<EmployeeDTO, String> colLastName;

    @FXML
    private TableColumn<EmployeeDTO, String> colGender;

    @FXML
    private TableColumn<EmployeeDTO, String> colPosition;

    @FXML
    private TableColumn<EmployeeDTO, LocalDate> colHireDate;

    @FXML
    private TableColumn<EmployeeDTO, BigDecimal> colBasicSalary;

    @FXML
    private TableColumn<EmployeeDTO, String> colStatus;

    @FXML
    private TableColumn<EmployeeDTO, Void> colDetails;

    @FXML
    private TextField txtSearch;

    @FXML
    private Button btnAdd;

    @FXML
    private Button btnEdit;

    @FXML
    private Button btnDelete;

    @FXML
    private Button btnRefresh;

    private final EmployeeBUS employeeBUS;
    private final PositionBUS positionBUS;
    private final DateTimeFormatter dateFormatter;
    private final BUSFactory busFactory;
    private ArrayList<EmployeeDTO> employees;

    public EmployeeController(BUSFactory busFactory) {
        this.busFactory = busFactory;
        this.employeeBUS = busFactory.getEmployeeBUS();
        this.positionBUS = busFactory.getPositionBUS();
        this.dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        setupActionButtons();
        setupSearch();
        Platform.runLater(this::refreshData);
    }

    private void setupTableColumns() {
        // Basic columns
        colEmployeeId.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colGender.setCellValueFactory(new PropertyValueFactory<>("gender"));

        // Format date
        colHireDate.setCellValueFactory(new PropertyValueFactory<>("hireDate"));
        colHireDate.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(dateFormatter));
                }
            }
        });

        // Format salary
        colBasicSalary.setCellValueFactory(new PropertyValueFactory<>("basicSalary"));
        colBasicSalary.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%,.0f VND", item));
                }
            }
        });

        // Position name from position ID
        colPosition.setCellValueFactory(cellData -> {
            int positionId = cellData.getValue().getPositionId();
            try {
                PositionDTO position = positionBUS.findById(positionId);
                return new SimpleStringProperty(position != null ? position.getPositionName() : "");
            } catch (Exception e) {
                return new SimpleStringProperty("N/A");
            }
        });

        // Status column
        colStatus.setCellValueFactory(cellData -> {
            Integer status = cellData.getValue().getStatus();
            String statusText;
            if (status == null) {
                statusText = "N/A";
            } else if (status == 1) {
                statusText = "Đang làm việc";
            } else if (status == 0) {
                statusText = "Đã nghỉ việc";
            } else if (status == 2) {
                statusText = "Nghỉ thai sản";
            }else {
                statusText = "Không xác định";
            }
            return new SimpleStringProperty(statusText);
        });

        // Setup details button column
        colDetails.setStyle("-fx-alignment: CENTER;");
        setupDetailsColumn();
    }

    private void setupDetailsColumn() {
        Callback<TableColumn<EmployeeDTO, Void>, TableCell<EmployeeDTO, Void>> cellFactory =
                param -> new TableCell<>() {
            private final HBox hbox = new HBox(5);
            private final Button detailButton = new Button("Chi tiết");
            private final Button dependentsButtons = new Button("Thân nhân ");

            {
                detailButton.getStyleClass().add("primary-button");
                detailButton.setOnAction(event -> {
                    EmployeeDTO employee = getTableView().getItems().get(getIndex());
                    showEmployeeDetails(employee);
                });
                dependentsButtons.getStyleClass().add("primary-button");
                dependentsButtons.setOnAction(event -> {
                    EmployeeDTO employee = getTableView().getItems().get(getIndex());
                    try {
                        DependentsController.show(employee, busFactory);
                    } catch (Exception e) {
                        NotificationUtil.showErrorNotification("Lỗi", "Không thể hiển thị thân nhân: " + e.getMessage());
                    }
                });
                hbox.getChildren().addAll(detailButton, dependentsButtons);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(hbox);
                }
            }
        };

        colDetails.setCellFactory(cellFactory);
    }

    private void showEmployeeDetails(EmployeeDTO employee) {
        try {
            EmployeeDetailsController.show(employee, busFactory);
        } catch (Exception e) {
            NotificationUtil.showErrorNotification("Lỗi", "Không thể hiển thị chi tiết nhân viên: " + e.getMessage());
        }
    }

    private void setupActionButtons() {
        // Add button
        btnAdd.setOnAction(event -> {
            try {
                EmployeeAction.showDialog(busFactory, this, SubAction.ActionType.ADD);
            } catch (Exception e) {
                NotificationUtil.showErrorNotification("Lỗi", "Không thể mở form thêm nhân viên: " + e.getMessage());
            }
        });

        // Edit button
        btnEdit.setOnAction(event -> {
            EmployeeDTO selected = tblEmployees.getSelectionModel().getSelectedItem();
            if (selected == null) {
                NotificationUtil.showErrorNotification("Cảnh báo", "Vui lòng chọn nhân viên để chỉnh sửa");
                return;
            }
            try {
                EmployeeAction.showDialog(busFactory, this, SubAction.ActionType.EDIT, selected);
            } catch (Exception e) {
                NotificationUtil.showErrorNotification("Lỗi", "Không thể mở form sửa nhân viên: " + e.getMessage());
            }
        });

        // Delete button
        btnDelete.setOnAction(event -> {
            EmployeeDTO selected = tblEmployees.getSelectionModel().getSelectedItem();
            if (selected == null) {
                NotificationUtil.showErrorNotification("Cảnh báo", "Vui lòng chọn nhân viên để xóa");
                return;
            }

            if (NotificationUtil.showConfirmation("Xác nhận xóa", "Bạn có chắc chắn muốn xóa nhân viên này?")) {
                deleteEmployee(selected);
            }
        });

        // Refresh button
        btnRefresh.setOnAction(event -> refreshData());
    }

    private void setupSearch() {
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                refreshData();
            } else {
                searchEmployees(newValue);
            }
        });
    }

    private void searchEmployees(String keyword) {
        try {
            List<EmployeeDTO> filteredEmployees = new ArrayList<>();
            for (EmployeeDTO employee : employees) {
                if (employee.getFirstName().toLowerCase().contains(keyword.toLowerCase()) ||
                        employee.getLastName().toLowerCase().contains(keyword.toLowerCase())) {
                    filteredEmployees.add(employee);
                }
            }
            tblEmployees.setItems(FXCollections.observableArrayList(filteredEmployees));
        } catch (Exception e) {
            NotificationUtil.showErrorNotification("Lỗi tìm kiếm", "Không thể tìm kiếm nhân viên: " + e.getMessage());
        }
    }

    private void deleteEmployee(EmployeeDTO employee) {
        try {
            boolean success = employeeBUS.delete(employee.getEmployeeId());
            if (success) {
                NotificationUtil.showSuccessNotification("Thành công", "Xóa nhân viên thành công");
                refreshData();
            } else {
                NotificationUtil.showErrorNotification("Cảnh báo", "Không thể xóa nhân viên");
            }
        } catch (DeleteFailedException e) {
            NotificationUtil.showErrorNotification("Lỗi xóa", e.getMessage());
        } catch (PermissionDeniedException e) {
            NotificationUtil.showErrorNotification("Lỗi quyền", "Bạn không có quyền xóa nhân viên");
        } catch (Exception e) {
            NotificationUtil.showErrorNotification("Lỗi", "Đã xảy ra lỗi: " + e.getMessage());
        }
    }

    public void refreshData() {
        try {
            if (employees != null) {
                employees.clear();
            } else {
                employees = new ArrayList<>();
            }
            employees.addAll(employeeBUS.findAll());
            tblEmployees.setItems(FXCollections.observableArrayList(employees));
        } catch (Exception e) {
            NotificationUtil.showErrorNotification("Lỗi tải dữ liệu", "Không thể tải danh sách nhân viên: " + e.getMessage());
        }
    }
}