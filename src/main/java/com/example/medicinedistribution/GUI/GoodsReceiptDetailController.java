package com.example.medicinedistribution.GUI;

import com.example.medicinedistribution.BUS.BUSFactory;
import com.example.medicinedistribution.DTO.*;
import com.example.medicinedistribution.Util.ExportUtils;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.math.BigDecimal;
import java.util.HashMap;

public class GoodsReceiptDetailController {
    //UI components:
    @FXML
    private Button btnClose;

    @FXML
    private Button btnPrint;

    @FXML
    private TableColumn<GoodsReceiptDetailDTO, BigDecimal> colPrice;

    @FXML
    private TableColumn<GoodsReceiptDetailDTO, Integer> colProductId;

    @FXML
    private TableColumn<GoodsReceiptDetailDTO, String> colProductName;

    @FXML
    private TableColumn<GoodsReceiptDetailDTO, Integer> colQuantity;

    @FXML
    private TableColumn<GoodsReceiptDetailDTO, BigDecimal> colTotal;

    @FXML
    private TableColumn<GoodsReceiptDetailDTO, String> colUnit;

    @FXML
    private Label lblManufacturer;

    @FXML
    private Label lblEmployee;

    @FXML
    private Label lblReceiptDate;

    @FXML
    private Label lblReceiptId;

    @FXML
    private Label lblPhone;

    @FXML
    private Label lblAddress;

    @FXML
    private Label lblTotal;

    @FXML
    private TableView<GoodsReceiptDetailDTO> tblReceiptDetails;

    //Functionality:
    private final BUSFactory busFactory;
    private final GoodsReceiptDTO receipt;
    private final HashMap<Integer, ProductDTO> productMap = new HashMap<>();

    public GoodsReceiptDetailController(BUSFactory busFactory, GoodsReceiptDTO receipt) {
        this.busFactory = busFactory;
        this.receipt = receipt;
    }

    @FXML
    public void initialize() {
        ManufacturerDTO manufacturer = busFactory.getManufacturerBUS().findById(receipt.getManufacturerId());
        EmployeeDTO employee = busFactory.getEmployeeBUS().findById(receipt.getEmployeeId());

        for(ProductDTO product : busFactory.getProductBUS().findAll()) {
            productMap.put(product.getProductId(), product);
        }

        // Initialize the table and labels with receipt details
        setupUI(manufacturer, employee);

        // Load receipt details into the table
        setupTable();
        loadReceiptDetails();
    }

    private void setupUI(ManufacturerDTO manufacturer, EmployeeDTO employee) {
        lblReceiptId.setText(String.valueOf(receipt.getGoodsReceiptId()));
        lblReceiptDate.setText(receipt.getDate().toString());
        lblManufacturer.setText(manufacturer.getManufacturerName());
        lblPhone.setText(manufacturer.getPhone());
        lblAddress.setText(manufacturer.getAddress());
        lblEmployee.setText(employee.getFullName());
        lblTotal.setText(String.valueOf(receipt.getTotal()));

        btnClose.setOnAction(event -> {
            // Close the window
            ((javafx.stage.Stage) btnClose.getScene().getWindow()).close();
        });

        btnPrint.setOnAction(event -> {
            // Print the receipt
            // Implement print functionality here
            ExportUtils.exportGoodsReceipt(receipt, busFactory);
        });
    }

    private void loadReceiptDetails() {
        tblReceiptDetails.getItems().clear();
        tblReceiptDetails.setItems(FXCollections.observableList(receipt.getDetails()));
        tblReceiptDetails.refresh();
    }

    private void setupTable() {
        // Set up the table columns
        colProductId.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getProductId()));
        colProductName.setCellValueFactory(cellData ->
                new SimpleStringProperty(productMap.get(cellData.getValue().getProductId()).getProductName()));
        colUnit.setCellValueFactory(cellData ->
                new SimpleStringProperty(productMap.get(cellData.getValue().getProductId()).getUnit()));
        colQuantity.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getQuantity()));
        colPrice.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getPrice()));
        colTotal.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getTotal()));
    }
}