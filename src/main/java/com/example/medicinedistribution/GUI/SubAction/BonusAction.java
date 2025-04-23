package com.example.medicinedistribution.GUI.SubAction;

import com.example.medicinedistribution.BUS.BUSFactory;
import com.example.medicinedistribution.BUS.Interface.BonusBUS;
import com.example.medicinedistribution.BUS.Interface.BonusTypeBUS;
import com.example.medicinedistribution.BUS.Interface.EmployeeBUS;
import com.example.medicinedistribution.DTO.BonusDTO;
import com.example.medicinedistribution.DTO.BonusTypeDTO;
import com.example.medicinedistribution.DTO.EmployeeDTO;
import com.example.medicinedistribution.Exception.PermissionDeniedException;
import com.example.medicinedistribution.GUI.BenefitsController;
import com.example.medicinedistribution.GUI.Component.CurrencyTextField;
import com.example.medicinedistribution.Util.NotificationUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.util.StringConverter;
import lombok.extern.slf4j.Slf4j;
import org.controlsfx.control.SearchableComboBox;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class BonusAction extends SubAction<BenefitsController, BonusDTO> {
    private final Map<String, Control> formFields = new HashMap<>();
    private final ActionType actionType;
    private final BonusBUS bonusBUS;
    private final BonusTypeBUS bonusTypeBUS;
    private final EmployeeBUS employeeBUS;

    private List<BonusTypeDTO> bonusTypeList;
    private List<EmployeeDTO> employeeList;

    @FXML
    private Label lblHeader;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public BonusAction(BUSFactory busFactory, BenefitsController parentController, ActionType actionType, BonusDTO selectedData) {
        super(busFactory, parentController, selectedData);
        this.actionType = actionType;
        this.bonusBUS = busFactory.getBonusBUS();
        this.bonusTypeBUS = busFactory.getBonusTypeBUS();
        this.employeeBUS = busFactory.getEmployeeBUS();

        // Load reference data
        this.bonusTypeList = bonusTypeBUS.findAll();
        this.employeeList = employeeBUS.findAll();
    }

    @FXML
    public void initialize() {
        // Set the header text based on action type
        lblHeader.setText(actionType == ActionType.ADD ? "Thêm thưởng" : "Cập nhật thưởng");

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
        // Create bonus field groups
        addEmployeeComboBox("employee", "Nhân viên",
                selectedData != null ? selectedData.getEmployee_id() : null);

        addBonusTypeComboBox("bonusType", "Loại thưởng",
                selectedData != null ? selectedData.getBonus_type_id() : null);

        addCurrencyField("amount", "Số tiền",
                selectedData != null ? selectedData.getAmount() : BigDecimal.ZERO, "Nhập số tiền thưởng");

        addDatePicker("date", "Ngày thưởng",
                selectedData != null ? selectedData.getDate() : LocalDate.now());
    }

    private void addEmployeeComboBox(String fieldName, String labelText, Integer selectedEmployeeId) {
        Label label = new Label(labelText);
        label.setPrefWidth(150);
        label.setFont(new Font(14));

        SearchableComboBox<EmployeeDTO> comboBox = new SearchableComboBox<>();
        comboBox.setItems(FXCollections.observableArrayList(employeeList));
        comboBox.setPrefWidth(300);
        comboBox.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(comboBox, javafx.scene.layout.Priority.ALWAYS);

        // Set converter to display employee name
        comboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(EmployeeDTO employee) {
                if (employee == null) return null;
                return employee.getFullName() + " (" + employee.getEmployeeId() + ")";
            }

            @Override
            public EmployeeDTO fromString(String string) {
                return null; // Not needed for this implementation
            }
        });

        // Set prompt text
        comboBox.setPromptText("Chọn nhân viên");

        // Select the previously selected employee if editing
        if (selectedEmployeeId != null) {
            for (EmployeeDTO employee : employeeList) {
                if (employee.getEmployeeId().equals(selectedEmployeeId)) {
                    comboBox.setValue(employee);
                    break;
                }
            }
        }

        HBox fieldContainer = new HBox(10);
        fieldContainer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        fieldContainer.getChildren().addAll(label, comboBox);
        formContent.getChildren().add(fieldContainer);

        formFields.put(fieldName, comboBox);
    }

    private void addBonusTypeComboBox(String fieldName, String labelText, Integer selectedTypeId) {
        Label label = new Label(labelText);
        label.setPrefWidth(150);
        label.setFont(new Font(14));

        ComboBox<BonusTypeDTO> comboBox = new ComboBox<>();
        comboBox.setItems(FXCollections.observableArrayList(bonusTypeList));
        comboBox.setPrefWidth(300);
        comboBox.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(comboBox, javafx.scene.layout.Priority.ALWAYS);

        // Set converter to display bonus type name
        comboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(BonusTypeDTO bonusType) {
                if (bonusType == null) return null;
                return bonusType.getName();
            }

            @Override
            public BonusTypeDTO fromString(String string) {
                return null; // Not needed for this implementation
            }
        });

        // Set prompt text
        comboBox.setPromptText("Chọn loại thưởng");

        // Select the previously selected bonus type if editing
        if (selectedTypeId != null) {
            for (BonusTypeDTO bonusType : bonusTypeList) {
                if (bonusType.getId().equals(selectedTypeId)) {
                    comboBox.setValue(bonusType);
                    break;
                }
            }
        }

        HBox fieldContainer = new HBox(10);
        fieldContainer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        fieldContainer.getChildren().addAll(label, comboBox);
        formContent.getChildren().add(fieldContainer);
        formFields.put(fieldName, comboBox);
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

    private void addDatePicker(String fieldName, String labelText, LocalDate defaultValue) {
        Label label = new Label(labelText);
        label.setPrefWidth(150);
        label.setFont(new Font(14));

        DatePicker datePicker = new DatePicker(defaultValue);
        datePicker.setEditable(false);
        datePicker.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(datePicker, javafx.scene.layout.Priority.ALWAYS);

        HBox fieldContainer = new HBox(10);
        fieldContainer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        fieldContainer.getChildren().addAll(label, datePicker);
        formContent.getChildren().add(fieldContainer);

        formFields.put(fieldName, datePicker);
    }

    @Override
    protected boolean validateForm() {
        // Get values from form fields
        ComboBox<EmployeeDTO> employeeComboBox = (ComboBox<EmployeeDTO>) formFields.get("employee");
        ComboBox<BonusTypeDTO> bonusTypeComboBox = (ComboBox<BonusTypeDTO>) formFields.get("bonusType");
        String amountText = ((TextField) formFields.get("amount")).getText().trim().replace(",", "");
        DatePicker datePicker = (DatePicker) formFields.get("date");

        // Validate employee selection
        if (employeeComboBox.getValue() == null) {
            NotificationUtil.showErrorNotification("Lỗi", "Vui lòng chọn nhân viên");
            return false;
        }

        // Validate bonus type selection
        if (bonusTypeComboBox.getValue() == null) {
            NotificationUtil.showErrorNotification("Lỗi", "Vui lòng chọn loại thưởng");
            return false;
        }

        // Validate amount
        if (amountText.isEmpty()) {
            NotificationUtil.showErrorNotification("Lỗi", "Số tiền không được để trống");
            return false;
        }

        try {
            BigDecimal amount = new BigDecimal(amountText);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                NotificationUtil.showErrorNotification("Lỗi", "Số tiền phải lớn hơn 0");
                return false;
            }
        } catch (NumberFormatException e) {
            NotificationUtil.showErrorNotification("Lỗi", "Số tiền không hợp lệ");
            return false;
        }

        // Validate date
        if (datePicker.getValue() == null) {
            NotificationUtil.showErrorNotification("Lỗi", "Vui lòng chọn ngày thưởng");
            return false;
        }

        // Ensure date is not in the future
        if (datePicker.getValue().isAfter(LocalDate.now())) {
            NotificationUtil.showErrorNotification("Lỗi", "Ngày thưởng không thể nằm trong tương lai");
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
            BonusDTO bonus = new BonusDTO();

            // Set bonus data from form fields
            if (actionType == ActionType.EDIT && selectedData != null) {
                bonus.setId(selectedData.getId());
            }

            ComboBox<EmployeeDTO> employeeComboBox = (ComboBox<EmployeeDTO>) formFields.get("employee");
            ComboBox<BonusTypeDTO> bonusTypeComboBox = (ComboBox<BonusTypeDTO>) formFields.get("bonusType");

            bonus.setEmployee_id(employeeComboBox.getValue().getEmployeeId());
            bonus.setBonus_type_id(bonusTypeComboBox.getValue().getId());
            bonus.setAmount(new BigDecimal(((TextField) formFields.get("amount")).getText().trim().replace(",", "")));
            bonus.setDate(((DatePicker) formFields.get("date")).getValue());

            boolean success;

            if (actionType == ActionType.ADD) {
                success = bonusBUS.insert(bonus);
                if (success) {
                    NotificationUtil.showSuccessNotification("Thành công", "Thêm khoản thưởng thành công");
                }
            } else {
                success = bonusBUS.update(bonus);
                if (success) {
                    NotificationUtil.showSuccessNotification("Thành công", "Cập nhật khoản thưởng thành công");
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

    public static void showDialog(BUSFactory busFactory, BenefitsController benefitsController, ActionType actionType, BonusDTO selectedItem) {
        BonusAction controller = new BonusAction(busFactory, benefitsController, actionType, selectedItem);
        SubAction.showDialog(controller, actionType == ActionType.ADD ? "Thêm khoản thưởng" : "Cập nhật khoản thưởng");
    }
}