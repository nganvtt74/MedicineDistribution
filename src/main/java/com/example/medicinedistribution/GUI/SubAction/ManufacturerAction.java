package com.example.medicinedistribution.GUI.SubAction;

import com.example.medicinedistribution.BUS.BUSFactory;
import com.example.medicinedistribution.BUS.Interface.ManufacturerBUS;
import com.example.medicinedistribution.DTO.ManufacturerDTO;
import com.example.medicinedistribution.Exception.PermissionDeniedException;
import com.example.medicinedistribution.GUI.ManufacturerController;
import com.example.medicinedistribution.Util.NotificationUtil;
import com.example.medicinedistribution.Util.ValidateUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;

import java.util.HashMap;
import java.util.Map;

public class ManufacturerAction extends SubAction<ManufacturerController, ManufacturerDTO> {
    private final Map<String, TextField> formFields = new HashMap<>();
    private final ActionType actionType;
    private final ManufacturerBUS manufacturerBUS;

    @FXML
    private Label lblHeader;

    @FXML
    private TextArea descriptionArea;

    public ManufacturerAction(BUSFactory busFactory, ManufacturerController parentController, ActionType actionType, ManufacturerDTO selectedData) {
        super(busFactory, parentController, selectedData);
        this.actionType = actionType;
        this.manufacturerBUS = busFactory.getManufacturerBUS();
    }

    @FXML
    public void initialize() {
        // Set the header text based on action type
        lblHeader.setText(actionType == ActionType.ADD ? "Thêm nhà sản xuất" : "Cập nhật nhà sản xuất");

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
        // Create manufacturer field groups
        addFormField("manufacturerName", "Tên nhà sản xuất",
                selectedData != null ? selectedData.getManufacturerName() : "", "Nhập tên nhà sản xuất");
        addFormField("country", "Quốc gia",
                selectedData != null ? selectedData.getCountry() : "", "Nhập quốc gia");
        addFormField("phone", "Số điện thoại",
                selectedData != null ? selectedData.getPhone() : "", "Nhập số điện thoại");
        addFormField("email", "Email",
                selectedData != null ? selectedData.getEmail() : "", "Nhập email");
        addFormField("address", "Địa chỉ",
                selectedData != null ? selectedData.getAddress() : "", "Nhập địa chỉ");

        descriptionArea = new TextArea();
        descriptionArea.setFont(new Font(14));
        descriptionArea.setPromptText("Nhập mô tả về nhà sản xuất");
        descriptionArea.setPrefRowCount(3);

        if (selectedData != null && selectedData.getDescription() != null) {
            descriptionArea.setText(selectedData.getDescription());
        }

        Label descriptionLabel = new Label("Mô tả");
        descriptionLabel.setPrefWidth(150);
        descriptionLabel.setFont(new Font(14));

        HBox descriptionContainer = new HBox(10);
        descriptionContainer.setAlignment(javafx.geometry.Pos.TOP_LEFT);
        descriptionContainer.getChildren().addAll(descriptionLabel, descriptionArea);
        HBox.setHgrow(descriptionArea, javafx.scene.layout.Priority.ALWAYS);

        formContent.getChildren().add(descriptionContainer);

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
        // Get values from form fields
        String manufacturerName = formFields.get("manufacturerName").getText().trim();
        String country = formFields.get("country").getText().trim();
        String phone = formFields.get("phone").getText().trim();
        String email = formFields.get("email").getText().trim();
        String address = formFields.get("address").getText().trim();

        // Validate manufacturer name
        if (manufacturerName.isEmpty()) {
            NotificationUtil.showErrorNotification("Lỗi", "Tên nhà sản xuất không được để trống");
            return false;
        }

        if (manufacturerName.length() > 100) {
            NotificationUtil.showErrorNotification("Lỗi", "Tên nhà sản xuất không được vượt quá 100 ký tự");
            return false;
        }

        // Validate country
        if (country.isEmpty()) {
            NotificationUtil.showErrorNotification("Lỗi", "Quốc gia không được để trống");
            return false;
        }

        // Validate phone number
        if (phone.isEmpty()) {
            NotificationUtil.showErrorNotification("Lỗi", "Số điện thoại không được để trống");
            return false;
        }

        if (!ValidateUtil.isValidPhoneNumber(phone)) {
            NotificationUtil.showErrorNotification("Lỗi", "Số điện thoại không hợp lệ");
            return false;
        }

        // Validate email (optional)
        if (!email.isEmpty() && !ValidateUtil.isValidEmail(email)) {
            NotificationUtil.showErrorNotification("Lỗi", "Email không hợp lệ");
            return false;
        }

        // Validate address
        if (address.isEmpty()) {
            NotificationUtil.showErrorNotification("Lỗi", "Địa chỉ không được để trống");
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
            ManufacturerDTO manufacturer = new ManufacturerDTO();

            // Set manufacturer data from form fields
            if (actionType == ActionType.EDIT) {
                manufacturer.setManufacturerId(selectedData.getManufacturerId());
            }

            manufacturer.setManufacturerName(formFields.get("manufacturerName").getText().trim());
            manufacturer.setCountry(formFields.get("country").getText().trim());
            manufacturer.setPhone(formFields.get("phone").getText().trim());
            manufacturer.setEmail(formFields.get("email").getText().trim());
            manufacturer.setAddress(formFields.get("address").getText().trim());
            manufacturer.setDescription(descriptionArea.getText().trim());

            System.out.println("Manufacturer data: " + manufacturer);

            boolean success;

            if (actionType == ActionType.ADD) {
                success = manufacturerBUS.insert(manufacturer);
                if (success) {
                    NotificationUtil.showSuccessNotification("Thành công", "Thêm nhà sản xuất thành công");
                }
            } else {
                success = manufacturerBUS.update(manufacturer);
                if (success) {
                    NotificationUtil.showSuccessNotification("Thành công", "Cập nhật nhà sản xuất thành công");
                }
            }

            if (success) {
                // Refresh parent controller data
                parentController.setupUIData();
                parentController.loadData();
                closeDialog();
            }

        } catch (PermissionDeniedException e) {
            NotificationUtil.showErrorNotification("Lỗi quyền hạn", e.getMessage());
        } catch (Exception e) {
            NotificationUtil.showErrorNotification("Lỗi", "Xảy ra lỗi: " + e.getMessage());
        }
    }

    public static void showDialog(BUSFactory busFactory, ManufacturerController manufacturerController, ActionType actionType) {
        showDialog(busFactory, manufacturerController, actionType, null);
    }

    public static void showDialog(BUSFactory busFactory, ManufacturerController manufacturerController, ActionType actionType, ManufacturerDTO selectedItem) {
        ManufacturerAction controller = new ManufacturerAction(busFactory, manufacturerController, actionType, selectedItem);
        SubAction.showDialog(controller, actionType == ActionType.ADD ? "Thêm nhà sản xuất" : "Cập nhật nhà sản xuất");
    }
}