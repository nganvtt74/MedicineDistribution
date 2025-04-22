package com.example.medicinedistribution.GUI.SubAction;

    import com.example.medicinedistribution.BUS.BUSFactory;
    import com.example.medicinedistribution.BUS.Interface.DependentsBUS;
    import com.example.medicinedistribution.DTO.DependentsDTO;
    import com.example.medicinedistribution.GUI.DependentsController;
    import com.example.medicinedistribution.Util.NotificationUtil;
    import com.example.medicinedistribution.Util.ValidateUtil;
    import javafx.collections.FXCollections;
    import javafx.fxml.FXML;
    import javafx.geometry.Pos;
    import javafx.scene.control.*;
    import javafx.scene.layout.HBox;
    import javafx.scene.text.Font;
    import lombok.extern.slf4j.Slf4j;

    import java.time.LocalDate;
    import java.time.format.DateTimeFormatter;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;

    @Slf4j
    public class DependentAction extends SubAction<DependentsController, DependentsDTO> {
        private final DependentsBUS dependentsBUS;
        private final ActionType actionType;
        private final Integer employeeId;
        private final Map<String, Control> formFields = new HashMap<>();
        private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        @FXML
        private Label lblHeader;

        public DependentAction(BUSFactory busFactory, DependentsController parentController, ActionType actionType, DependentsDTO dependentsDTO ,Integer employeeId) {
            super(busFactory, parentController, dependentsDTO);
            this.actionType = actionType;
            this.dependentsBUS = busFactory.getDependentsBUS();
            this.employeeId = employeeId;
        }

        @FXML
        public void initialize() {
            // Set the header text based on action type
            lblHeader.setText(actionType == ActionType.ADD ? "Thêm thân nhân" : "Cập nhật thân nhân");

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
            // Add first name field
            addFormField("firstName", "Họ",
                    selectedData != null ? selectedData.getFirstName() : "", "Nhập họ");

            // Add last name field
            addFormField("lastName", "Tên",
                    selectedData != null ? selectedData.getLastName() : "", "Nhập tên");

            // Add birthday field
            addDateField("birthday", "Ngày sinh",
                    selectedData != null && selectedData.getBirthday() != null ? selectedData.getBirthday() : LocalDate.now());

            // Add relationship field with combo box
            addComboField("relationship", "Mối quan hệ",
                    selectedData != null ? selectedData.getRelationship() : "");
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
            fieldContainer.setAlignment(Pos.CENTER_LEFT);
            fieldContainer.getChildren().addAll(label, textField);
            formContent.getChildren().add(fieldContainer);

            formFields.put(fieldName, textField);
        }

        private void addDateField(String fieldName, String labelText, LocalDate defaultValue) {
            Label label = new Label(labelText);
            label.setPrefWidth(150);
            label.setFont(new Font(14));

            DatePicker datePicker = new DatePicker(defaultValue);
            datePicker.setEditable(false);
            datePicker.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(datePicker, javafx.scene.layout.Priority.ALWAYS);

            HBox fieldContainer = new HBox(10);
            fieldContainer.setAlignment(Pos.CENTER_LEFT);
            fieldContainer.getChildren().addAll(label, datePicker);
            formContent.getChildren().add(fieldContainer);

            formFields.put(fieldName, datePicker);
        }

        private void addComboField(String fieldName, String labelText, String defaultValue) {
            Label label = new Label(labelText);
            label.setPrefWidth(150);
            label.setFont(new Font(14));

            ComboBox<String> comboBox = new ComboBox<>();
            comboBox.setItems(FXCollections.observableArrayList(
                    "Cha", "Mẹ", "Vợ", "Chồng", "Con", "Anh", "Chị", "Em", "Khác"
            ));

            // Set default value if exists
            if (!defaultValue.isEmpty()) {
                comboBox.setValue(defaultValue);
            }

            comboBox.setEditable(true);
            comboBox.setPromptText("Chọn mối quan hệ");
            comboBox.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(comboBox, javafx.scene.layout.Priority.ALWAYS);

            HBox fieldContainer = new HBox(10);
            fieldContainer.setAlignment(Pos.CENTER_LEFT);
            fieldContainer.getChildren().addAll(label, comboBox);
            formContent.getChildren().add(fieldContainer);

            formFields.put(fieldName, comboBox);
        }

        @Override
        protected boolean validateForm() {
            // Get form field values
            String firstName = ((TextField) formFields.get("firstName")).getText().trim();
            String lastName = ((TextField) formFields.get("lastName")).getText().trim();
            LocalDate birthday = ((DatePicker) formFields.get("birthday")).getValue();
            String relationship = ((ComboBox<String>) formFields.get("relationship")).getValue();

            // Validate first name
            if (firstName.isEmpty()) {
                NotificationUtil.showErrorNotification("Lỗi", "Họ không được để trống");
                return false;
            }

            if (firstName.length() > 50) {
                NotificationUtil.showErrorNotification("Lỗi", "Họ không được vượt quá 50 ký tự");
                return false;
            }

            // Validate last name
            if (lastName.isEmpty()) {
                NotificationUtil.showErrorNotification("Lỗi", "Tên không được để trống");
                return false;
            }

            if (lastName.length() > 50) {
                NotificationUtil.showErrorNotification("Lỗi", "Tên không được vượt quá 50 ký tự");
                return false;
            }

            // Validate birthday
            if (birthday == null) {
                NotificationUtil.showErrorNotification("Lỗi", "Ngày sinh không được để trống");
                return false;
            }

            if (birthday.isAfter(LocalDate.now())) {
                NotificationUtil.showErrorNotification("Lỗi", "Ngày sinh không được lớn hơn ngày hiện tại");
                return false;
            }

            // Validate relationship
            if (relationship == null || relationship.isEmpty()) {
                NotificationUtil.showErrorNotification("Lỗi", "Mối quan hệ không được để trống");
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
                DependentsDTO dependent = new DependentsDTO();

                // Set employee ID
                dependent.setEmployeeId(employeeId);

                // If editing, set the dependent number
                if (actionType == ActionType.EDIT && selectedData != null) {
                    dependent.setDependentNo(selectedData.getDependentNo());
                }

                // Set dependent data from form fields
                dependent.setFirstName(((TextField) formFields.get("firstName")).getText().trim());
                dependent.setLastName(((TextField) formFields.get("lastName")).getText().trim());
                dependent.setBirthday(((DatePicker) formFields.get("birthday")).getValue());
                dependent.setRelationship(((ComboBox<String>) formFields.get("relationship")).getValue());

                boolean success;

                if (actionType == ActionType.ADD) {
                    success = dependentsBUS.insert(dependent);
                    if (success) {
                        NotificationUtil.showSuccessNotification("Thành công", "Thêm thân nhân thành công");
                    }
                } else {
                    success = dependentsBUS.update(dependent);
                    if (success) {
                        NotificationUtil.showSuccessNotification("Thành công", "Cập nhật thân nhân thành công");
                    }
                }

                if (success) {
                    // Refresh parent controller data
                    parentController.refreshData();
                    closeDialog();
                }

            } catch (Exception e) {
                log.error("Error handling dependent submission: ", e);
                NotificationUtil.showErrorNotification("Lỗi", "Xảy ra lỗi: " + e.getMessage());
            }
        }

        public boolean insert(DependentsDTO dependentsDTO) {
            try {
                if (employeeId == null) {
                    log.warn("Employee ID is not set");
                    return false;
                }

                dependentsDTO.setEmployeeId(employeeId);
                boolean result = dependentsBUS.insert(dependentsDTO);

                if (result) {
                    NotificationUtil.showSuccessNotification("Thành công", "Thêm thân nhân thành công");
                    return true;
                } else {
                    NotificationUtil.showErrorNotification("Lỗi", "Không thể thêm thân nhân");
                    return false;
                }
            } catch (Exception e) {
                log.error("Error inserting dependent: ", e);
                NotificationUtil.showErrorNotification("Lỗi", "Không thể thêm thân nhân: " + e.getMessage());
                return false;
            }
        }
        /**
         * Check if the dependent data is valid before saving
         */
        public boolean validateDependent(DependentsDTO dependent) {
            if (dependent.getFirstName() == null || dependent.getFirstName().trim().isEmpty()) {
                NotificationUtil.showErrorNotification("Lỗi", "Họ không được để trống");
                return false;
            }

            if (dependent.getLastName() == null || dependent.getLastName().trim().isEmpty()) {
                NotificationUtil.showErrorNotification("Lỗi", "Tên không được để trống");
                return false;
            }

            if (dependent.getRelationship() == null || dependent.getRelationship().trim().isEmpty()) {
                NotificationUtil.showErrorNotification("Lỗi", "Mối quan hệ không được để trống");
                return false;
            }

            return true;
        }

        public static void showDialog(BUSFactory busFactory, DependentsController dependentsController, ActionType actionType , Integer employeeId) {
            showDialog(busFactory, dependentsController, actionType, null, employeeId);
        }

        public static void showDialog(BUSFactory busFactory, DependentsController dependentsController, ActionType actionType, DependentsDTO selectedItem , Integer employeeId) {
            DependentAction controller = new DependentAction(busFactory, dependentsController, actionType, selectedItem , employeeId);
            SubAction.showDialog(controller, actionType == ActionType.ADD ? "Thêm thân nhân" : "Cập nhật thân nhân");
        }
    }