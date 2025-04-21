package com.example.medicinedistribution.GUI;

import com.example.medicinedistribution.BUS.BUSFactory;
import com.example.medicinedistribution.BUS.Interface.AuthBUS;
import com.example.medicinedistribution.DTO.ComponentInfo;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
                checkPermission();

            }
        } catch (RuntimeException e) {
            NotificationUtil.showErrorNotification("Đăng nhập thất bại", e.getMessage());
            txtPassword.requestFocus();
        }

        // Clear the fields after login attempt

    }

    public void checkPermission() {
        ArrayList<ComponentInfo> componentInfoList = new ArrayList<>();
        componentInfoList.add(new ComponentInfo( "SALES_MANAGEMENT", "Sales-Management.fxml"));
        componentInfoList.add(new ComponentInfo("HUMAN_RESOURCES", "Human-Resources.fxml"));
        componentInfoList.add(new ComponentInfo("SYSTEM_MANAGEMENT", "System-Management.fxml"));


        int permissionCount = 0;

        // Create a copy or use an iterator-safe approach
        List<ComponentInfo> toRemove = new ArrayList<>();

        for (ComponentInfo componentInfo : componentInfoList) {
            if (busFactory.getUserSession().hasPermission(componentInfo.getPermission())) {
                permissionCount++;
            } else {
                toRemove.add(componentInfo);
            }
        }

        // Remove the items after iteration is complete
        componentInfoList.removeAll(toRemove);

        if (permissionCount == 0) {
            NotificationUtil.showErrorNotification("Lỗi", "Bạn không có quyền truy cập vào hệ thống này");
            Stage stage = (Stage) btnLogin.getScene().getWindow();
            stage.close();
        } else if (permissionCount == 1) {
            for (ComponentInfo componentInfo : componentInfoList) {
                if (busFactory.getUserSession().hasPermission(componentInfo.getPermission())) {
                    switch (componentInfo.getPermission()) {
                        case "SALES_MANAGEMENT" ->
                                openStage(componentInfo.getFxmlPath(), new SalesManagementController(busFactory, permissionCount));
                        case "HUMAN_RESOURCES" ->
                                openStage(componentInfo.getFxmlPath(), new HumanResourcesController(busFactory));
                        case "SYSTEM_MANAGEMENT" ->
                                openStage(componentInfo.getFxmlPath(), new SystemManagementController(busFactory));
                        default -> {
                            // Handle unknown permission case
                            NotificationUtil.showErrorNotification("Lỗi", "Bạn không có quyền truy cập vào hệ thống này");
                            Stage stage = (Stage) btnLogin.getScene().getWindow();
                            stage.close();
                        }
                    }
                }
            }
        } else if (permissionCount > 1) {
            openStage("Post-Login.fxml", new PostLoginController(busFactory , componentInfoList));
        }

    }





    public void openStage(String fxmlFile,Object controller) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(LoginController.class.getResource(fxmlFile));
            loader.setController(controller); // Set the controller for the new stage
            Parent root = loader.load();
            AnchorPane anchorPane = (AnchorPane) root;
            ImageView icon = new ImageView(new Image(Objects.requireNonNull(getClass().getResource("../../../../img/logo.png")).toExternalForm()));
            Stage stage = (Stage) btnLogin.getScene().getWindow();stage.close();
            stage.getIcons().add(icon.getImage());
            stage.setTitle("Medicine Distribution Management");
            stage.setScene(new Scene(anchorPane));

            stage.show(); // Display the stage

        } catch (IOException e) {
            log.error("Error",e);
        }
    }

}
