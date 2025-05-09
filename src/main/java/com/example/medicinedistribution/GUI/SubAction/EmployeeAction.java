package com.example.medicinedistribution.GUI.SubAction;

import com.example.medicinedistribution.BUS.BUSFactory;
import com.example.medicinedistribution.BUS.Interface.DepartmentBUS;
import com.example.medicinedistribution.BUS.Interface.EmployeeBUS;
import com.example.medicinedistribution.BUS.Interface.PositionBUS;
import com.example.medicinedistribution.DTO.DepartmentDTO;
import com.example.medicinedistribution.DTO.EmployeeDTO;
import com.example.medicinedistribution.DTO.PositionDTO;
import com.example.medicinedistribution.GUI.Component.CurrencyTextField;
import com.example.medicinedistribution.GUI.EmployeeController;
import com.example.medicinedistribution.GUI.EmployeeHistoryController;
import com.example.medicinedistribution.Util.NotificationUtil;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.controlsfx.control.SearchableComboBox;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class EmployeeAction extends SubAction<EmployeeController, EmployeeDTO> {
    // Form fields
    private TextField txtFirstName;
    private TextField txtLastName;
    @FXML
    private Label lblHeader;
    private ComboBox<String> cboGender;
    private DatePicker dtpBirthday;
    private TextField txtPhone;
    private TextField txtEmail;
    private TextArea txtAddress;
    private SearchableComboBox<PositionWrapper> cboPosition;
    private DatePicker dtpHireDate;
    private TextField txtBasicSalary;
    private ComboBox<String> cboStatus;


    // Business logic objects
    private EmployeeBUS employeeBUS;
    private PositionBUS positionBUS;
    private DepartmentBUS departmentBUS;
    private ActionType actionType;




    public EmployeeAction(BUSFactory busFactory, EmployeeController controller, ActionType actionType, EmployeeDTO selectedData) {
        super(busFactory, controller);
        // Initialize BUS objects
        this.employeeBUS = busFactory.getEmployeeBUS();
        this.positionBUS = busFactory.getPositionBUS();
        this.departmentBUS = busFactory.getDepartmentBUS();
        this.actionType = actionType;
        this.selectedData = selectedData;


    }

    public void initialize() {
        // Create the form
        createFormFields();

        // Load data if editing
        if (actionType == ActionType.EDIT && this.selectedData != null) {
            // Set the header text based on action type
            loadEmployeeData();
            lblHeader.setText("Cập nhật nhân viên");
        } else {
            // Default values for new employee
            lblHeader.setText("Thêm nhân viên");
            dtpBirthday.setValue(LocalDate.now().minusYears(20));
            dtpHireDate.setValue(LocalDate.now());
            cboGender.getSelectionModel().selectFirst();
            cboStatus.getSelectionModel().selectFirst();
        }
        btnSubmit.setText(actionType == ActionType.ADD ? "Thêm" : "Cập nhật");
        scrollPane.setMaxHeight(400);
        formContent.setPadding(new Insets(0,20,0,20));

        Platform.runLater(() -> {
            // Set the initial focus to the first field
            btnCancel.setOnAction(event -> closeDialog());
            validProperty();
        });
    }

private void validProperty() {
    dtpBirthday.setEditable(false);
    dtpHireDate.setEditable(false);

    // Remove the automatic formatting that conflicts with the validation regex
    txtPhone.textProperty().addListener((observable, oldValue, newValue) -> {
        if (newValue == null) return;

        // Only allow valid characters for the phone regex pattern
        String cleaned = newValue.replaceAll("[^0-9+\\- ]", "");

        // Don't update if nothing changed (to avoid infinite recursion)
        if (!cleaned.equals(newValue)) {
            txtPhone.setText(cleaned);
        }

        // Check if valid against the pattern on every change
        boolean isValid = cleaned.matches("^(?:(?:0|\\+84)\\d{9}|\\+\\d{1,3}(?:[ \\-]\\d{1,4}){2,4})$");

        // Visual feedback for validity (optional)
        if (!cleaned.isEmpty()) {
            txtPhone.setStyle(isValid ? "-fx-border-color: green;" : "-fx-border-color: red;");
        } else {
            txtPhone.setStyle("");
        }
    });
}
    @Override
    protected void createFormFields() {
        // Create containers for sections
        VBox personalInfoSection = createSectionContainer("Thông tin cá nhân");
        VBox employmentInfoSection = createSectionContainer("Thông tin công việc");

        // Create form components
        createPersonalInfoForm(personalInfoSection);
        createEmploymentInfoForm(employmentInfoSection);
        // Add sections to the form
        formContent.getChildren().addAll(personalInfoSection, employmentInfoSection);
        buttonBar.getChildren().remove(btnSubmit);
    }

    @Override
    protected boolean validateForm() {
        return false;
    }

    private VBox createSectionContainer(String title) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("section-title");

        Separator separator = new Separator();

        VBox section = new VBox(5, titleLabel, separator);
        section.getStyleClass().add("form-section");
        return section;
    }

    private void createPersonalInfoForm(VBox container) {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(0, 0, 15, 0));

        int row = 0;

        // Form fields remain the same
        // First Name (Họ đệm)
        txtFirstName = new TextField();
        txtFirstName.setPromptText("Họ đệm");
        addFormField(grid, "Họ đệm:", txtFirstName, row++);

        // Last Name (Tên)
        txtLastName = new TextField();
        txtLastName.setPromptText("Tên");
        addFormField(grid, "Tên:", txtLastName, row++);

        // Gender
        cboGender = new ComboBox<>();
        cboGender.setItems(FXCollections.observableArrayList("Nam", "Nữ", "Khác"));
        cboGender.setMaxWidth(Double.MAX_VALUE);
        addFormField(grid, "Giới tính:", cboGender, row++);

        // Birthday
        dtpBirthday = new DatePicker();
        dtpBirthday.setMaxWidth(Double.MAX_VALUE);
        addFormField(grid, "Ngày sinh:", dtpBirthday, row++);

        // Phone
        txtPhone = new TextField();
        txtPhone.setPromptText("Số điện thoại");
        addFormField(grid, "Số điện thoại:", txtPhone, row++);

        // Email
        txtEmail = new TextField();
        txtEmail.setPromptText("Email");
        addFormField(grid, "Email:", txtEmail, row++);

        // Address
        txtAddress = new TextArea();
        txtAddress.setPromptText("Địa chỉ");
        txtAddress.setPrefRowCount(3);
        txtAddress.setWrapText(true);
        addFormField(grid, "Địa chỉ:", txtAddress, row++);

        // Add a submit button for personal info
        Button btnSubmitPersonalInfo = new Button(actionType == ActionType.ADD ? "Lưu thông tin cá nhân" : "Cập nhật thông tin cá nhân");
        btnSubmitPersonalInfo.getStyleClass().add("primary-button");
        btnSubmitPersonalInfo.setStyle("-fx-font-size: 14;");
        btnSubmitPersonalInfo.setMaxWidth(Double.MAX_VALUE);
        btnSubmitPersonalInfo.setOnAction(e -> handleSubmitPersonalInfo());

        HBox buttonBox = new HBox(10, btnSubmitPersonalInfo);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 10, 0));

        container.getChildren().addAll(grid, buttonBox);
    }

    private void createEmploymentInfoForm(VBox container) {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(0, 0, 15, 0));

        int row = 0;

        // Form fields remain the same
        // Position
        cboPosition = new SearchableComboBox<>();
        cboPosition.getStyleClass().add("searchable-combo-box");
        cboPosition.setMaxWidth(Double.MAX_VALUE);
        cboPosition.setPromptText("Chọn chức vụ");
        cboPosition.setButtonCell(new ListCell<PositionWrapper>() {
            @Override
            protected void updateItem(PositionWrapper item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Chọn chức vụ");
                } else {
                    setText(item.toString());
                }
            }
        });
    cboPosition.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
        log.info("ComboBox Selection Change:");
        log.info("Observable: {}", observable);
        log.info("Old Value: {}", oldValue != null ? oldValue.toString() : "null");
        log.info("New Value: {}", newValue != null ? newValue.toString() : "null");

        // Skip if no new value
        if (newValue == null) {
            log.info("Skipping: New value is null");
            return;
        }

        // Skip if old value was null (initial load)
        if (oldValue == null) {
            log.info("Initial selection - not clearing salary field");
            return;
        }

        // Only process when actually selecting a different position by ID
        if (newValue.getPosition().getPositionId() == oldValue.getPosition().getPositionId()) {
            log.info("Skipping: Same position ID selected");
            return;
        }

        log.info("Position changed - clearing salary field");
        // Clear salary field only when position changes
        String oldSalary = txtBasicSalary.getText();
        if (!oldSalary.isEmpty()) {
            txtBasicSalary.setPromptText(oldSalary);
            txtBasicSalary.setText("");
            log.info("Old salary '{}' moved to prompt text", oldSalary);
        }
    });




        loadPositions();

        HBox positionContainer = new HBox(5, cboPosition);
        positionContainer.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(cboPosition, javafx.scene.layout.Priority.ALWAYS);

        Button btnRefreshPositions = new Button("🔃");
        btnRefreshPositions.getStyleClass().add("primary-button");
        btnRefreshPositions.setStyle("-fx-font-size: 14;");
        btnRefreshPositions.setMinWidth(32);
        btnRefreshPositions.setMaxWidth(32);
        btnRefreshPositions.setMaxHeight(32);
        btnRefreshPositions.setMinHeight(32);

        btnRefreshPositions.setTooltip(new Tooltip("Làm mới danh sách"));
        btnRefreshPositions.setOnAction(event -> loadPositions());
        positionContainer.getChildren().add(btnRefreshPositions);

        addFormField(grid, "Chức vụ:", positionContainer, row++);

        // Hire Date
        dtpHireDate = new DatePicker();
        dtpHireDate.setMaxWidth(Double.MAX_VALUE);
        addFormField(grid, "Ngày thuê:", dtpHireDate, row++);

        // Basic Salary
        txtBasicSalary = new CurrencyTextField();
        txtBasicSalary.setPromptText("Lương cơ bản (VND)");
        addFormField(grid, "Lương cơ bản:", txtBasicSalary, row++);

        // Status
        cboStatus = new ComboBox<>();
        cboStatus.setItems(FXCollections.observableArrayList("Đang làm việc", "Đã nghỉ việc"));
        cboStatus.setMaxWidth(Double.MAX_VALUE);
        addFormField(grid, "Trạng thái:", cboStatus, row);

        // Add buttons for employment info
        Button btnSubmitEmploymentInfo = new Button(actionType == ActionType.ADD ? "Lưu thông tin công việc" : "Cập nhật thông tin công việc");
        btnSubmitEmploymentInfo.getStyleClass().add("primary-button");
        btnSubmitEmploymentInfo.setStyle("-fx-font-size: 14;");

        Button btnViewHistory = new Button("Xem lịch sử chỉnh sửa");
        btnViewHistory.getStyleClass().addAll("secondary-button");
        btnViewHistory.setStyle("-fx-font-size: 14;");

        // Disable history button for new employees
        btnViewHistory.setDisable(actionType == ActionType.ADD || selectedData == null);

        btnSubmitEmploymentInfo.setOnAction(e -> handleSubmitEmploymentInfo());
        btnViewHistory.setOnAction(e -> handleViewHistory());

        HBox buttonBox = new HBox(10, btnViewHistory, btnSubmitEmploymentInfo);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 10, 0));

        container.getChildren().addAll(grid, buttonBox);
    }

    // Handle personal info submission
    private void handleSubmitPersonalInfo() {
        try {
            if (!validatePersonalInfo()) {
                return;
            }

            EmployeeDTO employee = extractPersonalInfoFromForm();
            boolean result;

            if (actionType == ActionType.ADD) {
                // For new employee, we'll save basic personal info first
                if (selectedData == null) {
                    // Create a new employee with only personal info
                    //TODO : Check if email already exists
                    result = employeeBUS.insertPersonalInfo(employee);
                    if (result) {
                        NotificationUtil.showSuccessNotification("Thành công", "Lưu thông tin cá nhân thành công");
                        // Get the created employee ID
                        selectedData = employeeBUS.findById(employee.getEmployeeId());
                        actionType = ActionType.EDIT; // Switch to edit mode
                    }
                } else {
                    result = employeeBUS.updatePersonalInfo(employee);
                    if (result) {
                        NotificationUtil.showSuccessNotification("Thành công", "Cập nhật thông tin cá nhân thành công");
                    }
                }
            } else {
                employee.setEmployeeId(selectedData.getEmployeeId());
                result = employeeBUS.updatePersonalInfo(employee);
                if (result) {
                    NotificationUtil.showSuccessNotification("Thành công", "Cập nhật thông tin cá nhân thành công");
                    parentController.refreshData();
                }
            }
        } catch (IllegalArgumentException e) {
            NotificationUtil.showErrorNotification("Lỗi dữ liệu", e.getMessage());
        } catch (Exception e) {
            NotificationUtil.showErrorNotification("Lỗi", "Không thể lưu thông tin cá nhân: " + e.getMessage());
        }
    }

    // Handle employment info submission
    private void handleSubmitEmploymentInfo() {
        try {
            if (!validateEmploymentInfo()) {
                return;
            }

            EmployeeDTO employee = extractEmploymentInfoFromForm();
            boolean result;

            if (actionType == ActionType.ADD || selectedData == null) {
                NotificationUtil.showErrorNotification("Lỗi", "Vui lòng lưu thông tin cá nhân trước");
                return;
            } else {
                employee.setEmployeeId(selectedData.getEmployeeId());
                result = employeeBUS.updateEmploymentInfo(employee);
                if (result) {
                    NotificationUtil.showSuccessNotification("Thành công", "Cập nhật thông tin công việc thành công");
                    parentController.refreshData();
                }
            }
        } catch (IllegalArgumentException e) {
            NotificationUtil.showErrorNotification("Lỗi dữ liệu", e.getMessage());
        } catch (Exception e) {
            NotificationUtil.showErrorNotification("Lỗi", "Không thể lưu thông tin công việc: " + e.getMessage());
        }
    }

    // Handle view history action
    private void handleViewHistory() {
        if (selectedData == null) {
            NotificationUtil.showErrorNotification("Lỗi", "Không tìm thấy thông tin nhân viên");
            return;
        }
        try {
            // Here you would implement the history view
            // This is a placeholder for where you'd show employee edit history
//            NotificationUtil.showSuccessNotification("Lịch sử chỉnh sửa",
//                    "Đang hiển thị lịch sử chỉnh sửa cho nhân viên: " + selectedData.getFirstName() + " " + selectedData.getLastName());

            // TODO: Replace with actual history display logic
             EmployeeHistoryController.show(busFactory, selectedData);
        } catch (Exception e) {
            NotificationUtil.showErrorNotification("Lỗi", "Không thể hiển thị lịch sử chỉnh sửa: " + e.getMessage());
        }
    }

    private <T extends Control> void addFormField(GridPane grid, String labelText, T control, int row) {
        Label label = new Label(labelText);
        label.getStyleClass().add("form-label");

        grid.add(label, 0, row);
        grid.add(control, 1, row);
    }

    private void addFormField(GridPane grid, String labelText, HBox controlContainer, int row) {
        Label label = new Label(labelText);
        label.getStyleClass().add("form-label");

        grid.add(label, 0, row);
        grid.add(controlContainer, 1, row);
    }

    private void loadPositions() {
        try {
            List<PositionDTO> positions = positionBUS.findAll();
            ObservableList<PositionWrapper> positionWrappers = FXCollections.observableArrayList(
                positions.stream()
                    .map(pos -> {
                        String departmentName = "N/A";
                        try {
                            DepartmentDTO dept = departmentBUS.findById(pos.getDepartmentId());
                            if (dept != null) {
                                departmentName = dept.getDepartmentName();
                            }
                        } catch (Exception ignored) {}

                        return new PositionWrapper(pos, departmentName);
                    })
                    .collect(Collectors.toList())
            );
            cboPosition.setItems(positionWrappers);

            // Select first item if nothing selected
            if (cboPosition.getSelectionModel().getSelectedItem() == null && !positionWrappers.isEmpty()) {
                cboPosition.getSelectionModel().clearSelection();
            }
        } catch (Exception e) {
            NotificationUtil.showErrorNotification("Lỗi", "Không thể tải danh sách chức vụ: " + e.getMessage());
        }
    }

    private void loadEmployeeData() {
        Platform.runLater(() -> {
            // Fill in the form with employee data
            txtFirstName.setText(selectedData.getFirstName());
            txtLastName.setText(selectedData.getLastName());
            cboGender.setValue(selectedData.getGender());
            dtpBirthday.setValue(selectedData.getBirthday());
            txtPhone.setText(selectedData.getPhone());
            txtEmail.setText(selectedData.getEmail());
            txtAddress.setText(selectedData.getAddress());
            dtpHireDate.setValue(selectedData.getHireDate()== null ? LocalDate.now() : selectedData.getHireDate());

            // Set status
            int statusIndex = selectedData.getStatus() == 1 ? 0 : 1; // 1 = Working (index 0), 0 = Left (index 1)
            cboStatus.getSelectionModel().select(statusIndex);

            // Select position in combo box
            for (PositionWrapper posWrapper : cboPosition.getItems()) {
                if (posWrapper.getPosition().getPositionId() == selectedData.getPositionId()) {
                    cboPosition.setValue(posWrapper);
                    break;
                }
            }
            txtBasicSalary.setText(formatSalary(selectedData.getBasicSalary()));

        });
    }

    private String formatSalary(BigDecimal basicSalary) {
        if (basicSalary != null) {
            return String.format("%,.0f", basicSalary);
        }
        return "";
    }

    // Separate validation for personal info
    private boolean validatePersonalInfo() {
        StringBuilder errorMessages = new StringBuilder();

        // Validate required fields
        if (txtFirstName.getText().trim().isEmpty()) {
            errorMessages.append("- Họ đệm không được để trống\n");
        }

        if (txtLastName.getText().trim().isEmpty()) {
            errorMessages.append("- Tên không được để trống\n");
        }

        if (cboGender.getValue() == null) {
            errorMessages.append("- Vui lòng chọn giới tính\n");
        }

        if (dtpBirthday.getValue() == null) {
            errorMessages.append("- Vui lòng chọn ngày sinh\n");
        } else if (dtpBirthday.getValue().isAfter(LocalDate.now())) {
            errorMessages.append("- Ngày sinh không thể là ngày trong tương lai\n");
        }

        if (txtPhone.getText().trim().isEmpty()) {
            errorMessages.append("- Số điện thoại không được để trống\n");
        } else
    if (!txtPhone.getText().trim().matches("^(?:(?:0|\\+84)\\d{9}|\\+\\d{1,3}(?:[ \\-]\\d{1,4}){2,4})$")) {
        errorMessages.append("- Số điện thoại không đúng định dạng\n");
    }

        if (!txtEmail.getText().trim().isEmpty() &&
                !txtEmail.getText().trim().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            errorMessages.append("- Email không đúng định dạng\n");
        }

        if (txtAddress.getText().trim().isEmpty()) {
            errorMessages.append("- Địa chỉ không được để trống\n");
        }

        // Show error dialog if there are validation errors
        if (!errorMessages.isEmpty()) {
            NotificationUtil.showErrorNotification("Lỗi", "Vui lòng kiểm tra lại thông tin cá nhân:", String.valueOf(errorMessages));
            return false;
        }

        return true;
    }

    // Separate validation for employment info
    private boolean validateEmploymentInfo() {
        StringBuilder errorMessages = new StringBuilder();

        if (cboPosition.getValue() == null) {
            errorMessages.append("- Vui lòng chọn chức vụ\n");
        }

        if (dtpHireDate.getValue() == null) {
            errorMessages.append("- Vui lòng chọn ngày thuê\n");
        }

        try {
            if (txtBasicSalary.getText().trim().isEmpty()) {
                errorMessages.append("- Lương cơ bản không được để trống\n");
            } else {
                BigDecimal salary = new BigDecimal(txtBasicSalary.getText().trim().replace(",", ""));
                if (salary.compareTo(BigDecimal.ZERO) < 0) {
                    errorMessages.append("- Lương cơ bản không thể âm\n");
                }
            }
        } catch (NumberFormatException e) {
            errorMessages.append("- Lương cơ bản phải là số\n");
        }

        if (cboStatus.getValue() == null) {
            errorMessages.append("- Vui lòng chọn trạng thái\n");
        }

        // Show error dialog if there are validation errors
        if (!errorMessages.isEmpty()) {
            NotificationUtil.showErrorNotification("Lỗi", "Vui lòng kiểm tra lại thông tin công việc:", String.valueOf(errorMessages));
            return false;
        }

        return true;
    }

    // Extract personal info
    protected EmployeeDTO extractPersonalInfoFromForm() {
        EmployeeDTO employee = new EmployeeDTO();

        if (actionType == ActionType.EDIT && selectedData != null) {
            employee.setEmployeeId(selectedData.getEmployeeId());
        }
        employee.setFirstName(txtFirstName.getText().trim());
        employee.setLastName(txtLastName.getText().trim());
        employee.setGender(cboGender.getValue());
        employee.setBirthday(dtpBirthday.getValue());
        employee.setPhone(txtPhone.getText().trim());
        employee.setEmail(txtEmail.getText().trim());
        employee.setAddress(txtAddress.getText().trim());

        return employee;
    }

    // Extract employment info
    protected EmployeeDTO extractEmploymentInfoFromForm() {
        EmployeeDTO employee = new EmployeeDTO();

        if (actionType == ActionType.EDIT && selectedData != null) {
            employee.setEmployeeId(selectedData.getEmployeeId());
        }
        employee.setPositionId(cboPosition.getValue().getPosition().getPositionId());
        employee.setHireDate(dtpHireDate.getValue());
        employee.setBasicSalary(new BigDecimal(txtBasicSalary.getText().trim().replace(",", "")));
        employee.setPositionName(cboPosition.getValue().getPosition().getPositionName());
        employee.setStatus(cboStatus.getValue().equals("Đang làm việc") ? 1 : 0);

        return employee;
    }

    @Override
    protected void handleSubmit() {
//        try {
//            if (!validateForm()) {
//                return;
//            }
//            EmployeeDTO employee = extractDataFromForm();
//            boolean result;
//            if (actionType == ActionType.ADD) {
//                result = employeeBUS.insert(employee);
//                if (result) {
//                    NotificationUtil.showSuccessNotification("Thành công", "Thêm nhân viên thành công");
//                    parentController.refreshData();
//                    closeDialog();
//                }
//            } else {
//                result = employeeBUS.update(employee);
//                if (result) {
//                    NotificationUtil.showSuccessNotification("Thành công", "Cập nhật thông tin nhân viên thành công");
//                    parentController.refreshData();
//                    closeDialog();
//                }
//            }
//        } catch (IllegalArgumentException e) {
//            NotificationUtil.showErrorNotification("Lỗi dữ liệu", e.getMessage());
//        } catch (Exception e) {
//            NotificationUtil.showErrorNotification("Lỗi", "Không thể lưu thông tin nhân viên: ", e.getMessage());
//        }
    }

    // Helper class to display position with department in ComboBox
    private static class PositionWrapper {
        @Getter
        private final PositionDTO position;
        private final String departmentName;

        public PositionWrapper(PositionDTO position, String departmentName) {
            this.position = position;
            this.departmentName = departmentName;
        }

        @Override
        public String toString() {
            return position.getPositionName() + " - " + departmentName;
        }
    }

    // Static helper method to show the dialog
    public static void showDialog(BUSFactory busFactory, EmployeeController controller, ActionType actionType) throws Exception {
        showDialog(busFactory, controller, actionType, null);
    }

    public static void showDialog(BUSFactory busFactory, EmployeeController controller, ActionType actionType, EmployeeDTO employee) throws Exception {
        EmployeeAction controllerInstance = new EmployeeAction(busFactory, controller, actionType , employee);
        // Show the dialog
        SubAction.showDialog(controllerInstance, actionType == ActionType.ADD ? "Thêm nhân viên" : "Cập nhật nhân viên");
    }
}