package com.example.medicinedistribution.GUI.SubAction;

    import com.example.medicinedistribution.BUS.BUSFactory;
    import com.example.medicinedistribution.BUS.Interface.CustomerBUS;
    import com.example.medicinedistribution.DTO.CustomerDTO;
    import com.example.medicinedistribution.Exception.PermissionDeniedException;
    import com.example.medicinedistribution.GUI.CustomerController;
    import com.example.medicinedistribution.Util.NotificationUtil;
    import com.example.medicinedistribution.Util.ValidateUtil;
    import javafx.fxml.FXML;
    import javafx.geometry.Insets;
    import javafx.scene.control.*;
    import javafx.scene.layout.HBox;
    import javafx.scene.layout.VBox;
    import javafx.scene.text.Font;
    import lombok.Getter;

    import java.util.HashMap;
    import java.util.Map;

    public class CustomerAction extends SubAction<CustomerController, CustomerDTO> {
        private final Map<String, TextField> formFields = new HashMap<>();
        private final ActionType actionType;
        private final CustomerBUS customerBUS;

        @FXML
        private Label lblHeader;

        public CustomerAction(BUSFactory busFactory, CustomerController parentController, ActionType actionType, CustomerDTO selectedData) {
            super(busFactory, parentController, selectedData);
            this.actionType = actionType;
            this.customerBUS = busFactory.getCustomerBUS();
        }

        @FXML
        public void initialize() {
            // Set the header text based on action type
            lblHeader.setText(actionType == ActionType.ADD ? "Thêm khách hàng" : "Cập nhật khách hàng");

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
            // Common styling for all form fields

            // Create customer field groups
            addFormField("customerName", "Tên khách hàng",
                    selectedData!=null ? selectedData.getCustomerName() :"" , "Nhập tên khách hàng");
            addFormField("phone", "Số điện thoại",
                    selectedData!=null ? selectedData.getPhone() :"" , "Nhập số điện thoại");
            addFormField("email", "Email",
                    selectedData!=null ? selectedData.getEmail() :"" , "Nhập email");
            addFormField("address", "Địa chỉ",
                    selectedData!=null ? selectedData.getAddress() :"" , "Nhập địa chỉ");

            // If editing, populate with existing data
            if (actionType == ActionType.EDIT && selectedData != null) {
                CustomerDTO customer = selectedData;

                // Fill in other fields
                formFields.get("customerName").setText(customer.getCustomerName());
                formFields.get("phone").setText(customer.getPhone());
                formFields.get("email").setText(customer.getEmail());
                formFields.get("address").setText(customer.getAddress());
            }
        }

        private void addFormField(String fieldName, String labelText, String defaultValue ,String placeholder) {
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
            String customerName = formFields.get("customerName").getText().trim();
            String phone = formFields.get("phone").getText().trim();
            String email = formFields.get("email").getText().trim();
            String address = formFields.get("address").getText().trim();

            // Validate customer name
            if (customerName.isEmpty()) {
                NotificationUtil.showErrorNotification("Lỗi", "Tên khách hàng không được để trống");
                return false;
            }

            if (customerName.length() > 100) {
                NotificationUtil.showErrorNotification("Lỗi", "Tên khách hàng không được vượt quá 100 ký tự");
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

            if (address.length() > 255) {
                NotificationUtil.showErrorNotification("Lỗi", "Địa chỉ không được vượt quá 255 ký tự");
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
                CustomerDTO customer = new CustomerDTO();

                // Set customer data from form fields
                if (actionType == ActionType.EDIT) {
                    customer.setCustomerId(selectedData.getCustomerId());
                }

                customer.setCustomerName(formFields.get("customerName").getText().trim());
                customer.setPhone(formFields.get("phone").getText().trim());
                customer.setEmail(formFields.get("email").getText().trim());
                customer.setAddress(formFields.get("address").getText().trim());

                boolean success;

                if (actionType == ActionType.ADD) {
                    success = customerBUS.insert(customer);
                    if (success) {
                        NotificationUtil.showSuccessNotification("Thành công", "Thêm khách hàng thành công");
                    }
                } else {
                    success = customerBUS.update(customer);
                    if (success) {
                        NotificationUtil.showSuccessNotification("Thành công", "Cập nhật khách hàng thành công");
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

        public static void showDialog(BUSFactory busFactory, CustomerController customerController, ActionType actionType) {
            showDialog(busFactory, customerController, actionType, null);
        }

        public static void showDialog(BUSFactory busFactory, CustomerController customerController, ActionType actionType, CustomerDTO selectedItem) {
            CustomerAction controller = new CustomerAction(busFactory, customerController, actionType, selectedItem);
            SubAction.showDialog(controller, actionType == ActionType.ADD ? "Thêm khách hàng" : "Cập nhật khách hàng");
        }
    }