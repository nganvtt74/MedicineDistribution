package com.example.medicinedistribution.GUI;

import com.example.medicinedistribution.BUS.BUSFactory;
import com.example.medicinedistribution.BUS.Interface.DependentsBUS;
import com.example.medicinedistribution.DTO.DependentsDTO;
import com.example.medicinedistribution.DTO.EmployeeDTO;
import com.example.medicinedistribution.Exception.DeleteFailedException;
import com.example.medicinedistribution.GUI.SubAction.DependentAction;
import com.example.medicinedistribution.GUI.SubAction.SubAction;
import com.example.medicinedistribution.Util.NotificationUtil;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
public class DependentsController {
    @FXML private BorderPane mainPane;
    @FXML private Label lblEmployeeId;
    @FXML private Label lblEmployeeName;
    @FXML private TableView<DependentsDTO> tblDependents;
    @FXML private TableColumn<DependentsDTO, String> colFirstName;
    @FXML private TableColumn<DependentsDTO, String> colLastName;
    @FXML private TableColumn<DependentsDTO, String> colFullName;
    @FXML private TableColumn<DependentsDTO, String> colBirthday;
    @FXML private TableColumn<DependentsDTO, String> colRelationship;
    @FXML private Button btnAdd;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;
    @FXML private Button btnRefresh;
    @FXML private Button btnClose;

    @Getter
    private final EmployeeDTO employee;
    private final BUSFactory busFactory;
    private final DependentsBUS dependentsBUS;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public DependentsController(EmployeeDTO employee, BUSFactory busFactory) {
        this.employee = employee;
        this.busFactory = busFactory;
        this.dependentsBUS = busFactory.getDependentsBUS();
    }

    @FXML
    public void initialize() {
        // Set employee info
        lblEmployeeId.setText(String.valueOf(employee.getEmployeeId()));
        lblEmployeeName.setText(employee.getFirstName() + " " + employee.getLastName());

        // Configure table columns
        setupTableColumns();

        // Load data
        refreshData();

        // Set up button actions
        setupButtonActions();
    }

    private void setupTableColumns() {


        colFirstName.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFirstName()));

        colLastName.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getLastName()));

        // Full name is a combination of first and last names
        colFullName.setCellValueFactory(cellData ->
                Bindings.createStringBinding(() ->
                    cellData.getValue().getFirstName() + " " + cellData.getValue().getLastName()));

        // Format birthday
        colBirthday.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getBirthday() != null ?
                        cellData.getValue().getBirthday().format(dateFormatter) : "N/A"));

        colRelationship.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getRelationship()));
    }

    private void setupButtonActions() {
        btnAdd.setOnAction(event -> handleAddDependent());
        btnEdit.setOnAction(event -> handleEditDependent());
        btnDelete.setOnAction(event -> handleDeleteDependent());
        btnRefresh.setOnAction(event -> refreshData());
        btnClose.setOnAction(event -> ((Stage) mainPane.getScene().getWindow()).close());
    }

    private void handleAddDependent() {
        try {
            // Show dialog to add dependent
            DependentAction.showDialog(
                 busFactory, this, SubAction.ActionType.ADD, employee.getEmployeeId());

        } catch (Exception e) {
            log.error("Error adding dependent: ", e);
            NotificationUtil.showErrorNotification("Lỗi", "Không thể thêm thân nhân: " + e.getMessage());
        }
    }

    private void handleEditDependent() {
        try {
            DependentsDTO selected = tblDependents.getSelectionModel().getSelectedItem();
            if (selected == null) {
                NotificationUtil.showErrorNotification("Cảnh báo", "Vui lòng chọn thân nhân để chỉnh sửa");
                return;
            }
            DependentAction.showDialog(
                busFactory, this, SubAction.ActionType.EDIT, selected, employee.getEmployeeId());

        } catch (Exception e) {
            log.error("Error editing dependent: ", e);
            NotificationUtil.showErrorNotification("Lỗi", "Không thể chỉnh sửa thân nhân: " + e.getMessage());
        }
    }

    private void handleDeleteDependent() {
        try {
            DependentsDTO selected = tblDependents.getSelectionModel().getSelectedItem();
            if (selected == null) {
                NotificationUtil.showErrorNotification("Cảnh báo", "Vui lòng chọn thân nhân để xóa");
                return;
            }

            boolean confirmed = NotificationUtil.showConfirmation("Xác nhận",
                    "Bạn có chắc chắn muốn xóa thân nhân này?");

            if (confirmed) {
                boolean success = dependentsBUS.delete(selected.getEmployeeId(), selected.getDependentNo());

                if (success) {
                    refreshData();
                    NotificationUtil.showSuccessNotification("Thành công", "Xóa thân nhân thành công");
                } else {
                    NotificationUtil.showErrorNotification("Cảnh báo", "Không thể xóa thân nhân");
                }
            }
        } catch (Exception e) {
            log.error("Error deleting dependent: ", e);
            NotificationUtil.showErrorNotification("Lỗi", "Không thể xóa thân nhân: " + e.getMessage());
        }
    }

    public void refreshData() {
        try {
            List<DependentsDTO> dependentsList = dependentsBUS.findByEmployeeId(employee.getEmployeeId());
            ObservableList<DependentsDTO> observableList = FXCollections.observableArrayList(dependentsList);
            tblDependents.setItems(observableList);

            if (dependentsList.isEmpty()) {
                tblDependents.setPlaceholder(new Label("Không có thân nhân nào"));
            }
        } catch (Exception e) {
            log.error("Error loading dependents data: ", e);
            tblDependents.setPlaceholder(new Label("Không thể tải dữ liệu thân nhân"));
        }
    }

    /**
     * Display form to add/edit dependent information
     * Note: You would need to implement DependentFormController
     */
//    private boolean showDependentForm(DependentsDTO dependent, boolean isNew) {
//
//        return true;
//    }

    /**
     * Static method to show dependent management dialog
     */
    public static void show(EmployeeDTO employee, BUSFactory busFactory) {
        try {
            // Load FXML
            FXMLLoader loader = new FXMLLoader(
                DependentsController.class.getResource("Dependents.fxml"));

            // Set controller
            DependentsController controller = new DependentsController(employee, busFactory);
            loader.setController(controller);

            // Create scene
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(
                DependentsController.class.getResource("../../../../css/main-style.css").toExternalForm());

            // Create and configure stage
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Quản lý thân nhân - " + employee.getFirstName() + " " + employee.getLastName());
            stage.setMinWidth(800);
            stage.setMinHeight(600);
            stage.initStyle(StageStyle.DECORATED);

            // Set icon
            try {
                stage.getIcons().add(new Image(Objects.requireNonNull(
                    DependentsController.class.getResourceAsStream("/img/logo.png"))));
            } catch (Exception e) {
                log.error("Could not load logo", e);
            }

            stage.setScene(scene);
            stage.showAndWait();

        } catch (IOException e) {
            log.error("Error showing dependents dialog: ", e);
            NotificationUtil.showErrorNotification("Lỗi", "Không thể hiển thị quản lý thân nhân: " + e.getMessage());
        }
    }
}