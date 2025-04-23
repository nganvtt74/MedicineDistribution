package com.example.medicinedistribution.GUI.SubAction;

import com.example.medicinedistribution.BUS.BUSFactory;
import com.example.medicinedistribution.BUS.Interface.AllowanceBUS;
import com.example.medicinedistribution.DTO.AllowanceDTO;
import com.example.medicinedistribution.Exception.PermissionDeniedException;
import com.example.medicinedistribution.GUI.BenefitsController;
import com.example.medicinedistribution.GUI.Component.CurrencyTextField;
import com.example.medicinedistribution.Util.NotificationUtil;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class AllowanceAction extends SubAction<BenefitsController, AllowanceDTO> {
    private final Map<String, Control> formFields = new HashMap<>();
    private final ActionType actionType;
    private final AllowanceBUS allowanceBUS;

    @FXML
    private Label lblHeader;

    public AllowanceAction(BUSFactory busFactory, BenefitsController parentController, ActionType actionType, AllowanceDTO selectedData) {
        super(busFactory, parentController, selectedData);
        this.actionType = actionType;
        this.allowanceBUS = busFactory.getAllowanceBUS();
    }

    @FXML
    public void initialize() {
        // Set the header text based on action type
        lblHeader.setText(actionType == ActionType.ADD ? "Thêm phụ cấp" : "Cập nhật phụ cấp");

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
        // Create allowance field groups
        addTextField("name", "Tên phụ cấp",
                selectedData != null ? selectedData.getName() : "", "Nhập tên phụ cấp");

        addCurrencyField("amount", "Số tiền",
                selectedData != null ? selectedData.getAmount(): BigDecimal.ZERO, "Nhập số tiền");

        addCheckBox("isInsuranceIncluded", "Tính vào bảo hiểm",
                selectedData != null && selectedData.getIs_insurance_included());
    }

    private void addTextField(String fieldName, String labelText, String defaultValue, String placeholder) {
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
    private String formatSalary(BigDecimal basicSalary) {
        if (basicSalary != null) {
            return String.format("%,.0f", basicSalary);
        }
        return "";
    }

    private void addCurrencyField(String fieldName, String labelText, BigDecimal defaultValue, String placeholder) {
        Label label = new Label(labelText);
        label.setPrefWidth(150);
        label.setFont(new Font(14));

        TextField textField = new CurrencyTextField(formatSalary(defaultValue));
        textField.setFont(new Font(14));
        textField.setPromptText(placeholder);
        HBox.setHgrow(textField, javafx.scene.layout.Priority.ALWAYS);

        HBox fieldContainer = new HBox(10);
        fieldContainer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        fieldContainer.getChildren().addAll(label, textField);
        formContent.getChildren().add(fieldContainer);

        formFields.put(fieldName, textField);
    }

    private void addCheckBox(String fieldName, String labelText, boolean checked) {
        Label label = new Label(labelText);
        label.setPrefWidth(150);
        label.setFont(new Font(14));

        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(checked);

        HBox fieldContainer = new HBox(10);
        fieldContainer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        fieldContainer.getChildren().addAll(label, checkBox);
        formContent.getChildren().add(fieldContainer);

        formFields.put(fieldName, checkBox);
    }

    @Override
    protected boolean validateForm() {
        // Get values from form fields
        String name = ((TextField) formFields.get("name")).getText().trim();
        String amountText = ((TextField) formFields.get("amount")).getText().trim().replace(",", "");

        // Validate allowance name
        if (name.isEmpty()) {
            NotificationUtil.showErrorNotification("Lỗi", "Tên phụ cấp không được để trống");
            return false;
        }

        if (name.length() > 100) {
            NotificationUtil.showErrorNotification("Lỗi", "Tên phụ cấp không được vượt quá 100 ký tự");
            return false;
        }

        // Validate amount
        if (amountText.isEmpty()) {
            NotificationUtil.showErrorNotification("Lỗi", "Số tiền không được để trống");
            return false;
        }

        try {
            BigDecimal amount = new BigDecimal(amountText);
            if (amount.compareTo(BigDecimal.ZERO) < 0) {
                NotificationUtil.showErrorNotification("Lỗi", "Số tiền phải lớn hơn hoặc bằng 0");
                return false;
            }
        } catch (NumberFormatException e) {
            NotificationUtil.showErrorNotification("Lỗi", "Số tiền không hợp lệ");
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
            AllowanceDTO allowance = new AllowanceDTO();

            // Set allowance data from form fields
            if (actionType == ActionType.EDIT && selectedData != null) {
                allowance.setId(selectedData.getId());
            }

            allowance.setName(((TextField) formFields.get("name")).getText().trim());
            allowance.setAmount(new BigDecimal(((TextField) formFields.get("amount")).getText().trim().replace(",", "")));
            allowance.setIs_insurance_included(((CheckBox) formFields.get("isInsuranceIncluded")).isSelected());

            boolean success;

            if (actionType == ActionType.ADD) {
                success = allowanceBUS.insert(allowance);
                if (success) {
                    NotificationUtil.showSuccessNotification("Thành công", "Thêm phụ cấp thành công");
                }
            } else {
                success = allowanceBUS.update(allowance);
                if (success) {
                    NotificationUtil.showSuccessNotification("Thành công", "Cập nhật phụ cấp thành công");
                }
            }

            if (success) {
                // Refresh parent controller data
                parentController.refreshData();
                closeDialog();
            }

        } catch (PermissionDeniedException e) {
            NotificationUtil.showErrorNotification("Lỗi quyền hạn", e.getMessage());
        } catch (Exception e) {
            NotificationUtil.showErrorNotification("Lỗi", "Xảy ra lỗi: " + e.getMessage());
        }
    }

    public static void showDialog(BUSFactory busFactory, BenefitsController benefitsController, ActionType actionType) {
        showDialog(busFactory, benefitsController, actionType, null);
    }

    public static void showDialog(BUSFactory busFactory, BenefitsController benefitsController, ActionType actionType, AllowanceDTO selectedItem) {
        AllowanceAction controller = new AllowanceAction(busFactory, benefitsController, actionType, selectedItem);
        SubAction.showDialog(controller, actionType == ActionType.ADD ? "Thêm phụ cấp" : "Cập nhật phụ cấp");
    }
}