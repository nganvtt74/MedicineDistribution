package com.example.medicinedistribution.GUI;

import com.example.medicinedistribution.BUS.BUSFactory;
import com.example.medicinedistribution.BUS.Interface.AccountBUS;
import com.example.medicinedistribution.DTO.AccountDTO;
import com.example.medicinedistribution.DTO.EmployeeDTO;
import com.example.medicinedistribution.DTO.RoleDTO;
import com.example.medicinedistribution.Exception.DeleteFailedException;
import com.example.medicinedistribution.Exception.PermissionDeniedException;
import com.example.medicinedistribution.GUI.SubAction.AccountAction;
import com.example.medicinedistribution.Util.NotificationUtil;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class AccountController {
    @FXML
    private Button btnAdd;

    @FXML
    private Button btnDelete;

    @FXML
    private Button btnEdit;

    @FXML
    private Button btnRefresh;

    @FXML
    private TableColumn<AccountDTO, Integer> colAccountId;

    @FXML
    private TableColumn<AccountDTO, String> colUsername;

    @FXML
    private TableColumn<AccountDTO, Integer> colEmployeeId;

    @FXML
    private TableColumn<AccountDTO, String> colEmployeeName;

    @FXML
    private TableColumn<AccountDTO, String> colRoleName;

    @FXML
    private TableColumn<AccountDTO, String> colEmployeeStatus;

    @FXML
    private TableView<AccountDTO> tblAccounts;

    @FXML
    private TextField txtSearch;

    private BUSFactory busFactory;
    private AccountBUS accountBUS;
    private ArrayList<AccountDTO> accountList;

    public AccountController(BUSFactory busFactory) {
        this.busFactory = busFactory;
    }

    public void initialize() {
        System.out.println("AccountController initialized");
        accountBUS = busFactory.getAccountBUS();
        setupUIData();
        setUpUI();
        setUpTable();
        loadData();
    }

    public void setupUIData() {
        if (accountList != null) {
            accountList.clear();
        }
        List<RoleDTO> roleList = busFactory.getRoleBUS().getRolesWithoutEditablePermissions();
        accountList = accountBUS.getAccountByRoleId(roleList);
        ArrayList<AccountDTO> NullAccountList = accountBUS.getAccountByNullRoleId();
        if (NullAccountList != null) {
            accountList.addAll(NullAccountList);
        }
        if (accountList == null) {
            accountList = new ArrayList<>();
        }



    }

    public void setUpUI() {
        btnAdd.setOnAction(event -> {
            AccountAction.showDialog(busFactory, this, AccountAction.ActionType.ADD);
        });

        btnEdit.setOnAction(event -> {
            AccountDTO accountDTO = tblAccounts.getSelectionModel().getSelectedItem();
            if (accountDTO == null) {
                NotificationUtil.showErrorNotification("Lỗi", "Vui lòng chọn một tài khoản để chỉnh sửa.");
                return;
            }
            AccountAction.showDialog(busFactory, this, AccountAction.ActionType.EDIT, accountDTO);
        });

        btnDelete.setOnAction(event -> {
            if (!NotificationUtil.showConfirmation("Xác nhận", "Bạn có chắc chắn muốn xóa tài khoản này?")) {
                return;
            }

            AccountDTO accountDTO = tblAccounts.getSelectionModel().getSelectedItem();
            if (accountDTO == null) {
                NotificationUtil.showErrorNotification("Lỗi", "Vui lòng chọn một tài khoản để xóa.");
                return;
            }

            try {
                if (accountBUS.delete(accountDTO.getAccountId())) {
                    NotificationUtil.showSuccessNotification("Thành công", "Xóa tài khoản thành công.");
                    setupUIData();
                    loadData();
                }
            } catch (PermissionDeniedException e) {
                NotificationUtil.showErrorNotification("Quyền truy cập", e.getMessage());
            } catch (DeleteFailedException e) {
                NotificationUtil.showErrorNotification("Lỗi", e.getMessage());
            } catch (Exception e) {
                NotificationUtil.showErrorNotification("Lỗi", "Có lỗi không xác định: " + e.getMessage());
            }
        });

        btnRefresh.setOnAction(event -> {
            setupUIData();
            loadData();
            txtSearch.clear();
        });

        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filterAccounts();
        });
    }

    private void filterAccounts() {
        String searchText = txtSearch.getText().toLowerCase();
        ArrayList<AccountDTO> filteredList = new ArrayList<>();

        for (AccountDTO account : accountList) {
            if (account.getUsername().toLowerCase().contains(searchText)){
                filteredList.add(account);
            }
        }
        tblAccounts.setItems(FXCollections.observableArrayList(filteredList));
    }

    public void setUpTable() {
        colAccountId.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getAccountId()));
        colUsername.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUsername()));
        colEmployeeId.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getEmployeeId()));

        // For employee name, you might need to fetch it from employee data
        colEmployeeName.setCellValueFactory(cellData -> {
            Integer employeeId = cellData.getValue().getEmployeeId();
            EmployeeDTO employee = busFactory.getEmployeeBUS().findById(employeeId);
            String employeeName = employee != null ?
                    employee.getFullName() : "N/A";
            return new SimpleStringProperty(employeeName);
        });

        // For role name, fetch from role data
        colRoleName.setCellValueFactory(cellData -> {
            if ((cellData.getValue().getRoleId() != null) && (cellData.getValue().getRoleId() != 0)) {
                Integer roleId = cellData.getValue().getRoleId();
                String roleName = busFactory.getRoleBUS().findById(roleId).getRoleName();
                return new SimpleStringProperty(roleName);
            }else {
                return new SimpleStringProperty("Chưa phân quyền");
            }
        });

        colEmployeeStatus.setCellValueFactory(cellData -> {
            Integer employeeId = cellData.getValue().getEmployeeId();
            EmployeeDTO employee = busFactory.getEmployeeBUS().findById(employeeId);
            int employeeStatus = employee != null ?
                    employee.getStatus() : 0;
            return new SimpleStringProperty(employeeStatus == 1 ? "Đang hoạt động" : "Ngừng hoạt động");

        });
        colEmployeeStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Đang hoạt động".equals(item)) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    }
                }
            }
        });
    }

    public void loadData() {
        tblAccounts.getItems().clear();
        tblAccounts.setItems(FXCollections.observableArrayList(accountList));
    }

    public void loadAccounts() {
        setupUIData();
        loadData();
    }
}