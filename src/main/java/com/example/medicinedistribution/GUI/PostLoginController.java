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
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle("Export Salary PDF");
        dialogStage.initOwner(btnSalary.getScene().getWindow());
        dialogStage.setResizable(false);
        dialogStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("../../../../img/logo.png")).toExternalForm()));
        VBox dialogVbox = new VBox(15);
        dialogVbox.setPadding(new Insets(20));
        dialogVbox.setAlignment(Pos.CENTER);
        dialogVbox.getStyleClass().add("dialog-pane");

        Label titleLabel = new Label("Select Month and Year");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        HBox periodSelectionBox = new HBox(15);
        periodSelectionBox.setAlignment(Pos.CENTER);

        // Month ComboBox
        ComboBox<Integer> monthComboBox = new ComboBox<>();
        monthComboBox.getItems().addAll(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
        monthComboBox.setValue(LocalDateTime.now().getMonthValue());
        monthComboBox.setPromptText("Month");
        monthComboBox.setPrefWidth(100);

        // Year ComboBox
        ComboBox<Integer> yearComboBox = new ComboBox<>();
        int currentYear = LocalDateTime.now().getYear();
        for (int i = currentYear - 5; i <= currentYear; i++) {
            yearComboBox.getItems().add(i);
        }
        yearComboBox.setValue(currentYear);
        yearComboBox.setPromptText("Year");
        yearComboBox.setPrefWidth(100);

        Label monthLabel = new Label("Month:");
        Label yearLabel = new Label("Year:");

        periodSelectionBox.getChildren().addAll(monthLabel, monthComboBox, yearLabel, yearComboBox);

        // Buttons
        Button exportButton = new Button("Export");
        Button cancelButton = new Button("Cancel");

        cancelButton.setOnAction(e -> dialogStage.close());

        exportButton.setOnAction(e -> {
            Integer selectedMonth = monthComboBox.getValue();
            Integer selectedYear = yearComboBox.getValue();

            if (selectedMonth != null && selectedYear != null) {
                PdfExportUtils.exportEmployeePayrollToPdf(
                        busFactory.getUserSession().getEmployee().getEmployeeId(),
                        selectedMonth,
                        selectedYear,
                        busFactory);
                dialogStage.close();
            } else {
                NotificationUtil.showErrorNotification("Error", "Please select both month and year");
            }
        });

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(exportButton, cancelButton);

        dialogVbox.getChildren().addAll(titleLabel, periodSelectionBox, buttonBox);

        // Apply CSS
        exportButton.getStyleClass().add("button");
        cancelButton.getStyleClass().add("button");
        cancelButton.setStyle("-fx-background-color: #999999;");

        Scene dialogScene = new Scene(dialogVbox, 400, 200);
        dialogScene.getStylesheets().add(getClass().getResource("/css/dialog-style.css").toExternalForm());

        dialogStage.setScene(dialogScene);
        dialogStage.show();
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

        if (!busFactory.getUserSession().hasPermission("INSERT_REQUEST")) {
            btnGroup.getChildren().remove(btnCreateRequest);
        }
        if (!busFactory.getUserSession().hasPermission("EXPORT_SALARY")) {
            btnGroup.getChildren().remove(btnSalary);
        }


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
        try {

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle("Tạo yêu cầu mới");
            dialogStage.initOwner(btnCreateRequest.getScene().getWindow());
            dialogStage.setResizable(false);
            dialogStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("../../../../img/logo.png")).toExternalForm()));

            VBox dialogVbox = new VBox(15);
            dialogVbox.setPadding(new Insets(20));
            dialogVbox.setAlignment(Pos.TOP_CENTER);
            dialogVbox.getStyleClass().add("content-area");

            Label titleLabel = new Label("Tạo yêu cầu mới");
            titleLabel.getStyleClass().add("section-header");

            // Request type selection
            Label typeLabel = new Label("Loại yêu cầu:");
            typeLabel.getStyleClass().add("form-label");

            ComboBox<RequestTypeDTO> typeComboBox = new ComboBox<>();
            typeComboBox.setPrefWidth(250);
            RequestTypeBUS requestTypeBUS = busFactory.getRequestTypeBUS();
            List<RequestTypeDTO> requestTypes = requestTypeBUS.findAll();
            typeComboBox.getItems().addAll(requestTypes);

            if (!requestTypes.isEmpty()) {
                typeComboBox.setValue(requestTypes.getFirst());
            }

            // Custom cell factory for the request type combo box
            typeComboBox.setCellFactory(param -> new ListCell<RequestTypeDTO>() {
                @Override
                protected void updateItem(RequestTypeDTO item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getType_name());
                    }
                }
            });

            // Custom string converter for the selected value
            typeComboBox.setButtonCell(new ListCell<RequestTypeDTO>() {
                @Override
                protected void updateItem(RequestTypeDTO item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getType_name());
                    }
                }
            });

            // Date range selection
            Label startDateLabel = new Label("Ngày bắt đầu:");
            startDateLabel.getStyleClass().add("form-label");

            DatePicker startDatePicker = new DatePicker(LocalDate.now());
            startDatePicker.setPrefWidth(250);

            Label endDateLabel = new Label("Ngày kết thúc:");
            endDateLabel.getStyleClass().add("form-label");

            DatePicker endDatePicker = new DatePicker(LocalDate.now());
            endDatePicker.setPrefWidth(250);

            // Calculate duration automatically
            Label durationLabel = new Label("Thời gian (ngày): 1");
            durationLabel.getStyleClass().addAll("form-label", "label-info");

            IntegerProperty duration = new SimpleIntegerProperty(1);

            startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && endDatePicker.getValue() != null) {
                    long days = ChronoUnit.DAYS.between(newVal, endDatePicker.getValue()) + 1;
                    duration.set((int) days);
                    durationLabel.setText("Thời gian (ngày): " + duration.get());
                }
            });

            endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && startDatePicker.getValue() != null) {
                    long days = ChronoUnit.DAYS.between(startDatePicker.getValue(), newVal) + 1;
                    duration.set((int) days);
                    durationLabel.setText("Thời gian (ngày): " + duration.get());
                }
            });

            // Reason text area
            Label reasonLabel = new Label("Lý do:");
            reasonLabel.getStyleClass().add("form-label");

            TextArea reasonArea = new TextArea();
            reasonArea.setPrefWidth(250);
            reasonArea.setPrefHeight(100);
            reasonArea.setWrapText(true);

            // Submit and cancel buttons
            Button submitButton = new Button("Gửi yêu cầu");
            submitButton.getStyleClass().add("primary-button");
            submitButton.setFont(javafx.scene.text.Font.font(16));

            Button cancelButton = new Button("Hủy bỏ");
            cancelButton.getStyleClass().add("secondary-button");
            cancelButton.setFont(javafx.scene.text.Font.font(16));

            HBox buttonBox = new HBox(15);
            buttonBox.setPadding(new Insets(10, 0, 0, 0));
            buttonBox.setAlignment(Pos.CENTER);

            buttonBox.getChildren().addAll(submitButton, cancelButton);

            // Layout for form elements
            GridPane formGrid = new GridPane();
            formGrid.setHgap(15);
            formGrid.setVgap(15);
            formGrid.setAlignment(Pos.CENTER);
            formGrid.getStyleClass().add("form-section");

            formGrid.add(typeLabel, 0, 0);
            formGrid.add(typeComboBox, 1, 0);
            formGrid.add(startDateLabel, 0, 1);
            formGrid.add(startDatePicker, 1, 1);
            formGrid.add(endDateLabel, 0, 2);
            formGrid.add(endDatePicker, 1, 2);
            formGrid.add(durationLabel, 1, 3);
            formGrid.add(reasonLabel, 0, 4);
            formGrid.add(reasonArea, 1, 4);

            // Add a separator before buttons
            Separator separator = new Separator();
            separator.getStyleClass().add("divider");

            // Action handlers
            cancelButton.setOnAction(e -> dialogStage.close());

            submitButton.setOnAction(e -> {
                if (typeComboBox.getValue() == null) {
                    NotificationUtil.showErrorNotification("Lỗi", "Vui lòng chọn loại yêu cầu");
                    return;
                }

                if (startDatePicker.getValue() == null || endDatePicker.getValue() == null) {
                    NotificationUtil.showErrorNotification("Lỗi", "Vui lòng chọn ngày bắt đầu và kết thúc");
                    return;
                }

                if (duration.get() < 1) {
                    NotificationUtil.showErrorNotification("Lỗi", "Thời gian yêu cầu không hợp lệ");
                    return;
                }

                if (reasonArea.getText().trim().isEmpty()) {
                    NotificationUtil.showErrorNotification("Lỗi", "Vui lòng nhập lý do");
                    return;
                }

                // Create and save the request
                RequestsDTO request = RequestsDTO.builder()
                        .type_id(typeComboBox.getValue().getType_id())
                        .start_date(startDatePicker.getValue())
                        .end_date(endDatePicker.getValue())
                        .duration(duration.get())
                        .employee_id(busFactory.getUserSession().getEmployee().getEmployeeId())
                        .reason(reasonArea.getText().trim())
                        .status("PENDING")
                        .created_at(LocalDate.now())
                        .build();

                try {
                    RequestBUS requestBUS = busFactory.getRequestBUS();
                    boolean success = requestBUS.insert(request);
                    if (success) {
                        NotificationUtil.showSuccessNotification("Thành công", "Yêu cầu đã được gửi thành công");
                        dialogStage.close();
                    } else {
                        NotificationUtil.showErrorNotification("Lỗi", "Không thể gửi yêu cầu");
                    }
                } catch (Exception ex) {
                    log.error("Error submitting request", ex);
                    NotificationUtil.showErrorNotification("Lỗi", "Không thể gửi yêu cầu: " + ex.getMessage());
                }
            });

            dialogVbox.getChildren().addAll(titleLabel, formGrid, separator, buttonBox);
            Scene dialogScene = new Scene(dialogVbox, 450, 500);
            dialogScene.getStylesheets().add(getClass().getResource("../../../../css/main-style.css").toExternalForm());

            dialogStage.setScene(dialogScene);
            dialogStage.show();
        } catch (Exception e) {
            log.error("Error opening request form", e);
            NotificationUtil.showErrorNotification("Lỗi", "Không thể mở form tạo yêu cầu: " + e.getMessage());
        }
    }
}
