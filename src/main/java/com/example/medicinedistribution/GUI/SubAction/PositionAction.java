package com.example.medicinedistribution.GUI.SubAction;

import com.example.medicinedistribution.BUS.BUSFactory;
import com.example.medicinedistribution.BUS.Interface.DepartmentBUS;
import com.example.medicinedistribution.BUS.Interface.PositionBUS;
import com.example.medicinedistribution.DTO.DepartmentDTO;
import com.example.medicinedistribution.DTO.PositionDTO;
import com.example.medicinedistribution.Exception.InsertFailedException;
import com.example.medicinedistribution.Exception.PermissionDeniedException;
import com.example.medicinedistribution.Exception.UpdateFailedException;
import com.example.medicinedistribution.GUI.DepartmentController;
import com.example.medicinedistribution.Util.NotificationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PositionAction extends SubAction<DepartmentController, PositionDTO> {
    private final Map<String, TextField> formFields = new HashMap<>();
    private final ActionType actionType;
    private final PositionBUS positionBUS;
    private final DepartmentBUS departmentBUS;
    private ComboBox<DepartmentDTO> cbDepartment;
    private DepartmentDTO selectedDepartment;

    @FXML
    private Label lblHeader;

    public PositionAction(BUSFactory busFactory, DepartmentController parentController, ActionType actionType, PositionDTO selectedData, DepartmentDTO selectedDepartment) {
        super(busFactory, parentController, selectedData);
        this.actionType = actionType;
        this.positionBUS = busFactory.getPositionBUS();
        this.departmentBUS = busFactory.getDepartmentBUS();
        this.selectedDepartment = selectedDepartment;
    }

    @FXML
    public void initialize() {
        // Set header text based on action type
        lblHeader.setText(actionType == ActionType.ADD ? "Thêm chức vụ" : "Cập nhật chức vụ");

        // Clear existing form content
        formContent.getChildren().clear();

        // Create form fields
        createFormFields();

        // Set button text
        btnSubmit.setText(actionType == ActionType.ADD ? "Thêm" : "Cập nhật");

        // Set button actions
        btnSubmit.setOnAction(event -> handleSubmit());
        btnCancel.setOnAction(event -> closeDialog());
    }

    @Override
    protected void createFormFields() {
        // Position name field
        addFormField("positionName", "Tên chức vụ",
                selectedData != null ? selectedData.getPositionName() : "",
                "Nhập tên chức vụ");

        // Allowance field with number-only filter
        TextField allowanceField = new TextField();
        allowanceField.setFont(new Font(14));
        allowanceField.setPromptText("Nhập trợ cấp");
        HBox.setHgrow(allowanceField, javafx.scene.layout.Priority.ALWAYS);

        // Set allowance value if editing
        if (selectedData != null && selectedData.getAllowance() != null) {
            allowanceField.setText(selectedData.getAllowance().toString());
        }

        // Add numeric only input filter
        allowanceField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                allowanceField.setText(oldValue);
            }
        });

        Label allowanceLabel = new Label("Phụ cấp chức vụ");
        allowanceLabel.setPrefWidth(150);
        allowanceLabel.setFont(new Font(14));

        HBox allowanceContainer = new HBox(10);
        allowanceContainer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        allowanceContainer.getChildren().addAll(allowanceLabel, allowanceField);
        formContent.getChildren().add(allowanceContainer);

        formFields.put("allowance", allowanceField);

        // Department ComboBox
        Label departmentLabel = new Label("Phòng ban");
        departmentLabel.setPrefWidth(150);
        departmentLabel.setFont(new Font(14));

        cbDepartment = new ComboBox<>();
        cbDepartment.setPrefWidth(300);
        HBox.setHgrow(cbDepartment, javafx.scene.layout.Priority.ALWAYS);

        // Set department items
        List<DepartmentDTO> departments = departmentBUS.findAll();
        cbDepartment.getItems().addAll(departments);

        // Set display converter for department names
        cbDepartment.setConverter(new javafx.util.StringConverter<DepartmentDTO>() {
            @Override
            public String toString(DepartmentDTO department) {
                return department != null ? department.getDepartmentName() : "";
            }

            @Override
            public DepartmentDTO fromString(String string) {
                return null; // Not needed for display-only
            }
        });

        // Pre-select department
        if (actionType == ActionType.ADD && selectedDepartment != null) {
            // When adding a new position for a specific department
            cbDepartment.setValue(selectedDepartment);
        } else if (selectedData != null) {
            // When editing an existing position
            int departmentId = selectedData.getDepartmentId();
            departments.stream()
                    .filter(dept -> dept.getDepartmentId() == departmentId)
                    .findFirst()
                    .ifPresent(cbDepartment::setValue);
        }

        HBox departmentContainer = new HBox(10);
        departmentContainer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        departmentContainer.getChildren().addAll(departmentLabel, cbDepartment);
        formContent.getChildren().add(departmentContainer);
    }

    private void addFormField(String fieldName, String labelText, String defaultValue, String placeholder) {
        Label label = new Label(labelText);
        label.setPrefWidth(150);
        label.setFont(new Font(14));

        TextField textField = new TextField(defaultValue);
        textField.setFont(new Font(14));
        textField.setPromptText(placeholder);
        HBox.setHgrow(textField, javafx.scene.layout.Priority.ALWAYS);

        HBox fieldContainer = new HBox(10);
        fieldContainer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        fieldContainer.getChildren().addAll(label, textField);
        formContent.getChildren().add(fieldContainer);

        formFields.put(fieldName, textField);
    }

    @Override
    protected boolean validateForm() {
        // Validate position name
        String positionName = formFields.get("positionName").getText().trim();
        if (positionName.isEmpty()) {
            NotificationUtil.showErrorNotification("Lỗi", "Tên chức vụ không được để trống");
            return false;
        }

        if (positionName.length() > 100) {
            NotificationUtil.showErrorNotification("Lỗi", "Tên chức vụ không được vượt quá 100 ký tự");
            return false;
        }

        // Validate allowance
        String allowanceText = formFields.get("allowance").getText().trim();
        if (allowanceText.isEmpty()) {
            NotificationUtil.showErrorNotification("Lỗi", "Phụ cấp chức vụ không được để trống");
            return false;
        }

        try {
            BigDecimal allowance = new BigDecimal(allowanceText);
            if (allowance.compareTo(BigDecimal.ZERO) < 0) {
                NotificationUtil.showErrorNotification("Lỗi", "Phụ cấp chức vụ không được âm");
                return false;
            }
        } catch (NumberFormatException e) {
            NotificationUtil.showErrorNotification("Lỗi", "Phụ cấp chức vụ phải là số");
            return false;
        }

        // Validate department selection
        if (cbDepartment.getValue() == null) {
            NotificationUtil.showErrorNotification("Lỗi", "Vui lòng chọn phòng ban");
            return false;
        }

        return true;
    }

    @Override
    protected void handleSubmit() {
        if (!validateForm()) {
            return;
        }

        try {
            // Get values from form
            String positionName = formFields.get("positionName").getText().trim();
            BigDecimal allowance = new BigDecimal(formFields.get("allowance").getText().trim());
            DepartmentDTO department = cbDepartment.getValue();

            // Create or update position
            PositionDTO position;
            if (actionType == ActionType.ADD) {
                position = new PositionDTO();
            } else {
                position = selectedData;
            }

            position.setPositionName(positionName);
            position.setAllowance(allowance);
            position.setDepartmentId(department.getDepartmentId());

            boolean success;
            if (actionType == ActionType.ADD) {
                success = positionBUS.insert(position);
                if (success) {
                    NotificationUtil.showSuccessNotification("Thành công", "Thêm chức vụ thành công");
                }
            } else {
                success = positionBUS.update(position);
                if (success) {
                    NotificationUtil.showSuccessNotification("Thành công", "Cập nhật chức vụ thành công");
                }
            }

            if (success) {
                // Refresh parent data and close dialog
                parentController.refreshData();
                closeDialog();
            }

        } catch (InsertFailedException | UpdateFailedException e) {
            NotificationUtil.showErrorNotification("Lỗi", e.getMessage());
        } catch (PermissionDeniedException e) {
            NotificationUtil.showErrorNotification("Lỗi quyền truy cập", e.getMessage());
        } catch (Exception e) {
            NotificationUtil.showErrorNotification("Lỗi", "Đã xảy ra lỗi không xác định: " + e.getMessage());
        }
    }

    public static void showDialog(BUSFactory busFactory, DepartmentController departmentController, ActionType actionType, DepartmentDTO selectedDepartment) {
        showDialog(busFactory, departmentController, actionType, null, selectedDepartment);
    }

    public static void showDialog(BUSFactory busFactory, DepartmentController departmentController, ActionType actionType, PositionDTO selectedItem) {
        showDialog(busFactory, departmentController, actionType, selectedItem, null);
    }

    public static void showDialog(BUSFactory busFactory, DepartmentController departmentController, ActionType actionType, PositionDTO selectedItem, DepartmentDTO selectedDepartment) {
        PositionAction controller = new PositionAction(busFactory, departmentController, actionType, selectedItem, selectedDepartment);
        SubAction.showDialog(controller, actionType == ActionType.ADD ? "Thêm chức vụ" : "Cập nhật chức vụ");
    }
}