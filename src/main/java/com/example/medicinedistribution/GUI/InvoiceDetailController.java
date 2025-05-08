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

public class InvoiceDetailController {
    //UI components:
    @FXML
    private Button btnClose;

    @FXML
    private Button btnPrint;

    @FXML
    private TableColumn<InvoiceDetailDTO, BigDecimal> colPrice;

    @FXML
    private TableColumn<InvoiceDetailDTO, Integer> colProductId;

    @FXML
    private TableColumn<InvoiceDetailDTO, String> colProductName;

    @FXML
    private TableColumn<InvoiceDetailDTO, Integer> colQuantity;

    @FXML
    private TableColumn<InvoiceDetailDTO, BigDecimal> colTotal;

    @FXML
    private TableColumn<InvoiceDetailDTO, String> colUnit;

    @FXML
    private Label lblAddress;

    @FXML
    private Label lblCustomer;

    @FXML
    private Label lblEmployee;

    @FXML
    private Label lblInvoiceDate;

    @FXML
    private Label lblInvoiceId;

    @FXML
    private Label lblPhone;

    @FXML
    private Label lblTotal;

    @FXML
    private TableView<InvoiceDetailDTO> tblInvoiceDetails;



    //Functionality:
    private final BUSFactory busFactory;
    private final InvoiceDTO invoice;
    private final HashMap<Integer, ProductDTO> productMap = new HashMap<>();

    public InvoiceDetailController(BUSFactory busFactory, InvoiceDTO invoice) {
        this.busFactory = busFactory;
        this.invoice = invoice;
    }

    @FXML
    public void initialize() {
        CustomerDTO customer = busFactory.getCustomerBUS().findById(invoice.getCustomerId());
        EmployeeDTO employee = busFactory.getEmployeeBUS().findById(invoice.getEmployeeId());

        for(ProductDTO product : busFactory.getProductBUS().findAll()) {
            productMap.put(product.getProductId(), product);
        }

        // Initialize the table and labels with invoice details
        setupUI(customer, employee);

        // Load invoice details into the table
        setupTable();
        loadInvoiceDetails();
    }

    private void setupUI(CustomerDTO customer, EmployeeDTO employee) {
        lblInvoiceId.setText(String.valueOf(invoice.getInvoiceId()));
        lblInvoiceDate.setText(invoice.getDate().toString());
        lblCustomer.setText(customer.getCustomerName());
        lblPhone.setText(customer.getPhone());
        lblAddress.setText(customer.getAddress());
        lblEmployee.setText(employee.getFullName());
        lblTotal.setText(String.valueOf(invoice.getTotal()));

        btnClose.setOnAction(event -> {
            // Close the window
            ((javafx.stage.Stage) btnClose.getScene().getWindow()).close();
        });

        btnPrint.setOnAction(event -> {
            // Print the invoice
            ExportUtils.exportInvoice(invoice, busFactory);
        });

    }

    private void loadInvoiceDetails() {
        tblInvoiceDetails.getItems().clear();
        tblInvoiceDetails.setItems(FXCollections.observableList(invoice.getDetails()));
        tblInvoiceDetails.refresh();
    }

    private void setupTable() {
        // Assuming you have a method to get invoice details from the invoice object

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
