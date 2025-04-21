package com.example.medicinedistribution.GUI.SubSelect;

import com.example.medicinedistribution.BUS.BUSFactory;
import com.example.medicinedistribution.BUS.Interface.EmployeeBUS;
import com.example.medicinedistribution.DTO.EmployeeDTO;
import com.example.medicinedistribution.Util.NotificationUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class EmployeeSelectController {

    @FXML
    private Label lblTitle;

    @FXML
    private TextField txtSearch;

    @FXML
    private TableView<EmployeeDTO> tblItems;

    @FXML
    private TableColumn<EmployeeDTO, String> colId;

    @FXML
    private TableColumn<EmployeeDTO, String> colLastName;

    @FXML
    private TableColumn<EmployeeDTO, String> colFirstName;

    @FXML
    private TableColumn<EmployeeDTO, String> colPhone;

    @FXML
    private Button btnSelect;

    @FXML
    private Button btnCancel;

    private final EmployeeBUS employeeBUS;
    private ObservableList<EmployeeDTO> employeeList;
    private List<EmployeeDTO> allEmployees;

    @Setter
    private SelectionHandler<EmployeeDTO> selectionHandler;

    public EmployeeSelectController(BUSFactory busFactory) {
        this.employeeBUS = busFactory.getEmployeeBUS();
    }

    public void initialize() {
        setupTable();
        setupSearchField();
        setupButtons();
        loadEmployees();
    }

    private void setupTable() {
        colId= new TableColumn<>("Mã NV");
        colLastName = new TableColumn<>("Họ");
        colFirstName = new TableColumn<>("Tên");
        colPhone = new TableColumn<>("Số điện thoại");
        colId.setMaxWidth(50);
        colLastName.setMinWidth(150);
        colFirstName.setMinWidth(150);
        colId.setStyle("-fx-alignment: CENTER;");
        colLastName.setStyle("-fx-alignment: CENTER;");
        colFirstName.setStyle("-fx-alignment: CENTER;");
        colPhone.setStyle("-fx-alignment: CENTER;");

        tblItems.getColumns().addAll(colId, colLastName, colFirstName, colPhone);


        colId.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));


        // Double click to select
        tblItems.setRowFactory(tv -> {
            TableRow<EmployeeDTO> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleSelection();
                }
            });
            return row;
        });
    }

    private void setupSearchField() {
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filterEmployees(newValue);
        });
    }

    private void filterEmployees(String searchText) {
        if (allEmployees == null) return;

        searchText = searchText.toLowerCase();
        String finalSearchText = searchText;
        List<EmployeeDTO> filteredList = allEmployees.stream()
                .filter(emp ->
                        emp.getFirstName().toLowerCase().contains(finalSearchText) ||
                        emp.getLastName().toLowerCase().contains(finalSearchText) ||
                        emp.getPhone().toLowerCase().contains(finalSearchText))
                .collect(Collectors.toList());

        tblItems.setItems(FXCollections.observableArrayList(filteredList));
    }

    private void setupButtons() {
        btnSelect.setOnAction(event -> handleSelection());

        btnCancel.setOnAction(event -> {
            if (selectionHandler != null) {
                selectionHandler.onSelectionCancelled();
            }
            closeDialog();
        });
    }

    private void handleSelection() {
        EmployeeDTO selectedEmployee = tblItems.getSelectionModel().getSelectedItem();
        log.info("Selected employee: {}", selectedEmployee);
        if (selectedEmployee != null && selectionHandler != null) {
            if (selectionHandler.validateSelection(selectedEmployee)) {
                selectionHandler.onItemSelected(selectedEmployee);
                closeDialog();
            }
        } else {
            NotificationUtil.showErrorNotification("Lỗi", "Vui lòng chọn một nhân viên.");
        }
    }

    private void loadEmployees() {
        try {
            allEmployees = employeeBUS.getEmployeeWithoutAccount();
            employeeList = FXCollections.observableArrayList(allEmployees);
            tblItems.setItems(employeeList);
        } catch (Exception e) {
            NotificationUtil.showErrorNotification("Lỗi", "Không thể tải danh sách nhân viên: " + e.getMessage());
        }
    }

    /**
     * Allows filtering the displayed employees based on specific criteria
     * @param filter A filter function to apply to the employee list
     */
    public void setEmployeeFilter(java.util.function.Predicate<EmployeeDTO> filter) {
        if (allEmployees != null) {
            List<EmployeeDTO> filteredList = allEmployees.stream()
                    .filter(filter)
                    .collect(Collectors.toList());
            tblItems.setItems(FXCollections.observableArrayList(filteredList));
        }
    }

    private void closeDialog() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }
}