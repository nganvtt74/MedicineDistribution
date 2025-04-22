package com.example.medicinedistribution.GUI;

import com.example.medicinedistribution.BUS.BUSFactory;
import com.example.medicinedistribution.DTO.EmployeeDTO;
import com.example.medicinedistribution.DTO.PositionHistoryDTO;
import com.example.medicinedistribution.Util.NotificationUtil;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
public class EmployeeHistoryController {

    @FXML
    private BorderPane rootPane;

    @FXML
    private Label lblEmployeeName;

    @FXML
    private TableView<PositionHistoryDTO> tblHistory;

    @FXML
    private TableColumn<PositionHistoryDTO, String> colDate;

    @FXML
    private TableColumn<PositionHistoryDTO, String> colPosition;

    @FXML
    private TableColumn<PositionHistoryDTO, String> colSalaryBefore;

    @FXML
    private TableColumn<PositionHistoryDTO, String> colSalaryAfter;

    @FXML
    private HBox headerModule;

    private boolean removeHeader = false;

    private final BUSFactory busFactory;
    private final EmployeeDTO employee;

    public EmployeeHistoryController(BUSFactory busFactory, EmployeeDTO employee) {
        this.busFactory = busFactory;
        this.employee = employee;
    }

    public EmployeeHistoryController(BUSFactory busFactory, EmployeeDTO employee, boolean removeHeader) {
        this(busFactory, employee);
        this.removeHeader = removeHeader;

    }

    public void initialize() {
        // Set the employee name in the header
        lblEmployeeName.setText("Lịch sử chức vụ: " + employee.getFirstName() + " " + employee.getLastName());

        // Configure table columns
        setupTableColumns();

        // Load position history data
        loadHistoryData();

        Platform.runLater(() -> {
            // Set the header module to null if removeHeader is true
            if (removeHeader) {
                headerModule.getChildren().clear();
            }
        });

    }

    private void setupTableColumns() {
        // Format date column
        colDate.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getDate();
            if (date != null) {
                return new SimpleStringProperty(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
            return new SimpleStringProperty("N/A");
        });

        // Position column
        colPosition.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getPositionName()));

        // Format salary columns with thousand separators
        colSalaryBefore.setCellValueFactory(cellData -> {
            BigDecimal salary = cellData.getValue().getSalaryBefore();
            if (salary != null) {
                return new SimpleStringProperty(String.format("%,d", salary.longValue()));
            }
            return new SimpleStringProperty("N/A");
        });

        colSalaryAfter.setCellValueFactory(cellData -> {
            BigDecimal salary = cellData.getValue().getSalaryAfter();
            if (salary != null) {
                return new SimpleStringProperty(String.format("%,d", salary.longValue()));
            }
            return new SimpleStringProperty("N/A");
        });
    }

    private void loadHistoryData() {
        try {
            List<PositionHistoryDTO> historyList = busFactory.getEmployeeBUS().findHistoryByEmployeeId(employee.getEmployeeId());
            tblHistory.setItems(FXCollections.observableArrayList(historyList));

            if (historyList.isEmpty()) {
                // You might want to show a message in the table or elsewhere
                log.info("No position history found for employee: {}", employee.getEmployeeId());
            }
        } catch (Exception e) {
            log.error("Error loading position history: ", e);
            NotificationUtil.showErrorNotification("Lỗi", "Không thể tải lịch sử chức vụ: " + e.getMessage());
        }
    }

    /**
     * Static method to display the position history for an employee
     *
     * @param busFactory The BUS factory instance
     * @param employee The employee to display history for
     */
    public static void show(BUSFactory busFactory, EmployeeDTO employee) {
        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(EmployeeHistoryController.class.getResource("EmployeeHistory.fxml"));

            // Set the controller with the required parameters
            EmployeeHistoryController controller = new EmployeeHistoryController(busFactory, employee);
            loader.setController(controller);

            // Load the scene
            Scene scene = new Scene(loader.load());

            // Create and configure the stage
            Stage stage = new Stage();
            stage.setTitle("Lịch sử chức vụ nhân viên");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(true);

            // Show the stage
            stage.showAndWait();
        } catch (IOException e) {
            log.error("Error showing position history window: ", e);
            NotificationUtil.showErrorNotification("Lỗi", "Không thể hiển thị lịch sử chức vụ: " + e.getMessage());
        }
    }

    public static BorderPane getHistoryPane(BUSFactory busFactory, EmployeeDTO employee) {
        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(EmployeeHistoryController.class.getResource("EmployeeHistory.fxml"));

            // Set the controller with the required parameters
            EmployeeHistoryController controller = new EmployeeHistoryController(busFactory, employee,true);
            loader.setController(controller);

            BorderPane pane = loader.load();

            // Load and return the pane

            // Additional initialization if needed
            return pane;
        } catch (IOException e) {
            log.error("Error loading position history pane: ", e);
            NotificationUtil.showErrorNotification("Lỗi", "Không thể tạo bảng lịch sử chức vụ: " + e.getMessage());

            // Return an empty pane if there's an error
            return new BorderPane();
        }
    }
}