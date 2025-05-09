package com.example.medicinedistribution.GUI;

import com.example.medicinedistribution.BUS.BUSFactory;
import com.example.medicinedistribution.BUS.Interface.EmployeeBUS;
import com.example.medicinedistribution.DTO.EmployeeDTO;
import com.example.medicinedistribution.Util.NotificationUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;


public class UserProfileStage extends Stage {
    private final EmployeeDTO currentUser;
    private final EmployeeBUS employeeBUS;
    private final BUSFactory busFactory ;

    public UserProfileStage(BUSFactory busFactory) {
        this.currentUser = busFactory.getUserSession().getEmployee();
        this.employeeBUS = busFactory.getEmployeeBUS();
        this.busFactory = busFactory;

        setTitle("Thông tin người dùng");
        initModality(Modality.APPLICATION_MODAL);
        setMinWidth(800);
        setMinHeight(500);

        BorderPane mainLayout = new BorderPane();
        mainLayout.getStyleClass().add("main-container");

        // Header
        HBox header = createHeader();
        mainLayout.setTop(header);

        // Content
        HBox content = new HBox(20);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);

        // Personal info section
        VBox personalInfoSection = createPersonalInfoSection();

        // Password change section
        VBox passwordChangeSection = createPasswordChangeSection();

        content.getChildren().addAll(personalInfoSection, passwordChangeSection);
        mainLayout.setCenter(content);

        Scene scene = new Scene(mainLayout);
        scene.getStylesheets().add(getClass().getResource("/css/main-style.css").toExternalForm());
        setScene(scene);
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.getStyleClass().add("header-section");
        header.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Thông tin cá nhân");
        titleLabel.getStyleClass().add("heading-label");

        header.getChildren().add(titleLabel);
        return header;
    }

    private VBox createPersonalInfoSection() {
        VBox section = new VBox(15);
        section.setPrefWidth(350);
        section.getStyleClass().add("settings-section");

        Label sectionTitle = new Label("Thông tin cá nhân");
        sectionTitle.getStyleClass().add("section-title");

        // Personal info form
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(15);
        infoGrid.setVgap(10);

        // First name field
        infoGrid.add(new Label("Họ:"), 0, 0);
        TextField firstNameField = new TextField(currentUser.getFirstName());
        infoGrid.add(firstNameField, 1, 0);
        // Last name field
        infoGrid.add(new Label("Tên:"), 0, 1);
        TextField lastNameField = new TextField(currentUser.getLastName());
        infoGrid.add(lastNameField, 1, 1);


        // Email field
        infoGrid.add(new Label("Email:"), 0, 2);
        TextField emailField = new TextField(currentUser.getEmail());
        infoGrid.add(emailField, 1, 2);

        // Phone field
        infoGrid.add(new Label("Điện thoại:"), 0, 3);
        TextField phoneField = new TextField(currentUser.getPhone());
        infoGrid.add(phoneField, 1, 3);

        // Address field
        infoGrid.add(new Label("Địa chỉ:"), 0, 4);
        TextField addressField = new TextField(currentUser.getAddress());
        infoGrid.add(addressField, 1, 4);

        // Date of birth field
        infoGrid.add(new Label("Ngày sinh:"), 0, 5);
        DatePicker dobPicker = new DatePicker(currentUser.getBirthday());
        infoGrid.add(dobPicker, 1, 5);

        // Save button
        Button saveInfoButton = new Button("Lưu thông tin");
        saveInfoButton.getStyleClass().add("success-button");
        saveInfoButton.setOnAction(e -> savePersonalInfo(
            firstNameField.getText(),
            lastNameField.getText(),
            emailField.getText(),
            phoneField.getText(),
            addressField.getText(),
            dobPicker.getValue()
        ));

        section.getChildren().addAll(sectionTitle, infoGrid, saveInfoButton);
        return section;
    }

    private VBox createPasswordChangeSection() {
        VBox section = new VBox(15);
        section.setPrefWidth(350);
        section.getStyleClass().add("settings-section");

        Label sectionTitle = new Label("Đổi mật khẩu");
        sectionTitle.getStyleClass().add("section-title");

        // Password change form
        GridPane passwordGrid = new GridPane();
        passwordGrid.setHgap(15);
        passwordGrid.setVgap(10);

        // Current password field
        passwordGrid.add(new Label("Mật khẩu hiện tại:"), 0, 0);
        PasswordField currentPasswordField = new PasswordField();
        passwordGrid.add(currentPasswordField, 1, 0);

        // New password field
        passwordGrid.add(new Label("Mật khẩu mới:"), 0, 1);
        PasswordField newPasswordField = new PasswordField();
        passwordGrid.add(newPasswordField, 1, 1);

        // Confirm new password field
        passwordGrid.add(new Label("Nhập lại mật khẩu:"), 0, 2);
        PasswordField confirmPasswordField = new PasswordField();
        passwordGrid.add(confirmPasswordField, 1, 2);

        // Error label
        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setVisible(false);

        // Save password button
        Button savePasswordButton = new Button("Đổi mật khẩu");
        savePasswordButton.getStyleClass().add("primary-button");
        savePasswordButton.setOnAction(e -> changePassword(
            currentPasswordField.getText(),
            newPasswordField.getText(),
            confirmPasswordField.getText(),
            errorLabel
        ));

        // Add a separator
        Separator separator = new Separator();
        separator.getStyleClass().add("divider");
        separator.setPadding(new Insets(10, 0, 10, 0));

        // Additional features section
        Label additionalFeaturesLabel = new Label("Tính năng bổ sung");
        additionalFeaturesLabel.getStyleClass().add("section-subtitle");

        // Salary button
        Button salaryButton = new Button("Xem lương");
        salaryButton.getStyleClass().addAll("button", "primary-button");
        salaryButton.setMaxWidth(Double.MAX_VALUE);
        salaryButton.setOnAction(e -> showSalaryExport());

        // Request button
        Button requestButton = new Button("Nộp đơn");
        requestButton.getStyleClass().addAll("button", "secondary-button");
        requestButton.setMaxWidth(Double.MAX_VALUE);
        requestButton.setOnAction(e -> showRequestCreation());

        // Add buttons to a VBox for additional features
        VBox additionalButtons = new VBox(10);
        additionalButtons.getChildren().addAll(salaryButton, requestButton);

        section.getChildren().addAll(
                sectionTitle,
                passwordGrid,
                savePasswordButton,
                errorLabel,
                separator,
                additionalFeaturesLabel,
                additionalButtons
        );
        return section;
    }

    private void showSalaryExport() {
        SalaryExportDialog.show(busFactory, this);
    }
    private void showRequestCreation() {
        RequestCreationDialog.show(busFactory, this);
    }

    private void savePersonalInfo(String firstName,String lateName, String email, String phone, String address, LocalDate dob) {
        try {
            // Create a copy of current user with updated info
            EmployeeDTO updatedUser = new EmployeeDTO(currentUser);
            updatedUser.setFirstName(firstName);
            updatedUser.setLastName(lateName);
            updatedUser.setEmail(email);
            updatedUser.setPhone(phone);
            updatedUser.setAddress(address);
            updatedUser.setBirthday(dob);

            // Update user in database
            boolean success = employeeBUS.update(updatedUser);

            if (success) {
                NotificationUtil.showSuccessNotification("Cập nhật thành công",
                    "Thông tin cá nhân đã được cập nhật");
                // Update user session
                busFactory.getUserSession().setEmployee(updatedUser);
            } else {
                NotificationUtil.showErrorNotification("Lỗi",
                    "Không thể cập nhật thông tin cá nhân");
            }
        } catch (Exception e) {
            NotificationUtil.showErrorNotification("Lỗi",
                "Đã xảy ra lỗi: " + e.getMessage());
        }
    }

    private void changePassword(String currentPassword, String newPassword, String confirmPassword, Label errorLabel) {
        // Hide previous error messages
        errorLabel.setVisible(false);

        // Validate input
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showError(errorLabel, "Vui lòng điền đầy đủ thông tin");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showError(errorLabel, "Mật khẩu mới không khớp");
            return;
        }

        if (newPassword.length() < 6) {
            showError(errorLabel, "Mật khẩu mới phải có ít nhất 6 ký tự");
            return;
        }

        try {
            // Check if current password is correct and update password
            boolean success = busFactory.getAuthBUS().changePassword(busFactory.getUserSession().getAccount().getUsername(), currentPassword, newPassword);

            if (success) {
                NotificationUtil.showSuccessNotification("Đổi mật khẩu thành công",
                    "Mật khẩu của bạn đã được thay đổi");
            } else {
                showError(errorLabel, "Mật khẩu hiện tại không đúng");
            }
        } catch (Exception e) {
            showError(errorLabel, "Đã xảy ra lỗi: " + e.getMessage());
        }
    }

    private void showError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}