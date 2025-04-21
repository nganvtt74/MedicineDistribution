package com.example.medicinedistribution.GUI.SubAction;

import com.example.medicinedistribution.BUS.BUSFactory;
import com.example.medicinedistribution.DTO.AccountDTO;
import com.example.medicinedistribution.DTO.EmployeeDTO;
import com.example.medicinedistribution.DTO.RoleDTO;
import com.example.medicinedistribution.GUI.AccountController;
import com.example.medicinedistribution.GUI.SubSelect.EmployeeSelectController;
import com.example.medicinedistribution.GUI.SubSelect.SelectionHandler;
import com.example.medicinedistribution.Util.NotificationUtil;
import com.example.medicinedistribution.Util.PasswordUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class AccountAction extends SubAction<AccountController, AccountDTO> {
    private HashMap<String, TextField> formFields = new HashMap<>();
    private ComboBox<RoleDTO> roleComboBox = new ComboBox<>();
    private ActionType actionType;
    private List<RoleDTO> roles;
    private TextField employeeTextField;
    private EmployeeDTO selectedEmployee;

    @FXML
    private Label lblHeader;

    @FXML
    private VBox formContent;

    @FXML
    private Button btnCancel;

    public AccountAction(BUSFactory busFactory, AccountController parentController, ActionType actionType, AccountDTO selectedData) {
        super(busFactory, parentController, selectedData);
        this.actionType = actionType;
    }

    @FXML
    public void initialize() {
        // Set up the header label
        lblHeader.setText(actionType == ActionType.ADD ? "Thêm tài khoản" : "Cập nhật tài khoản");
        btnCancel.setOnAction(e -> closeDialog());
        btnSubmit.setOnAction(e -> handleSubmit());

        // Create form fields
        createFormFields();
    }



    @Override
    protected void createFormFields() {
        // Initialize data
        roles = busFactory.getRoleBUS().getRolesWithoutEditablePermissions();

        // Employee selection field
        if (actionType == ActionType.ADD) {
            addEmployeeSelectionField(null);
        } else {
            Integer employeeId = selectedData.getEmployeeId();
            selectedEmployee = busFactory.getEmployeeBUS().findById(employeeId);
            addEmployeeSelectionField(selectedEmployee);

            // Username field
            addFormField("username", "Tên đăng nhập:", selectedData.getUsername(), "Nhập tên đăng nhập" , false);
        }
        // Role combobox
        addRoleComboBox(selectedData != null ? selectedData.getRoleId() : null);
        // Password reset button
        if (actionType == ActionType.EDIT) {
            addPasswordResetButton();
        }
    }
private void addPasswordResetButton() {
    HBox fieldContainer = new HBox(10);
    fieldContainer.setAlignment(Pos.CENTER_LEFT);
    fieldContainer.setPadding(new Insets(5, 0, 5, 0));

    Label label = new Label("Mật khẩu:");
    label.setPrefWidth(150);
    label.setFont(new Font(14));

    Label passwordStatus = new Label("Đã được cài đặt");
    passwordStatus.setFont(new Font(14));
    passwordStatus.getStyleClass().add("text-muted");

    Button resetButton = new Button("Cài đặt lại mật khẩu");
    resetButton.getStyleClass().addAll("warning-button");
    resetButton.setOnAction(e -> handlePasswordReset());
    resetButton.setMaxWidth(Double.MAX_VALUE); // This makes the button expand to fill available width

    VBox statusAndButtonBox = new VBox(5);
    statusAndButtonBox.getChildren().addAll(passwordStatus, resetButton);
    HBox.setHgrow(statusAndButtonBox, Priority.ALWAYS); // Set HGrow on the container

    fieldContainer.getChildren().addAll(label, statusAndButtonBox);
    formContent.getChildren().add(fieldContainer);
}

    private void handlePasswordReset() {
            if (NotificationUtil.showConfirmation("Cài đặt lại mật khẩu", "Bạn có chắc chắn muốn cài đặt lại mật khẩu cho tài khoản này?")) {
                try {
                    // Get employee details to generate default password
                    EmployeeDTO employee = busFactory.getEmployeeBUS().findById(selectedData.getEmployeeId());
                    if (employee != null) {
                        String newPassword = PasswordUtil.generateDefaultPassword(employee);
                        if (newPassword != null) {
                            AccountDTO updatedAccount = new AccountDTO();
                            updatedAccount.setAccountId(selectedData.getAccountId());
                            updatedAccount.setUsername(selectedData.getUsername());
                            updatedAccount.setPassword(newPassword);
                            updatedAccount.setEmployeeId(selectedData.getEmployeeId());
                            updatedAccount.setRoleId(selectedData.getRoleId());

                            busFactory.getAccountBUS().resetPassword(updatedAccount);
                            NotificationUtil.showSuccessNotification("Thành công", "Đã cài đặt lại mật khẩu thành công");
                        } else {
                            NotificationUtil.showErrorNotification("Lỗi", "Không thể tạo mật khẩu mặc định");
                        }
                    } else {
                        NotificationUtil.showErrorNotification("Lỗi", "Không tìm thấy thông tin nhân viên");
                    }
                } catch (Exception ex) {
                    NotificationUtil.showErrorNotification("Lỗi", "Không thể cài đặt lại mật khẩu: " + ex.getMessage());
                }
            }
    }
    private void addEmployeeSelectionField(EmployeeDTO defaultEmployee) {
        HBox fieldContainer = new HBox(10);
        fieldContainer.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label("Nhân viên:");
        label.setPrefWidth(150);
        label.setMinWidth(150); // Thêm minWidth để đảm bảo label không bị co lại
        label.setFont(new Font(14));

        employeeTextField = new TextField();
        employeeTextField.setFont(new Font(14));
        employeeTextField.setPromptText("Chọn nhân viên...");
        employeeTextField.setEditable(false);
        employeeTextField.setDisable(true);

        Button browseButton = new Button("...");
        browseButton.setFont(new Font(15));
        browseButton.getStyleClass().add("primary-button");
        browseButton.setOnAction(e -> openEmployeeSelector());
        browseButton.setPrefWidth(30);
        browseButton.setMinWidth(30);
        browseButton.setMaxWidth(30);

        HBox textButtonContainer = new HBox(5);
        textButtonContainer.setAlignment(Pos.CENTER_LEFT);
        if (actionType == ActionType.EDIT){
            log.info("Default employee: {}", defaultEmployee);
            if (defaultEmployee != null) {
                employeeTextField.setText(defaultEmployee.getEmployeeId() + " - " +
                        defaultEmployee.getFullName());
                employeeTextField.setDisable(true);
                employeeTextField.setEditable(false);
                textButtonContainer.getChildren().add(employeeTextField);
            }
            else {
                employeeTextField.setPromptText("Chọn nhân viên...");
                textButtonContainer.getChildren().addAll(employeeTextField, browseButton);
            }
        } else {
            employeeTextField.setPromptText("Chọn nhân viên...");
            textButtonContainer.getChildren().addAll(employeeTextField, browseButton);

        }



        // Thiết lập để employeeTextField phình ra trong textButtonContainer
        HBox.setHgrow(employeeTextField, Priority.ALWAYS);

        // Thiết lập để textButtonContainer phình ra trong fieldContainer
        HBox.setHgrow(textButtonContainer, Priority.ALWAYS);

        fieldContainer.getChildren().addAll(label, textButtonContainer);
        formContent.getChildren().add(fieldContainer);

        if (actionType == ActionType.ADD) {
            addFormField("username", "Tên đăng nhập:", "", "Nhập tên đăng nhập" , true);
        }
    }

    private void openEmployeeSelector() {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../SubSelect.fxml"));
        EmployeeSelectController controller = new EmployeeSelectController(busFactory);
        loader.setController(controller);

        Parent root = loader.load();

        // Set the selection handler
        controller.setSelectionHandler(new SelectionHandler<EmployeeDTO>() {
            @Override
            public void onItemSelected(EmployeeDTO employee) {
                selectedEmployee = employee;
                employeeTextField.setText(employee.getEmployeeId() + " - " +
                        employee.getFullName());
            }
        });

        Stage stage = new Stage();
        stage.setTitle("Chọn nhân viên");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(root));
        stage.showAndWait();

    } catch (IOException e) {
        log.error("Error loading EmployeeSelect dialog: ", e);
    }
}

    private void addRoleComboBox(Integer defaultRoleId) {
        HBox fieldContainer = new HBox(10);
        fieldContainer.setAlignment(Pos.CENTER_LEFT);
        Label label = new Label("Vai trò:");
        label.setPrefWidth(150);
        label.setFont(new Font(14));

        // Configure role combo box
        HBox.setHgrow(roleComboBox, Priority.ALWAYS);
        roleComboBox.setPromptText("Chọn vai trò");

        // Setup converter for display
        roleComboBox.setMaxWidth(Double.MAX_VALUE);
        roleComboBox.setConverter(new StringConverter<RoleDTO>() {
            @Override
            public String toString(RoleDTO role) {
                return role == null ? "" : role.getRoleId() + " - " + role.getRoleName();
            }

            @Override
            public RoleDTO fromString(String string) {
                return null; // Not needed for ComboBox
            }
        });

        // Add items to combo box
        if (roles != null && !roles.isEmpty()) {
            roleComboBox.setItems(FXCollections.observableArrayList(roles));

            // Set default value if available
            if (defaultRoleId != null) {
                RoleDTO defaultRole = roles.stream()
                        .filter(role -> role.getRoleId().equals(defaultRoleId))
                        .findFirst()
                        .orElse(null);
                roleComboBox.setValue(defaultRole);
            }
        }

        fieldContainer.getChildren().addAll(label, roleComboBox);
        formContent.getChildren().add(fieldContainer);
    }

    private void addFormField(String fieldName, String labelText, String defaultValue, String placeholder,boolean isEditable) {
        HBox fieldContainer = new HBox(10);
        fieldContainer.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label(labelText);
        label.setPrefWidth(150);
        label.setFont(new Font(14));

        TextField textField = new TextField(defaultValue);
        textField.setFont(new Font(14));
        textField.setPromptText(placeholder);
        HBox.setHgrow(textField, Priority.ALWAYS);
        textField.setEditable(isEditable);
        textField.setDisable(!isEditable);

        fieldContainer.getChildren().addAll(label, textField);
        formContent.getChildren().add(fieldContainer);

        formFields.put(fieldName, textField);
    }

    @Override
    protected boolean validateForm() {
        // Validate employee selection
        if (selectedEmployee == null) {
            NotificationUtil.showErrorNotification("Lỗi", "Vui lòng chọn nhân viên");
            return false;
        }

        // Basic validation
        for (Map.Entry<String, TextField> entry : formFields.entrySet()) {
            if (entry.getValue().getText().trim().isEmpty()) {
                NotificationUtil.showErrorNotification("Lỗi", "Vui lòng nhập " + entry.getKey());
                entry.getValue().requestFocus();
                return false;
            }
        }

        // Validate role selection
        if (roleComboBox.getValue() == null) {
            NotificationUtil.showErrorNotification("Lỗi", "Vui lòng chọn vai trò");
            roleComboBox.requestFocus();
            return false;
        }

        return true;
    }

    @Override
    protected void handleSubmit() {
        if (validateForm()) {
            try {
                if (actionType == ActionType.ADD) {
                    addAccount();
                } else {
                    updateAccount();
                }

                // Close the window
                ((Stage) btnSubmit.getScene().getWindow()).close();

                // Refresh parent view
                parentController.setupUIData();
                parentController.loadAccounts();
            } catch (Exception e) {
                NotificationUtil.showErrorNotification("Lỗi", "Có lỗi xảy ra: " + e.getMessage());
            }
        }
    }

    private void addAccount() {
        AccountDTO newAccount = createAccountFromForm();
        busFactory.getAccountBUS().insert(newAccount);
    }

    private void updateAccount() {
        AccountDTO updatedAccount = createAccountFromForm();
        updatedAccount.setAccountId(selectedData.getAccountId());
        busFactory.getAccountBUS().update(updatedAccount);
    }

    private AccountDTO createAccountFromForm() {
        AccountDTO account = new AccountDTO();
        account.setUsername(formFields.get("username").getText());
        account.setPassword(PasswordUtil.generateDefaultPassword(selectedEmployee));

        if (selectedEmployee != null) {
            account.setEmployeeId(selectedEmployee.getEmployeeId());
        }

        RoleDTO selectedRole = roleComboBox.getValue();
        if (selectedRole != null) {
            account.setRoleId(selectedRole.getRoleId());
        }

        return account;
    }

    // Static helper methods for showing AccountAction dialogs
    public static void showDialog(BUSFactory busFactory, AccountController parentController, ActionType actionType) {
        showDialog(busFactory, parentController, actionType, null);
    }

    public static void showDialog(BUSFactory busFactory, AccountController parentController, ActionType actionType, AccountDTO selectedItem) {
        AccountAction controller = new AccountAction(busFactory, parentController, actionType, selectedItem);
        SubAction.showDialog(controller, actionType == ActionType.ADD ? "Thêm tài khoản" : "Cập nhật tài khoản");
    }
}