package com.example.medicinedistribution.GUI;

import com.example.medicinedistribution.BUS.BUSFactory;
import com.example.medicinedistribution.BUS.Interface.DependentsBUS;
import com.example.medicinedistribution.BUS.Interface.PositionBUS;
import com.example.medicinedistribution.DTO.DependentsDTO;
import com.example.medicinedistribution.DTO.EmployeeDTO;
import com.example.medicinedistribution.DTO.PositionDTO;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Slf4j
public class EmployeeDetailsController {

    @FXML private BorderPane masterLayout;
    @FXML private GridPane personalInfoGrid;
    @FXML private GridPane employmentInfoGrid;
    @FXML private TableView<DependentsDTO> relativesTable;
    @FXML private TableColumn<DependentsDTO, String> nameColumn;
    @FXML private TableColumn<DependentsDTO, String> relationColumn;
    @FXML private TableColumn<DependentsDTO, String> birthdayColumn;
    @FXML private StackPane historyContainer;
    @FXML private Button closeBtn;

    private final BUSFactory busFactory;
    private final EmployeeDTO employee;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public EmployeeDetailsController(BUSFactory busFactory, EmployeeDTO employee) {
        this.busFactory = busFactory;
        this.employee = employee;
    }

    @FXML
    public void initialize() {
        // Set up table columns
        setupTableColumns();

        // Load employee data
        loadEmployeeData();

        // Load relatives data
        loadRelativesData();

        // Load position history
        loadPositionHistory();

        // Set up close button action
        closeBtn.setOnAction(e -> ((Stage) masterLayout.getScene().getWindow()).close());
    }

    private void setupTableColumns() {
        // Name column combines first and last names
        nameColumn.setCellValueFactory(cellData ->
            Bindings.createStringBinding(() ->
                cellData.getValue().getFirstName() + " " + cellData.getValue().getLastName()));

        // Relation column
        relationColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getRelationship()));

        // Format the birthday column
        birthdayColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getBirthday() != null ?
                cellData.getValue().getBirthday().format(dateFormatter) : "N/A"));
    }

    private void loadEmployeeData() {
        try {
            // Get position information
            PositionBUS positionBUS = busFactory.getPositionBUS();
            PositionDTO position = positionBUS.findById(employee.getPositionId());

            // Add personal information
            int row = 0;
            addDetailRow(personalInfoGrid, "Mã nhân viên:", employee.getEmployeeId().toString(), row++);
            addDetailRow(personalInfoGrid, "Họ và tên:", employee.getFirstName() + " " + employee.getLastName(), row++);
            addDetailRow(personalInfoGrid, "Ngày sinh:", employee.getBirthday().format(dateFormatter), row++);
            addDetailRow(personalInfoGrid, "Giới tính:", employee.getGender(), row++);
            addDetailRow(personalInfoGrid, "Số điện thoại:", employee.getPhone(), row++);
            addDetailRow(personalInfoGrid, "Email:", employee.getEmail() != null ? employee.getEmail() : "", row++);
            addDetailRow(personalInfoGrid, "Địa chỉ:", employee.getAddress(), row++);

            // Add employment information
            row = 0;
            addDetailRow(employmentInfoGrid, "Chức vụ:", position != null ? position.getPositionName() : "N/A", row++);
            addDetailRow(employmentInfoGrid, "Phòng ban:", position != null ? "Phòng " + position.getDepartmentId() : "N/A", row++);
            addDetailRow(employmentInfoGrid, "Ngày thuê:", employee.getHireDate().format(dateFormatter), row++);
            addDetailRow(employmentInfoGrid, "Lương cơ bản:", String.format("%,.0f VND", employee.getBasicSalary()), row++);

            String statusText = employee.getStatus() == 1 ? "Đang làm việc" : "Đã nghỉ việc";
            String statusClass = employee.getStatus() == 1 ? "status-active" : "status-inactive";

            Label statusLabel = new Label(statusText);
            statusLabel.getStyleClass().add(statusClass);

            addDetailRow(employmentInfoGrid, "Trạng thái:", statusLabel, row);

        } catch (Exception e) {
            log.error("Error loading employee data: ", e);
        }
    }

    private void loadRelativesData() {
        try {
            DependentsBUS dependentsBUS = busFactory.getDependentsBUS();
            List<DependentsDTO> dependents = dependentsBUS.findByEmployeeId(employee.getEmployeeId());
            relativesTable.setItems(FXCollections.observableArrayList(dependents));

            if (dependents.isEmpty()) {
                relativesTable.setPlaceholder(new Label("Không có thông tin thân nhân"));
            }
        } catch (Exception e) {
            log.error("Error loading relatives data: ", e);
            relativesTable.setItems(FXCollections.observableArrayList());
            relativesTable.setPlaceholder(new Label("Không thể tải dữ liệu thân nhân"));
        }
    }

    private void loadPositionHistory() {
        try {
            // Get position history pane from EmployeeHistoryController
            BorderPane historyPane = EmployeeHistoryController.getHistoryPane(busFactory, employee);
            historyContainer.getChildren().clear();
            historyContainer.getChildren().add(historyPane);
        } catch (Exception e) {
            log.error("Error loading position history: ", e);
            Label errorLabel = new Label("Không thể tải lịch sử chức vụ");
            errorLabel.getStyleClass().add("error-label");
            historyContainer.getChildren().add(errorLabel);
        }
    }

    private void addDetailRow(GridPane grid, String labelText, String value, int row) {
        Label keyLabel = new Label(labelText);
        keyLabel.getStyleClass().add("detail-key");

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("detail-value");
        valueLabel.setWrapText(true);

        grid.add(keyLabel, 0, row);
        grid.add(valueLabel, 1, row);
    }

    private void addDetailRow(GridPane grid, String labelText, Label valueLabel, int row) {
        Label keyLabel = new Label(labelText);
        keyLabel.getStyleClass().add("detail-key");

        valueLabel.setWrapText(true);

        grid.add(keyLabel, 0, row);
        grid.add(valueLabel, 1, row);
    }

    /**
     * Static method to show the employee details dialog
     *
     * @param employee The employee to show details for
     * @param busFactory The BUS factory for database access
     */
    public static void show(EmployeeDTO employee, BUSFactory busFactory) {
        try {
            // Load the FXML file
            log.info("Loading EmployeeDetails.fxml{}", EmployeeDetailsController.class.getResource("EmployeeDetails.fxml"));
            FXMLLoader loader = new FXMLLoader(EmployeeDetailsController.class.getResource("EmployeeDetails.fxml"));

            // Set the controller with required parameters
            EmployeeDetailsController controller = new EmployeeDetailsController(busFactory, employee);
            loader.setController(controller);

            // Load the scene
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(EmployeeDetailsController.class.getResource("../../../../css/main-style.css").toExternalForm());

            // Create and configure the stage
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Chi tiết nhân viên");
            dialog.setMinWidth(1024);
            dialog.setMinHeight(700);
            dialog.initStyle(StageStyle.DECORATED);

            // Set the icon (optional)
            try {
                dialog.getIcons().add(new Image(Objects.requireNonNull(
                    EmployeeDetailsController.class.getResourceAsStream("/img/logo.png"))));
            } catch (Exception e) {
                log.error("Could not load logo", e);
            }

            dialog.setScene(scene);
            dialog.showAndWait();

        } catch (IOException e) {
            log.error("Error showing employee details: ", e);
        }
    }
}