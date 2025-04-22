package com.example.medicinedistribution.GUI;

import com.example.medicinedistribution.BUS.BUSFactory;
import com.example.medicinedistribution.BUS.Interface.DepartmentBUS;
import com.example.medicinedistribution.BUS.Interface.PositionBUS;
import com.example.medicinedistribution.DTO.DepartmentDTO;
import com.example.medicinedistribution.DTO.PositionDTO;
import com.example.medicinedistribution.Exception.DeleteFailedException;
import com.example.medicinedistribution.Exception.PermissionDeniedException;
import com.example.medicinedistribution.GUI.SubAction.DepartmentAction;
import com.example.medicinedistribution.GUI.SubAction.PositionAction;
import com.example.medicinedistribution.Util.NotificationUtil;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class DepartmentController {
    // Department controls
    @FXML
    private Button btnDeptAdd;
    @FXML
    private Button btnDeptDelete;
    @FXML
    private Button btnDeptEdit;
    @FXML
    private Button btnDeptRefresh;
    @FXML
    private TableColumn<DepartmentDTO, Integer> colDepartmentId;
    @FXML
    private TableColumn<DepartmentDTO, String> colDepartmentName;
    @FXML
    private TableView<DepartmentDTO> tblDepartments;
    @FXML
    private TextField txtDeptSearch;

    // Position controls
    @FXML
    private Button btnPosAdd;
    @FXML
    private Button btnPosDelete;
    @FXML
    private Button btnPosEdit;
    @FXML
    private TableColumn<PositionDTO, BigDecimal> colAllowance;
    @FXML
    private TableColumn<PositionDTO, Integer> colPositionId;
    @FXML
    private TableColumn<PositionDTO, String> colPositionName;
    @FXML
    private TableView<PositionDTO> tblPositions;
    @FXML
    private TextField txtPosSearch;

    private BUSFactory busFactory;
    private DepartmentBUS departmentBUS;
    private PositionBUS positionBUS;
    private ArrayList<DepartmentDTO> departmentList;
    private ArrayList<PositionDTO> positionList;

    public DepartmentController(BUSFactory busFactory) {
        this.busFactory = busFactory;
    }

    public void initialize() {
        departmentBUS = busFactory.getDepartmentBUS();
        positionBUS = busFactory.getPositionBUS();

        setupUIData();
        setupDepartmentUI();
        setupPositionUI();
        setupDepartmentTable();
        setupPositionTable();

        loadDepartmentData();
        loadAllPositionData();

        setupTableRelationship();
    }

    public void setupUIData() {
        if (departmentList != null) {
            departmentList.clear();
        }
        departmentList = new ArrayList<>(departmentBUS.findAll());

        if (positionList != null) {
            positionList.clear();
        }
        positionList = new ArrayList<>(positionBUS.findAll());
    }

    private void setupDepartmentUI() {
        btnDeptAdd.setOnAction(event -> {
            DepartmentAction.showDialog(busFactory, this, DepartmentAction.ActionType.ADD);
        });

        btnDeptEdit.setOnAction(event -> {
            DepartmentDTO departmentDTO = tblDepartments.getSelectionModel().getSelectedItem();
            if (departmentDTO == null) {
                NotificationUtil.showErrorNotification("Lỗi", "Vui lòng chọn một phòng ban để chỉnh sửa.");
                return;
            }
            DepartmentAction.showDialog(busFactory, this, DepartmentAction.ActionType.EDIT, departmentDTO);
        });

        btnDeptDelete.setOnAction(event -> {
            if (!NotificationUtil.showConfirmation("Xác nhận", "Bạn có chắc chắn muốn xóa phòng ban này?")) {
                return;
            }
            DepartmentDTO departmentDTO = tblDepartments.getSelectionModel().getSelectedItem();
            if (departmentDTO == null) {
                NotificationUtil.showErrorNotification("Lỗi", "Vui lòng chọn một phòng ban để xóa.");
                return;
            }
            try {
                if (departmentBUS.delete(departmentDTO.getDepartmentId())) {
                    NotificationUtil.showSuccessNotification("Thành công", "Xóa phòng ban thành công.");
                    setupUIData();
                    loadDepartmentData();
                    loadAllPositionData();
                }
            } catch (PermissionDeniedException e) {
                NotificationUtil.showErrorNotification("Quyền truy cập", e.getMessage());
            } catch (DeleteFailedException e) {
                NotificationUtil.showErrorNotification("Lỗi", e.getMessage());
            } catch (Exception e) {
                NotificationUtil.showErrorNotification("Lỗi", "Có lỗi không xác định: " + e.getMessage());
            }
        });

        btnDeptRefresh.setOnAction(event -> {
            setupUIData();
            loadDepartmentData();
            loadAllPositionData();
            txtDeptSearch.clear();
        });

        txtDeptSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filterDepartments();
        });
    }

    private void setupPositionUI() {
        btnPosAdd.setOnAction(event -> {
            DepartmentDTO selectedDepartment = tblDepartments.getSelectionModel().getSelectedItem();
            if (selectedDepartment == null) {
                NotificationUtil.showErrorNotification("Lỗi", "Vui lòng chọn một phòng ban trước khi thêm chức vụ.");
                return;
            }
            PositionAction.showDialog(busFactory, this, PositionAction.ActionType.ADD, selectedDepartment);
        });

        btnPosEdit.setOnAction(event -> {
            PositionDTO positionDTO = tblPositions.getSelectionModel().getSelectedItem();
            if (positionDTO == null) {
                NotificationUtil.showErrorNotification("Lỗi", "Vui lòng chọn một chức vụ để chỉnh sửa.");
                return;
            }
            PositionAction.showDialog(busFactory, this, PositionAction.ActionType.EDIT, positionDTO);
        });

        btnPosDelete.setOnAction(event -> {
            if (!NotificationUtil.showConfirmation("Xác nhận", "Bạn có chắc chắn muốn xóa chức vụ này?")) {
                return;
            }
            PositionDTO positionDTO = tblPositions.getSelectionModel().getSelectedItem();
            if (positionDTO == null) {
                NotificationUtil.showErrorNotification("Lỗi", "Vui lòng chọn một chức vụ để xóa.");
                return;
            }
            try {
                if (positionBUS.delete(positionDTO.getPositionId())) {
                    NotificationUtil.showSuccessNotification("Thành công", "Xóa chức vụ thành công.");
                    setupUIData();
                    DepartmentDTO selectedDepartment = tblDepartments.getSelectionModel().getSelectedItem();
                    if (selectedDepartment != null) {
                        loadPositionsForDepartment(selectedDepartment.getDepartmentId());
                    } else {
                        loadAllPositionData();
                    }
                }
            } catch (PermissionDeniedException e) {
                NotificationUtil.showErrorNotification("Quyền truy cập", e.getMessage());
            } catch (DeleteFailedException e) {
                NotificationUtil.showErrorNotification("Lỗi", e.getMessage());
            } catch (Exception e) {
                NotificationUtil.showErrorNotification("Lỗi", "Có lỗi không xác định: " + e.getMessage());
            }
        });

        txtPosSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filterPositions();
        });
    }

    private void setupTableRelationship() {
        tblDepartments.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                loadPositionsForDepartment(newSelection.getDepartmentId());
            }
        });
    }

    private void filterDepartments() {
        String searchText = txtDeptSearch.getText().toLowerCase();
        ArrayList<DepartmentDTO> filteredList = new ArrayList<>();

        for (DepartmentDTO department : departmentList) {
            if (department.getDepartmentName().toLowerCase().contains(searchText)) {
                filteredList.add(department);
            }
        }
        tblDepartments.setItems(FXCollections.observableArrayList(filteredList));
    }

    private void filterPositions() {
        String searchText = txtPosSearch.getText().toLowerCase();
        ArrayList<PositionDTO> filteredList = new ArrayList<>();

        // Get currently displayed positions (might be filtered by department already)
        ArrayList<PositionDTO> currentPositions = new ArrayList<>(tblPositions.getItems());

        for (PositionDTO position : currentPositions) {
            if (position.getPositionName().toLowerCase().contains(searchText) ||
                    String.valueOf(position.getAllowance()).contains(searchText)) {
                filteredList.add(position);
            }
        }
        tblPositions.setItems(FXCollections.observableArrayList(filteredList));
    }

    private void setupDepartmentTable() {
        colDepartmentId.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDepartmentId()));
        colDepartmentName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDepartmentName()));
    }

    private void setupPositionTable() {
        colPositionId.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getPositionId()));
        colPositionName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPositionName()));
        colAllowance.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getAllowance()));
    }

    public void loadDepartmentData() {
        tblDepartments.getItems().clear();
        tblDepartments.setItems(FXCollections.observableArrayList(departmentList));
    }

    public void loadAllPositionData() {
        tblPositions.getItems().clear();
        tblPositions.setItems(FXCollections.observableArrayList(positionList));
    }

    public void loadPositionsForDepartment(int departmentId) {
        ArrayList<PositionDTO> filteredPositions = positionList.stream()
                .filter(position -> position.getDepartmentId() == departmentId)
                .collect(Collectors.toCollection(ArrayList::new));

        tblPositions.getItems().clear();
        tblPositions.setItems(FXCollections.observableArrayList(filteredPositions));
    }

    // Method to be called after CRUD operations from Action dialogs
    public void refreshData() {
        setupUIData();
        loadDepartmentData();

        DepartmentDTO selectedDepartment = tblDepartments.getSelectionModel().getSelectedItem();
        if (selectedDepartment != null) {
            loadPositionsForDepartment(selectedDepartment.getDepartmentId());
        } else {
            loadAllPositionData();
        }
    }
}