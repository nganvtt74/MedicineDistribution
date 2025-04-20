package com.example.medicinedistribution.GUI;

import com.example.medicinedistribution.BUS.BUSFactory;
import com.example.medicinedistribution.BUS.Interface.AuthBUS;
import com.example.medicinedistribution.Util.NotificationUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Objects;

@Slf4j
public class LoginController {
    @FXML
    public TextField txtUsername;
    @FXML
    public PasswordField txtPassword;
    @FXML
    public Button btnLogin;
    private final BUSFactory busFactory;
    private AuthBUS authBUS;


    public LoginController(BUSFactory busFactory) {
        // Constructor to initialize the controller with the BUSFactory
        // You can use this factory to access your business logic
        this.busFactory = busFactory;

    }

    @FXML
    public void initialize() {
        authBUS = busFactory.getAuthBUS();
        btnLogin.setOnAction(event -> handleLogin());
        txtUsername.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("TAB")) {
                txtPassword.requestFocus();
            }
        });
        txtPassword.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) {
                handleLogin();
            }
        });

    }
    private void handleLogin() {
        String username = txtUsername.getText();
        String password = txtPassword.getText();

        // Perform login logic here
        // For example, you can call a method to authenticate the user
        try {
            if (authBUS.login(username, password)) {
                NotificationUtil.showNotification("Đăng nhập thành công", "Chào mừng bạn đến với hệ thống phân phối thuốc!");
                // Load the main application UI or perform any other actions after successful login
                SalesManagementController salesManagementController = new SalesManagementController(busFactory);
                openStage("Sales-Management.fxml", salesManagementController);

            }
        } catch (RuntimeException e) {
            NotificationUtil.showErrorNotification("Đăng nhập thất bại", e.getMessage());
            txtPassword.requestFocus();
        }

        // Clear the fields after login attempt

    }

    public void openStage(String fxmlFile,Object controller) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(LoginController.class.getResource(fxmlFile));
            loader.setController(controller); // Set the controller for the new stage
            Parent root = loader.load();
            BorderPane borderPane = (BorderPane) root;
            ImageView icon = new ImageView(new Image(Objects.requireNonNull(getClass().getResource("../../../../img/logo.png")).toExternalForm()));
            Stage stage = (Stage) btnLogin.getScene().getWindow();stage.close();
            stage.getIcons().add(icon.getImage());
            stage.setTitle("Medicine Distribution Management");
            stage.setScene(new Scene(borderPane));

            stage.show(); // Display the stage

        } catch (IOException e) {
            log.error("Error",e);
        }
    }

}
