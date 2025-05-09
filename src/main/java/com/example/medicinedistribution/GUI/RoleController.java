package com.example.medicinedistribution.GUI;

import com.example.medicinedistribution.BUS.BUSFactory;
import com.example.medicinedistribution.DTO.PermissionDTO;
import com.example.medicinedistribution.DTO.RoleDTO;
import com.example.medicinedistribution.Exception.AlreadyExistsException;
import com.example.medicinedistribution.Exception.InsertFailedException;
import com.example.medicinedistribution.Util.GenericTablePrinter;
import com.example.medicinedistribution.Util.NotificationUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.Parent;
import lombok.extern.slf4j.Slf4j;

import javax.management.relation.Role;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class RoleController {

    @FXML
    private Button btnAddRole;

    @FXML
    private Button btnDeleteRole;

    @FXML
    private Button btnSavePermissions;

    @FXML
    private Button btnSearch;

    @FXML
    private TextField txtSelectedRole;

    @FXML
    private TableColumn<RoleDTO,String> colRoleName;

    @FXML
    private TableColumn<RoleDTO,String> colRoleStatus;


    @FXML
    private TableView<RoleDTO> tblRole;

    @FXML
    private Button btnStatus;

    @FXML
    private Button btnRefresh;

    @FXML
    private Pane permissionsContainer;
    private PermissionViewController currentPermissionController;

    @FXML
    private TextField txtSearch;

    private BUSFactory busFactory;

    private RoleDTO newRole;

    private List<RoleDTO> roles;

    public RoleController(BUSFactory busFactory) {
        this.busFactory = busFactory;
    }

    @FXML
    public void initialize() {
        setupTable();
        // Initialize the controller
        setup();
    }

    private void setupTable() {
        colRoleName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRoleName()));
        colRoleStatus.setCellValueFactory(cellData -> {
            if (cellData.getValue().getStatus() == 1) {
                return new SimpleStringProperty("Active");
            } else {
                return new SimpleStringProperty("Inactive");
            }
        });

        colRoleStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Active".equals(item)) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    }
                }
            }
        });

        tblRole.setPlaceholder(new Label("No roles available"));
        tblRole.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }


    private void setup() {
        txtSelectedRole.setEditable(false);
        txtSelectedRole.setDisable(true);
        // Set up the UI components and event handlers
        btnAddRole.setOnAction(event -> addRole());
        btnDeleteRole.setOnAction(event -> deleteRole());
        btnSavePermissions.setOnAction(event -> savePermissions());
        btnRefresh.setOnAction(event -> {
            loadRoles();
            txtSelectedRole.setText("");
            permissionsContainer.getChildren().clear();
            txtSelectedRole.setDisable(true);
            txtSelectedRole.setEditable(false);
            btnStatus.setDisable(true);
            btnStatus.setText("Khoá");
            btnStatus.setStyle("-fx-background-color: #e74c3c;");
            tblRole.getSelectionModel().clearSelection();
            txtSearch.clear();
            txtSearch.requestFocus();

        });
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            String searchText = newValue.toLowerCase();
            tblRole.getItems().clear();
            for (RoleDTO role : roles) {
                if (role.getRoleName().toLowerCase().contains(searchText)) {
                    tblRole.getItems().add(role);
                }
            }
        });
        // Load roles into the list view
        loadRoles();
    }

    private void deleteRole() {
        // Delete the selected role
        if (NotificationUtil.showConfirmation("Xoá vai trò", "Bạn có chắc chắn muốn xoá vai trò này?")) {
            RoleDTO selectedRole = tblRole.getSelectionModel().getSelectedItem();
            if (selectedRole != null) {
                boolean success = busFactory.getRoleBUS().delete(selectedRole.getRoleId());
                if (success) {
                    log.info("Role deleted successfully: {}", selectedRole.getRoleName());
                    NotificationUtil.showSuccessNotification("Thành công", "Xoá vai trò thành công");
                    loadRoles();
                } else {
                    log.error("Failed to delete role: {}", selectedRole.getRoleName());
                    NotificationUtil.showErrorNotification("Thất bại", "Xoá vai trò thất bại");
                }
            } else {
                NotificationUtil.showErrorNotification("Lỗi", "Vui lòng chọn một vai trò để xoá");
            }
        }
    }

    private void addRole() {
        // Create a new role and add it to the list view
        newRole = new RoleDTO();
        newRole.setRoleName("New Role");
        newRole.setPermissions(new ArrayList<>());
        log.info("New role created: {}", newRole.getRoleName());
        txtSelectedRole.setText(newRole.getRoleName());
        tblRole.getSelectionModel().clearSelection();
        loadNewPermissions();
    }

    private void loadRoles() {
        btnStatus.setDisable(true);
        // Load roles from the database and display them in the list view
        tblRole.getItems().clear();

        if (roles!=null){
            roles.clear();
        }
        roles = busFactory.getRoleBUS().getRolesWithoutEditablePermissions();
        for (RoleDTO role : roles) {
            tblRole.getItems().add(role);
        }

        tblRole.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                newRole = null;
                txtSelectedRole.setText(newValue.getRoleName());
                loadPermissions(newValue);
                btnStatus.setDisable(false);
                btnStatus.setText(newValue.getStatus() == 1 ? "Khoá" : "Mở khoá");
                btnStatus.setOnAction(event -> {
                    if (newValue.getStatus() == 1) {
                        newValue.setStatus(0);
                        btnStatus.setText("Mở khoá");
                    } else {
                        newValue.setStatus(1);
                        btnStatus.setText("Khoá");
                    }
                    log.info("Role status changed: {}", newValue);
                    boolean success = busFactory.getRoleBUS().update(newValue);
                    if (success) {
                        log.info("Role status updated successfully: {}", newValue.getRoleName());
                        NotificationUtil.showSuccessNotification("Thành công", "Cập nhật trạng thái vai trò thành công");
                        loadRoles();
                    } else {
                        log.error("Failed to update role status: {}", newValue.getRoleName());
                        NotificationUtil.showErrorNotification("Thất bại", "Cập nhật trạng thái vai trò thất bại");
                    }
                });
                btnStatus.setStyle(newValue.getStatus() == 1 ? "-fx-background-color: #e74c3c;" : "-fx-background-color: green;");

            }
        });
    }

    private void loadNewPermissions() {
        if (newRole != null) {
            txtSelectedRole.setDisable(false);
            txtSelectedRole.setEditable(true);

            permissionsContainer.getChildren().clear();
            List<PermissionDTO> permissions = busFactory.getRoleBUS().getNewPermissionsForEdit();

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("PermissionModule.fxml"));
                PermissionViewController permissionModuleController = new PermissionViewController(permissions);// Show checkbox for all except VIEW)
                loader.setController(permissionModuleController);
                Parent moduleSection = loader.load();

                currentPermissionController = permissionModuleController;
                permissionsContainer.getChildren().add(moduleSection);

            } catch (IOException e) {
                log.error("Error loading PermissionModule.fxml: ", e);
            }
        }
    }

    private void loadPermissions(RoleDTO newValue) {
        permissionsContainer.getChildren().clear();
        txtSelectedRole.setDisable(false);
        txtSelectedRole.setEditable(true);
        RoleDTO roleForEdit = busFactory.getRoleBUS().getRoleForEdit(newValue.getRoleId());
        List<PermissionDTO> permissions = roleForEdit.getPermissions();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("PermissionModule.fxml"));
            PermissionViewController permissionModuleController = new PermissionViewController(permissions);// Show checkbox for all except VIEW)
            loader.setController(permissionModuleController);
            Parent moduleSection = loader.load();

            currentPermissionController = permissionModuleController;
            permissionsContainer.getChildren().add(moduleSection);

        } catch (IOException e) {
            log.error("Error loading PermissionModule.fxml: ", e);
        }


    }
    private void savePermissions() {
        if (currentPermissionController != null && tblRole.getSelectionModel().getSelectedItem() != null) {
                RoleDTO selectedRole = tblRole.getSelectionModel().getSelectedItem();
                selectedRole.setRoleName(txtSelectedRole.getText());
                System.out.println("Selected role: " + selectedRole.getRoleName());

                List<PermissionDTO> updatedPermissions = currentPermissionController.getUpdatedPermissions();

                if (updatedPermissions != null) {


                    List<PermissionDTO> newPermissions = new ArrayList<>();
                    for (PermissionDTO permission : updatedPermissions) {
                        if (permission.isChecked()) {
                            newPermissions.add(permission);
                        }
                    }
                    GenericTablePrinter.printTable(newPermissions);

                    // Update the role permissions in the database
                    selectedRole.setPermissions(newPermissions);
                boolean success = busFactory.getRoleBUS().update(selectedRole);
                if (success) {
                    // Show success message or notification
                    log.info("Permissions updated successfully for role: {}", selectedRole.getRoleName());
                    NotificationUtil.showSuccessNotification("Thành công", "Cập nhật vai trò thành công");
                    loadRoles();
                    // You might want to refresh the view or show a success message
                } else {
                    // Show error message
                    log.error("Failed to update permissions for role: {}", selectedRole.getRoleName());
                    NotificationUtil.showErrorNotification("Thất bại", "Cập nhật vai trò thất bại");
                    // You might want to show an error message
                }
                }
        } else if (currentPermissionController != null && newRole != null) {
            List<PermissionDTO> updatedPermissions = currentPermissionController.getUpdatedPermissions();

            if (updatedPermissions != null) {
                List<PermissionDTO> newPermissions = new ArrayList<>();
                for (PermissionDTO permission : updatedPermissions) {
                    if (permission.isChecked()) {
                        newPermissions.add(permission);
                    }
                }
                GenericTablePrinter.printTable(newPermissions);

                // Update the role permissions in the database
                newRole.setPermissions(newPermissions);
                newRole.setRoleName(txtSelectedRole.getText());
                boolean success;
                try {
                    success = busFactory.getRoleBUS().insert(newRole);
                    if (success) {
                        // Show success message or notification
                        log.info("New role created successfully: {}", newRole.getRoleName());
                        NotificationUtil.showSuccessNotification("Thành công", "Tạo vai trò thành công");
                        loadRoles();
                        // You might want to refresh the view or show a success message
                    }
                }catch (InsertFailedException e){
                    log.error("Failed to create new role: {}", newRole.getRoleName());
                    NotificationUtil.showErrorNotification("Thất bại", e.getMessage());
                }catch (AlreadyExistsException e){
                    log.error("Role already exists: {}", newRole.getRoleName());
                    NotificationUtil.showErrorNotification("Thất bại", e.getMessage());
                }catch (Exception e){
                    log.error("Failed to create new role: {}", newRole.getRoleName());
                    NotificationUtil.showErrorNotification("Thất bại", "Tạo vai trò thất bại");
                }
            }

        }
    }


}
