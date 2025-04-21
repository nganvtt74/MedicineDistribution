package com.example.medicinedistribution.GUI.SubSelect;

import com.example.medicinedistribution.BUS.BUSFactory;
import com.example.medicinedistribution.BUS.Interface.ManufacturerBUS;
import com.example.medicinedistribution.DTO.CustomerDTO;
import com.example.medicinedistribution.DTO.ManufacturerDTO;
import com.example.medicinedistribution.Util.NotificationUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import lombok.Setter;

public class ManufacturerSelectController {
    @FXML
    private Label lblTitle;

    @FXML
    private TextField txtSearch;

    @FXML
    private TableView<ManufacturerDTO> tblItems;

    @FXML
    private TableColumn<ManufacturerDTO, Integer> colId;

    @FXML
    private TableColumn<ManufacturerDTO, String> colName;

    @FXML
    private TableColumn<ManufacturerDTO, String> colPhone;

    @FXML
    private TableColumn<ManufacturerDTO, String> colEmail;

    @FXML
    private Button btnSelect;

    @FXML
    private Button btnCancel;

    private final BUSFactory busFactory;
    private ManufacturerBUS manufacturerBUS;
    private ObservableList<ManufacturerDTO> manufacturerList;

    @Setter
    private SelectionHandler<ManufacturerDTO> selectionHandler;


    public ManufacturerSelectController(BUSFactory busFactory) {
        this.busFactory = busFactory;

    }

    @FXML
    public void initialize() {
        manufacturerBUS = busFactory.getManufacturerBUS();
        colId = new TableColumn<>("Mã NSX");
        colName = new TableColumn<>("Tên nhà sản xuất");
        colPhone = new TableColumn<>("Số điện thoại");
        colEmail = new TableColumn<>("Email");

        colId.setMaxWidth(100);
        colName.setMinWidth(200);

        colId.setStyle("-fx-alignment: CENTER;");

        colId.setCellValueFactory(new PropertyValueFactory<>("manufacturerId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("manufacturerName"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        tblItems.getColumns().addAll(colId, colName, colPhone, colEmail);
        tblItems.setMinWidth(680);


        loadManufacturerData();

        // Set up search functionality
        setupSearch();

        // Set up button actions
        setupButtons();
    }

    private void setupButtons() {
        btnSelect.setOnAction(event -> {
            ManufacturerDTO selectedManufacturer = tblItems.getSelectionModel().getSelectedItem();
            if (selectedManufacturer != null) {
                // Handle the selection of the manufacturer
                // For example, you can pass the selected manufacturer to another controller or close the dialog
                selectionHandler.onItemSelected(selectedManufacturer);
                closeDialog();
            } else {
                NotificationUtil.showErrorNotification("Lỗi","Vui lòng chọn nhà sản xuất");
            }
        });

        btnCancel.setOnAction(event -> {
            if (selectionHandler != null) {
                selectionHandler.onSelectionCancelled();
            }
            closeDialog();
        });
    }

    private void setupSearch() {
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
           filterManufacturerList(newValue); 
        });
    }

    private void filterManufacturerList(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            tblItems.setItems(manufacturerList);
        } else {
            // Filter customers by ID or name containing the search text
            String lowerCaseFilter = searchText.toLowerCase();

            ObservableList<ManufacturerDTO> filteredList = manufacturerList.filtered(manufacturerDTO ->
                    manufacturerDTO.getManufacturerName().toLowerCase().contains(lowerCaseFilter) ||
                            manufacturerDTO.getPhone().toLowerCase().contains(lowerCaseFilter) ||
                            manufacturerDTO.getEmail().contains(lowerCaseFilter)
            );
            tblItems.setItems(filteredList);
        }
    }

    private void loadManufacturerData() {
        manufacturerList = FXCollections.observableList(manufacturerBUS.findAll());
        System.out.println("Manufacturer List: " + manufacturerList);
        tblItems.setItems(manufacturerList);
        tblItems.setPlaceholder(new Label("Không có dữ liệu"));
        tblItems.refresh();

    }

    private void closeDialog() {
        // Close the dialog or window
        // You can use the appropriate method to close the dialog based on your application structure
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

}
