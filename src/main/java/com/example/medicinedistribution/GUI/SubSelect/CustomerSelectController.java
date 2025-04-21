package com.example.medicinedistribution.GUI.SubSelect;

import com.example.medicinedistribution.BUS.BUSFactory;
import com.example.medicinedistribution.BUS.Interface.CustomerBUS;
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

public class CustomerSelectController{

    @FXML
    private Label lblTitle;

    @FXML
    private TextField txtSearch;

    @FXML
    private TableView<CustomerDTO> tblItems;

    @FXML
    private TableColumn<CustomerDTO, Integer> colId;

    @FXML
    private TableColumn<CustomerDTO, String> colName;

    @FXML
    private TableColumn<CustomerDTO, String> colPhone;

    @FXML
    private Button btnSelect;

    @FXML
    private Button btnCancel;

    private final CustomerBUS customerBUS;
    private ObservableList<CustomerDTO> customerList;
    // Method to set the selection handler
    @Setter
    private SelectionHandler<CustomerDTO> selectionHandler;

    public CustomerSelectController(BUSFactory busFactory) {
        this.customerBUS = busFactory.getCustomerBUS();
    }

    public void initialize() {
        colId = new TableColumn<>("Mã KH");
        colName = new TableColumn<>("Tên khách hàng");
        colPhone = new TableColumn<>("Số điện thoại");

        colId.setMaxWidth(50);
        colName.setMinWidth(300);

        colId.setStyle("-fx-alignment: CENTER;");


        // Set title
        lblTitle.setText("Danh sách khách hàng");
        // Set up table properties
        tblItems.setPlaceholder(new Label("Không có dữ liệu"));

        // Gắn columns vào table
        tblItems.getColumns().clear();
        tblItems.getColumns().addAll(colId, colName, colPhone);

        // Initialize table columns
        colId.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));

        // Load customer data
        loadCustomerData();

        // Set up search functionality
        setupSearch();

        // Set up button actions
        setupButtons();
    }

    private void loadCustomerData() {
        // Get all customers from BUS layer
        customerList = FXCollections.observableArrayList(customerBUS.findAll());
        tblItems.setItems(customerList);
    }

    private void setupSearch() {
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filterCustomers(newValue);
        });
    }

    private void filterCustomers(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            tblItems.setItems(customerList);
        } else {
            // Filter customers by ID or name containing the search text
            String lowerCaseFilter = searchText.toLowerCase();

            ObservableList<CustomerDTO> filteredList = customerList.filtered(customer ->
                customer.getCustomerName().toLowerCase().contains(lowerCaseFilter) ||
                (customer.getPhone() != null && customer.getPhone().contains(lowerCaseFilter))
            );

            tblItems.setItems(filteredList);
        }
    }

    private void setupButtons() {
        // Configure Select button
        btnSelect.setOnAction(event -> {
            CustomerDTO selectedCustomer = tblItems.getSelectionModel().getSelectedItem();
            if (selectedCustomer != null && selectionHandler != null) {
                selectionHandler.onItemSelected(selectedCustomer);
                closeDialog();
            } else {
                NotificationUtil.showErrorNotification("Lỗi", "Vui lòng chọn một khách hàng.");
            }
        });

        // Configure Cancel button
        btnCancel.setOnAction(event -> {
            if (selectionHandler != null) {
                selectionHandler.onSelectionCancelled();
            }
            closeDialog();
        });
    }

    private void closeDialog() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

}