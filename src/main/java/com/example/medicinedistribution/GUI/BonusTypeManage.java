package com.example.medicinedistribution.GUI;

import com.example.medicinedistribution.BUS.BUSFactory;
import com.example.medicinedistribution.BUS.Interface.BonusTypeBUS;
import com.example.medicinedistribution.DTO.BonusTypeDTO;
import com.example.medicinedistribution.Exception.PermissionDeniedException;
import com.example.medicinedistribution.Util.NotificationUtil;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
public class BonusTypeManage {
    private final BUSFactory busFactory;
    private final BonusTypeBUS bonusTypeBUS;
    private final SettingsHRController parentController;
    private ObservableList<BonusTypeDTO> bonusTypes;
    private TableView<BonusTypeDTO> tblBonusTypes;

    public BonusTypeManage(BUSFactory busFactory, SettingsHRController parentController) {
        this.busFactory = busFactory;
        this.bonusTypeBUS = busFactory.getBonusTypeBUS();
        this.parentController = parentController;
    }

    public static void showDialog(BUSFactory busFactory, SettingsHRController parentController) {
        BonusTypeManage manager = new BonusTypeManage(busFactory, parentController);
        manager.show();
    }

    private void show() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UTILITY);
        stage.setTitle("Quản lý loại thưởng");
        stage.setMinWidth(600);
        stage.setMinHeight(500);

        VBox root = createContent();
        Scene scene = new Scene(root);

        // Add CSS if needed
         scene.getStylesheets().add(getClass().getResource("../../../../css/main-style.css").toExternalForm());

        stage.setScene(scene);
        stage.show();

        loadData();
    }

    private VBox createContent() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        // Header
        Label lblHeader = new Label("Quản lý loại thưởng");
        lblHeader.setFont(new Font("System", 18));
        lblHeader.getStyleClass().add("heading-label");

        HBox headerBar = new HBox();
        headerBar.setAlignment(Pos.CENTER);
        headerBar.setPadding(new Insets(10));
        headerBar.getChildren().add(lblHeader);

        // Search and action buttons
        HBox actionBar = new HBox(10);
        actionBar.setAlignment(Pos.CENTER_LEFT);

        TextField txtSearch = new TextField();
        txtSearch.setPromptText("Tìm kiếm loại thưởng...");
        txtSearch.setPrefWidth(250);

        Button btnAdd = new Button("Thêm mới");
        btnAdd.getStyleClass().add("primary-button");
        btnAdd.setStyle("-fx-font-size: 15;");
        Button btnEdit = new Button("Chỉnh sửa");
        btnEdit.getStyleClass().add("secondary-button");
        btnEdit.setStyle("-fx-font-size: 15;");
        Button btnDelete = new Button("Xóa");
        btnDelete.getStyleClass().add("danger-button");
        btnDelete.setStyle("-fx-font-size: 15;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        actionBar.getChildren().addAll(txtSearch, spacer, btnAdd, btnEdit, btnDelete);

        // Table view
        tblBonusTypes = new TableView<>();
        tblBonusTypes.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        VBox.setVgrow(tblBonusTypes, Priority.ALWAYS);
        tblBonusTypes.columnResizePolicyProperty().setValue(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<BonusTypeDTO, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(50);

        TableColumn<BonusTypeDTO, String> colName = new TableColumn<>("Tên loại thưởng");
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colName.setPrefWidth(200);

        tblBonusTypes.getColumns().addAll(colId, colName);

        // Button bar at bottom
        HBox buttonBar = new HBox(10);
        buttonBar.setAlignment(Pos.CENTER);

        Button btnClose = new Button("Đóng");
        btnClose.getStyleClass().add("secondary-button");
        btnClose.setStyle("-fx-font-size: 18;");

        buttonBar.getChildren().add(btnClose);

        // Add all components to root
        root.getChildren().addAll(headerBar, actionBar, tblBonusTypes, buttonBar);

        // Set up event handlers
        btnAdd.setOnAction(event -> showAddEditDialog(null));

        btnEdit.setOnAction(event -> {
            BonusTypeDTO selected = tblBonusTypes.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showAddEditDialog(selected);
            } else {
                NotificationUtil.showWarningNotification("Thông báo", "Vui lòng chọn một loại thưởng để chỉnh sửa");
            }
        });

        btnDelete.setOnAction(event -> {
            BonusTypeDTO selected = tblBonusTypes.getSelectionModel().getSelectedItem();
            if (selected != null) {
                deleteBonusType(selected);
            } else {
                NotificationUtil.showWarningNotification("Thông báo", "Vui lòng chọn một loại thưởng để xóa");
            }
        });

        btnClose.setOnAction(event -> {
            Stage stage = (Stage) btnClose.getScene().getWindow();
            stage.close();
        });

        txtSearch.textProperty().addListener((obs, oldValue, newValue) -> {
            filterTable(newValue);
        });

        return root;
    }

    private void loadData() {
        try {
            List<BonusTypeDTO> list = bonusTypeBUS.findAll();
            bonusTypes = FXCollections.observableArrayList(list);
            tblBonusTypes.setItems(bonusTypes);
        } catch (Exception e) {
            log.error("Error loading bonus types: {}", e.getMessage());
            NotificationUtil.showErrorNotification("Lỗi", "Không thể tải dữ liệu loại thưởng");
        }
    }

    private void filterTable(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            tblBonusTypes.setItems(bonusTypes);
            return;
        }

        String lowerCaseFilter = searchText.toLowerCase();

        ObservableList<BonusTypeDTO> filteredList = bonusTypes.filtered(bonusType ->
            String.valueOf(bonusType.getId()).contains(lowerCaseFilter) ||
            bonusType.getName().toLowerCase().contains(lowerCaseFilter)
        );

        tblBonusTypes.setItems(filteredList);
    }

    private void showAddEditDialog(BonusTypeDTO bonusType) {
        // Create dialog
        Dialog<BonusTypeDTO> dialog = new Dialog<>();
        dialog.setTitle(bonusType == null ? "Thêm loại thưởng mới" : "Chỉnh sửa loại thưởng");

        // Set the button types
        ButtonType saveButtonType = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the content grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField txtName = new TextField();
        txtName.setPromptText("Tên loại thưởng");
        if (bonusType != null) {
            txtName.setText(bonusType.getName());
        }


        // Add fields to grid
        grid.add(new Label("Tên loại thưởng:"), 0, 0);
        grid.add(txtName, 1, 0);
        grid.add(new Label("Mô tả:"), 0, 1);

        dialog.getDialogPane().setContent(grid);

        // Request focus on name field by default
        txtName.requestFocus();

        // Convert the result to a bonus type when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                BonusTypeDTO result = bonusType != null ? bonusType : new BonusTypeDTO();
                result.setName(txtName.getText());
                return result;
            }
            return null;
        });

        Optional<BonusTypeDTO> result = dialog.showAndWait();

        result.ifPresent(newBonusType -> {
            try {
                boolean success;
                if (bonusType == null) {
                    // This is a new bonus type
                    success = bonusTypeBUS.insert(newBonusType);
                    if (success) {
                        NotificationUtil.showSuccessNotification("Thành công", "Thêm loại thưởng thành công");
                    }
                } else {
                    // This is an existing bonus type being edited
                    success = bonusTypeBUS.update(newBonusType);
                    if (success) {
                        NotificationUtil.showSuccessNotification("Thành công", "Cập nhật loại thưởng thành công");
                    }
                }

                if (success) {
                    loadData();
                    parentController.refreshData();
                }

            } catch (PermissionDeniedException e) {
                NotificationUtil.showErrorNotification("Lỗi quyền hạn", e.getMessage());
            } catch (Exception e) {
                log.error("Error saving bonus type: {}", e.getMessage());
                NotificationUtil.showErrorNotification("Lỗi", "Không thể lưu loại thưởng: " + e.getMessage());
            }
        });
    }

    private void deleteBonusType(BonusTypeDTO bonusType) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText("Xóa loại thưởng");
        alert.setContentText("Bạn có chắc chắn muốn xóa loại thưởng \"" + bonusType.getName() + "\"?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean success = bonusTypeBUS.delete(bonusType.getId());
                if (success) {
                    NotificationUtil.showSuccessNotification("Thành công", "Xóa loại thưởng thành công");
                    loadData();
                    parentController.refreshData();
                } else {
                    NotificationUtil.showErrorNotification("Lỗi", "Không thể xóa loại thưởng");
                }
            } catch (PermissionDeniedException e) {
                NotificationUtil.showErrorNotification("Lỗi quyền hạn", e.getMessage());
            } catch (Exception e) {
                log.error("Error deleting bonus type: {}", e.getMessage());
                NotificationUtil.showErrorNotification("Lỗi", "Không thể xóa loại thưởng: " + e.getMessage());
            }
        }
    }
}