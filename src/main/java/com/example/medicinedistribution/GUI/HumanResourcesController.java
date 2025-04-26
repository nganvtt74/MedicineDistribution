package com.example.medicinedistribution.GUI;

import com.example.medicinedistribution.BUS.BUSFactory;
import com.example.medicinedistribution.DTO.ComponentInfo;
import com.example.medicinedistribution.DTO.UserSession;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class HumanResourcesController extends ManagementController {
    //UI fields
    @FXML
    public Label headerUserName;

    @FXML
    public StackPane logoContainer;

    @FXML
    public VBox button_group_vbox,button_group_vbox_2;

    @FXML
    public Button btnEmployee,
            btnDepartment,
            btnAttendance,
            btnBenefits,
            btnPayroll,
            btnRequest,
            btnSettings,
            btnBack;

    @FXML
    public StackPane contentArea;

    @FXML
    public VBox parentVBOX;

    @FXML
    public Label lblStatistic;

    private Button activeButton;

    //Functional fields
    private UserSession userSession;
    private List<ComponentInfo> componentInfoList;
    private final int permissionCount;

    public HumanResourcesController(BUSFactory busFactory, int permissionCount) {
        super(busFactory);
        this.permissionCount = permissionCount;
    }

    public void initialize() {
        userSession = busFactory.getUserSession();
        setup();
        setupButtonMap();
        loadDashboard();
        if (permissionCount > 1) {
            setupBtnBack();
        } else {
            btnBack.setVisible(false);
            btnBack.setManaged(false);
        }
    }

    @Override
    public void setup() {
        headerUserName.setText(userSession.getAccount().getUsername());
    }

    @Override
    public void setupButtonMap() {
        componentInfoList = new ArrayList<>();
        componentInfoList.add(new ComponentInfo(btnEmployee, "MANAGE_EMPLOYEE", "Employee.fxml", new EmployeeController(busFactory)));
        componentInfoList.add(new ComponentInfo(btnDepartment, "MANAGE_DEPARTMENT", "Department.fxml", new DepartmentController(busFactory)));
        componentInfoList.add(new ComponentInfo(btnAttendance, "MANAGE_ATTENDANCE", "AttendanceManagement.fxml", new AttendanceManagementController(busFactory)));
//        componentInfoList.add(new ComponentInfo(btnStatistics, "HR_STATISTIC", "HRStatistic.fxml", new HRStatisticController(busFactory)));
        boolean defaultLoad = false;

        for (ComponentInfo componentInfo : componentInfoList) {
            if (!userSession.hasPermission(componentInfo.getPermission())) {
                button_group_vbox.getChildren().remove(componentInfo.getButton());
            } else {
                componentInfo.getButton().setOnAction(event -> {
                    String fxmlFile = componentInfo.getFxmlPath();
                    if (fxmlFile != null) {
                        loadFxml(fxmlFile, componentInfo.getController());
                    }
                });
                if (!defaultLoad) {
                    loadFxml(componentInfo.getFxmlPath(), componentInfo.getController());
                    defaultLoad = true;
                }
            }
        }
        btnSettings.setOnAction(event -> {
            // Handle settings button click
            loadFxml("settingHR.fxml", new SettingsHRController(busFactory));
        });

        if (userSession.hasPermission("MANAGE_BENEFITS")) {
            btnBenefits.setOnAction(event -> {
                // Handle benefits button click
                loadFxml("Benefits.fxml", new BenefitsController(busFactory));
            });
        }else {
            button_group_vbox_2.getChildren().remove(btnBenefits);
        }

        if (userSession.hasPermission("MANAGE_PAYROLL")) {
            btnPayroll.setOnAction(event -> {
                // Handle salary button click
                loadFxml("Payroll.fxml", new PayRollController(busFactory));
            });
        }else {
            button_group_vbox_2.getChildren().remove(btnPayroll);
        }
        if (userSession.hasPermission("MANAGE_REQUEST")) {
            btnRequest.setOnAction(event -> {
                // Handle request button click
                loadFxml("Request.fxml", new RequestController(busFactory));
            });
        }else {
            button_group_vbox_2.getChildren().remove(btnRequest);
        }

        logoContainer.setOnMouseClicked(event -> {
            // Handle logo click event
            loadDashboard();
        });

        btnLogout.setOnAction(event -> {
            logout();
        });
    }

    public void loadDashboard() {
//        try {
//            logoContainer.setDisable(true);
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("HRDashboard.fxml"));
//            loader.setController(new HRDashboardController(busFactory));
//            AnchorPane dashboard = loader.load();
//            contentArea.getChildren().clear();
//            contentArea.getChildren().add(dashboard);
//        } catch (IOException e) {
//            log.error("Error loading HR Dashboard: ", e);
//        }
    }

    public void loadFxml(String fxmlFile, Object controller) {
        try {
            if (activeButton != null) {
                // Remove active class from previous button
                activeButton.getStyleClass().remove("active-button");
                // Re-enable the previous active button
                activeButton.setDisable(false);
            }

            // Get and update the currently clicked button
            Button clickedButton = null;
            for (ComponentInfo info : componentInfoList) {
                if (info.getFxmlPath().equals(fxmlFile)) {
                    clickedButton = info.getButton();
                    break;
                }
            }

            if (clickedButton != null) {
                // Set the new button as active
                clickedButton.getStyleClass().add("active-button");
                // Disable it temporarily to prevent double-clicking
                clickedButton.setDisable(true);
                activeButton = clickedButton;
            }

            contentArea.getChildren().clear();

            LoadingOverlay loadingOverlay = new LoadingOverlay();
            contentArea.getChildren().add(loadingOverlay);

            // Load the content in a background thread
            try {
                // Create a new FXMLLoader
                FXMLLoader loader = new FXMLLoader();
                // Set the URL for the FXMLLoader to point to the new FXML file
                loader.setLocation(getClass().getResource(fxmlFile));
                loader.setController(controller);

                // Load the FXML content
                AnchorPane newContent = loader.load();

                // Simulate some loading time if needed
                Thread.sleep(100);

                // Update UI on JavaFX application thread
                Platform.runLater(() -> {
                    // Add the new content behind the overlay
                    contentArea.getChildren().addFirst(newContent);

                    // Hide loading overlay with smooth transition
                    loadingOverlay.hide();
                    logoContainer.setDisable(false);
                });
            } catch (Exception e) {
                log.error("Error loading FXML: ", e);
                Platform.runLater(() -> {
                    // Show error message in UI
                    Label errorLabel = new Label("Không thể tải giao diện: " + e.getMessage());
                    errorLabel.getStyleClass().add("error-label");
                    contentArea.getChildren().clear();
                    contentArea.getChildren().add(errorLabel);
                });
            }
        } catch (ClassCastException e) {
            log.error("ClassCastException: ", e);
        }
    }
}