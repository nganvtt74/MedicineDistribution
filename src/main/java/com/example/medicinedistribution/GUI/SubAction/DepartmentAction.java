package com.example.medicinedistribution.GUI.SubAction;

import com.example.medicinedistribution.BUS.BUSFactory;
import com.example.medicinedistribution.BUS.Interface.DepartmentBUS;
import com.example.medicinedistribution.DTO.DepartmentDTO;
import com.example.medicinedistribution.Exception.InsertFailedException;
import com.example.medicinedistribution.Exception.PermissionDeniedException;
import com.example.medicinedistribution.Exception.UpdateFailedException;
import com.example.medicinedistribution.GUI.DepartmentController;
import com.example.medicinedistribution.Util.NotificationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.util.HashMap;
import java.util.Map;

public class DepartmentAction extends SubAction<DepartmentController, DepartmentDTO> {
    private final Map<String, TextField> formFields = new HashMap<>();
    private final ActionType actionType;
    private final DepartmentBUS departmentBUS;

    @FXML
    private Label lblHeader;


    public DepartmentAction(BUSFactory busFactory, DepartmentController parentController, ActionType actionType, DepartmentDTO selectedData) {
        super(busFactory, parentController, selectedData);
        this.actionType = actionType;
        this.departmentBUS = busFactory.getDepartmentBUS();
    }

    @FXML
    public void initialize() {
        // Set the header text based on action type
        lblHeader.setText(actionType == ActionType.ADD ? "Thêm phòng ban" : "Cập nhật phòng ban");

        // Clear any sample fields that might be in the FXML
        formContent.getChildren().clear();

        // Build the form based on action type
        createFormFields();

        // Set button text based on action type
        btnSubmit.setText(actionType == ActionType.ADD ? "Thêm" : "Cập nhật");

        // Setup button actions
        btnSubmit.setOnAction(event -> handleSubmit());
        btnCancel.setOnAction(event -> closeDialog());
    }

    @Override
    protected void createFormFields() {
        // Create department name field
        addFormField("departmentName", "Tên phòng ban",
                selectedData != null ? selectedData.getDepartmentName() : "",
                "Nhập tên phòng ban");
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
        String departmentName = formFields.get("departmentName").getText().trim();

        if (departmentName.isEmpty()) {
            NotificationUtil.showErrorNotification("Lỗi", "Tên phòng ban không được để trống");
            return false;
        }

        if (departmentName.length() > 100) {
            NotificationUtil.showErrorNotification("Lỗi", "Tên phòng ban không được vượt quá 100 ký tự");
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
            String departmentName = formFields.get("departmentName").getText().trim();
            boolean success;

            if (actionType == ActionType.ADD) {
                // Create new department
                DepartmentDTO newDepartment = new DepartmentDTO();
                newDepartment.setDepartmentName(departmentName);

                success = departmentBUS.insert(newDepartment);
                if (success) {
                    NotificationUtil.showSuccessNotification("Thành công", "Thêm phòng ban thành công");
                }
            } else {
                // Update existing department
                selectedData.setDepartmentName(departmentName);
                success = departmentBUS.update(selectedData);
                if (success) {
                    NotificationUtil.showSuccessNotification("Thành công", "Cập nhật phòng ban thành công");
                }
            }

            if (success) {
                // Refresh parent controller data
                parentController.refreshData();
                closeDialog();
            }

        }catch (InsertFailedException | UpdateFailedException e ) {
            NotificationUtil.showErrorNotification("Lỗi", e.getMessage());
        } catch (PermissionDeniedException e) {
            NotificationUtil.showErrorNotification("Lỗi quyền truy cập", e.getMessage());
        } catch (Exception e) {
            NotificationUtil.showErrorNotification("Lỗi", "Đã xảy ra lỗi không xác định: " + e.getMessage());
        }
    }

    public static void showDialog(BUSFactory busFactory, DepartmentController departmentController, ActionType actionType) {
        showDialog(busFactory, departmentController, actionType, null);
    }

    public static void showDialog(BUSFactory busFactory, DepartmentController departmentController, ActionType actionType, DepartmentDTO selectedItem) {
        DepartmentAction controller = new DepartmentAction(busFactory, departmentController, actionType, selectedItem);
        SubAction.showDialog(controller, actionType == ActionType.ADD ? "Thêm phòng ban" : "Cập nhật phòng ban");
    }
}