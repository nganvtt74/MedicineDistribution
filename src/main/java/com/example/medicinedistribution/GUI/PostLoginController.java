package com.example.medicinedistribution.GUI;

import com.example.medicinedistribution.BUS.BUSFactory;
import com.example.medicinedistribution.BUS.Interface.RequestBUS;
import com.example.medicinedistribution.BUS.Interface.RequestTypeBUS;
import com.example.medicinedistribution.DTO.ComponentInfo;
import com.example.medicinedistribution.DTO.RequestTypeDTO;
import com.example.medicinedistribution.DTO.RequestsDTO;
import com.example.medicinedistribution.Util.NotificationUtil;
import com.example.medicinedistribution.Util.PdfExportUtils;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Slf4j
public class PostLoginController {
    @FXML
    private ImageView imgHumanResources;

    @FXML
    private ImageView imgSales;

    @FXML
    private ImageView imgSystemManagement;

    @FXML
    private VBox humanResourcesVBox;

    @FXML
    private VBox salesManagementVBox;

    @FXML
    private VBox systemManagementVBox;

    @FXML
    private Label lblDateTime;

    @FXML
    private Label lblEmployeeName;

    @FXML
    private Label lblTitle;


    @FXML private HBox btnGroup;
    @FXML private Button btnSalary;
    @FXML private Button btnCreateRequest;
    @FXML private Button btnLogout;
    @FXML private VBox content;


    private final BUSFactory busFactory;
    private final ArrayList<ComponentInfo> componentInfoList;
    HashMap<String,Object> controllerMap = new HashMap<>();
    String[] permissionNames = {"HUMAN_RESOURCES", "SALES_MANAGEMENT", "SYSTEM_MANAGEMENT"};

    public PostLoginController(BUSFactory busFactory, ArrayList<ComponentInfo> componentInfoList) {
        // Constructor to initialize the controller with the BUSFactory
        // You can use this factory to access your business logic
        this.busFactory = busFactory;
        this.componentInfoList = componentInfoList;
    }

    @FXML
    public void initialize() {
        checkPermissionToDisableComponents();
        lblEmployeeName.setText("Xin chào: " + busFactory.getUserSession().getEmployee().getFullName());
        setupDateTimeUpdater(); // Call the new method to start the timer
        setupAction();
        if (busFactory.getUserSession().hasPermission("ADMIN")) {
            System.out.println("Admin permission granted");
            content.getStyleClass().remove("dashboard-background");
            content.getStyleClass().add("admin-background");
            System.out.println(content.getStyleClass());
        }
    }

 private boolean isProcessingClick = false;

 private void setupAction() {
     // Set up VBox click handlers to forward events to images
     humanResourcesVBox.setOnMouseClicked(event -> {
         if (!isProcessingClick && !imgHumanResources.isDisabled()) {
             isProcessingClick = true;
             openModule("Human-Resources.fxml", "HUMAN_RESOURCES", new HumanResourcesController(busFactory,componentInfoList.size()));
         }
     });
     salesManagementVBox.setOnMouseClicked(event -> {
         if (!isProcessingClick && !imgSales.isDisabled()) {
             isProcessingClick = true;
             openModule("Sales-Management.fxml", "SALES_MANAGEMENT", new SalesManagementController(busFactory,componentInfoList.size()));
         }
     });
     systemManagementVBox.setOnMouseClicked(event -> {
         if (!isProcessingClick && !imgSystemManagement.isDisabled()) {
             isProcessingClick = true;
             openModule("System-Management.fxml", "SYSTEM_MANAGEMENT",new SystemManagementController(busFactory,componentInfoList.size()));
         }
     });

     lblTitle.setOnMouseClicked(event -> {
            if (!isProcessingClick) {
                isProcessingClick = true;
                openModule("EmployeeAttendance.fxml", "ADMIN", new EmployeeAttendanceController(busFactory));
            }
     });

        btnSalary.setOnMouseClicked(event -> {
            btnSalaryHandle();
        });

        btnLogout.setOnMouseClicked(event -> {
            Stage currentStage = (Stage) btnLogout.getScene().getWindow();
            currentStage.close();
            busFactory.getUserSession().clearSession();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
            loader.setController(new LoginController(busFactory));
            try {
                Parent root = loader.load();
                Stage newStage = new Stage();
                newStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("../../../../img/logo.png")).toExternalForm()));
                newStage.setTitle("Medicine Distribution Management");
                newStage.setScene(new Scene(root));
                newStage.setResizable(false);
                newStage.show();
            } catch (IOException e) {
                log.error("Failed to load Login.fxml", e);
            }
        });

     btnCreateRequest.setOnAction(event -> handleCreateRequest());

 }

    private void btnSalaryHandle() {
        SalaryExportDialog.show(busFactory, btnSalary.getScene().getWindow());

    }

    private void openModule(String fxmlPath, String permissionName,Object controller) {
     try {
         // Find matching component info to get controller
         try {
             FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));

             if (controller instanceof ManagementController) {
                 Stage currentStage = (Stage) lblEmployeeName.getScene().getWindow();
                 ((ManagementController) controller).setBackFunction(() -> {
                     currentStage.show();
                     // Get the current active window and close it
                     for (Window stage : javafx.stage.Window.getWindows().filtered(window ->
                             window instanceof Stage && window.isShowing() && window != currentStage)) {
                         ((Stage) stage).close();
                         break;
                     }
                 });
             }

             loader.setController(controller);
             Parent root = loader.load();
             ImageView icon = new ImageView(new Image(Objects.requireNonNull(getClass().getResource("../../../../img/logo.png")).toExternalForm()));

             Stage currentStage = (Stage) lblEmployeeName.getScene().getWindow();
             Scene scene = new Scene(root);
             Stage newStage = new Stage();
             newStage.setTitle("Medicine Distribution Management");
             newStage.getIcons().add(icon.getImage());
             newStage.setScene(scene);
             newStage.setResizable(false);
             newStage.show();
             currentStage.hide();




         } catch (IOException e) {
             log.error("Failed to open module: {}", fxmlPath, e);
         }
     } finally {
         // Reset the flag whether the operation succeeds or fails
         isProcessingClick = false;


     }
 }

    public void checkPermissionToDisableComponents() {
        // Check the permission of the user and disable components accordingly
        for (String componentInfo : permissionNames) {
            boolean hasPermission = busFactory.getUserSession().hasPermission(componentInfo);
            if (!hasPermission) {
                switch (componentInfo) {
                    case "HUMAN_RESOURCES":
                        imgHumanResources.setDisable(true);
                        humanResourcesVBox.setDisable(true);
                        humanResourcesVBox.getStyleClass().remove("module-box");
                        humanResourcesVBox.getStyleClass().add("module-box-disabled");
                        break;
                    case "SALES_MANAGEMENT":
                        imgSales.setDisable(true);
                        salesManagementVBox.setDisable(true);
                        salesManagementVBox.getStyleClass().remove("module-box");
                        salesManagementVBox.getStyleClass().add("module-box-disabled");
                        break;
                    case "SYSTEM_MANAGEMENT":
                        imgSystemManagement.setDisable(true);
                        systemManagementVBox.setDisable(true);
                        systemManagementVBox.getStyleClass().remove("module-box");
                        systemManagementVBox.getStyleClass().add("module-box-disabled");
                        break;
                    default:
                        log.warn("Unknown component: {}", componentInfo);
                }
            }
        }

//        if (!busFactory.getUserSession().hasPermission("INSERT_REQUEST")) {
//            btnGroup.getChildren().remove(btnCreateRequest);
//        }
//        if (!busFactory.getUserSession().hasPermission("EXPORT_SALARY")) {
//            btnGroup.getChildren().remove(btnSalary);
//        }


    }
    private void setupDateTimeUpdater() {
        // Create a timeline that triggers every second
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(0),
                        event -> updateDateTime()),
                new KeyFrame(Duration.seconds(1))
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void updateDateTime() {
        // Format current date and time
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm:ss");
        String formattedDateTime = now.format(formatter);

        // Update the label
        lblDateTime.setText("Ngày: " + formattedDateTime);
    }
   private void handleCreateRequest() {
        RequestCreationDialog.show(busFactory, btnCreateRequest.getScene().getWindow());
    }
}
