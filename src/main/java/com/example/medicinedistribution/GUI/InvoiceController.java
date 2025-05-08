package com.example.medicinedistribution.GUI;

import com.example.medicinedistribution.BUS.BUSFactory;
import com.example.medicinedistribution.BUS.Interface.*;
import com.example.medicinedistribution.DTO.*;
import com.example.medicinedistribution.Exception.InsertFailedException;
import com.example.medicinedistribution.Exception.PermissionDeniedException;
import com.example.medicinedistribution.GUI.SubSelect.CustomerSelectController;
import com.example.medicinedistribution.GUI.SubSelect.SelectionHandler;
import com.example.medicinedistribution.Util.ExportUtils;
import com.example.medicinedistribution.Util.NotificationUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.text.Text;

import lombok.extern.slf4j.Slf4j;


import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class InvoiceController {

    //UI fields
    @FXML
    private Button btnAddCustomer;

    @FXML
    private Button btnClear;

    @FXML
    private Button btnPrint;

    @FXML
    private Button btnSave;

    @FXML
    private Button btnSearchInvoice;

    @FXML
    private Button btnSelectCustomer;

    @FXML
    private Button btnRefresh;

    @FXML
    private TextField txtCustomer;

    @FXML
    private ComboBox<CategoryDTO> cboSearchType;


    @FXML
    private TableColumn<InvoiceDTO, Void> colInvoiceActions;

    @FXML
    private TableColumn<InvoiceDTO, String> colInvoiceCustomer;

    @FXML
    private TableColumn<InvoiceDTO, LocalDate> colInvoiceDate;

    @FXML
    private TableColumn<InvoiceDTO, String> colInvoiceEmployee;

    @FXML
    private TableColumn<InvoiceDTO, Integer> colInvoiceId;

    @FXML
    private TableColumn<InvoiceDTO, BigDecimal> colInvoiceTotal;

    @FXML
    private TableColumn<InvoiceDetailDTO, Void> colItemAction;

    @FXML
    private TableColumn<InvoiceDetailDTO, BigDecimal> colItemPrice;

    @FXML
    private TableColumn<InvoiceDetailDTO, String> colItemProductName;

    @FXML
    private TableColumn<InvoiceDetailDTO, Void> colItemQuantity;

    @FXML
    private TableColumn<InvoiceDetailDTO, BigDecimal> colItemTotal;

    @FXML
    private TableColumn<ProductDTO, BigDecimal> colPrice;

    @FXML
    private TableColumn<ProductDTO, Integer> colProductId;

    @FXML
    private TableColumn<ProductDTO, String> colProductName;

    @FXML
    private TableColumn<ProductDTO,Void> colAction;

    @FXML
    private TableColumn<ProductDTO, String> colQuantity;


    @FXML
    private DatePicker dpFromDate;

    @FXML
    private DatePicker dpToDate;

    @FXML
    private Label lblTotal;

    @FXML
    private TableView<InvoiceDetailDTO> tblInvoiceDetails;

    @FXML
    private TableView<InvoiceDTO> tblInvoices;

    @FXML
    private TableView<ProductDTO> tblProducts;

    @FXML
    private TextField txtSearchInvoice;

    @FXML
    private TextField txtSearchProduct;

    @FXML
    Tab tabInvoice;


    //Functional fields
    private final BUSFactory busFactory;
    private InvoiceBUS invoiceBUS;
    private ProductBUS productBUS;
    private CategoryBUS categoryBUS;
    private CustomerBUS customerBUS;
    private EmployeeBUS employeeBUS;
    private UserSession userSession;



    private ArrayList<InvoiceDetailDTO> invoiceDetails;
    private ArrayList<ProductDTO> productList;
    private ArrayList<CategoryDTO> categoryList;
    private HashMap<Integer,CustomerDTO> customerList;
    private HashMap<Integer,EmployeeDTO> employeeList;
    private ArrayList<InvoiceDTO> invoiceList;

    private CustomerDTO selectedCustomer;
    public InvoiceController(BUSFactory busFactory) {
        this.busFactory = busFactory;
    }

    public void initialize() {
        invoiceBUS = busFactory.getInvoiceBUS();
        productBUS = busFactory.getProductBUS();
        categoryBUS = busFactory.getCategoryBUS();
        userSession = busFactory.getUserSession();
        customerBUS = busFactory.getCustomerBUS();
        employeeBUS = busFactory.getEmployeeBUS();

        setupUIData();
        setupUI();
        setupProductTable();
        setupItemTable();
    }

    private void setupUIData() {

        try {
            if (productList!= null){
                productList.clear();
            }
            if (categoryList != null){
                categoryList.clear();
            }
            productList = new ArrayList<>(productBUS.getAllActiveProducts());
            categoryList = new ArrayList<>(categoryBUS.findAll());
        } catch (PermissionDeniedException e) {
//            throw new PermissionDeniedException(e.getMessage());
            NotificationUtil.showErrorNotification("Lỗi", e.getMessage());
            throw new RuntimeException(e);
        } catch (Exception e) {
            NotificationUtil.showErrorNotification("Lỗi", "Có lỗi không xác định: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void setupUI() {
    // Initialize combo box
        cboSearchType.getItems().add(null);
        cboSearchType.getItems().addAll(categoryList);
        cboSearchType.getSelectionModel().select(0);
    // Set up event handlers
    cboSearchType.setOnAction(event -> filterProducts());
    txtSearchProduct.textProperty().addListener((observable, oldValue, newValue) -> filterProducts());

        btnSelectCustomer.setOnAction(event -> openCustomerSelector());

        btnClear.setOnAction(event -> {
            selectedCustomer = null;
            txtSearchProduct.clear();
            txtCustomer.setText("Chọn khách hàng");
            tblInvoiceDetails.getItems().clear();
            tblProducts.getItems().clear();
            setupUIData();
            cboSearchType.getSelectionModel().select(0);
            lblTotal.setText("0₫");
            setupProductTable();
        });
        btnSave.setOnAction(event -> createInvoice());
        tabInvoice.setOnSelectionChanged(event -> {
            initInvoiceTab();
        });

        btnPrint.setOnAction(event -> {
            // Handle print action
            InvoiceDTO invoice = createInvoiceFromForm();
            if (invoice == null) {
                return;
            }
            invoice.setInvoiceId(invoiceBUS.getNextInvoiceId());
            ExportUtils.exportInvoice(invoice,busFactory);
        });

        btnAddCustomer.setOnAction(event -> {
            showAddCustomerDialog();
        });

    }
    private void showAddCustomerDialog() {
        // Create dialog
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Thêm khách hàng mới");
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setResizable(false);
        Image icon = new Image(getClass().getResource("../../../../img/logo.png").toExternalForm());
        dialogStage.getIcons().add(icon);
        // Create layout
        BorderPane layout = new BorderPane();
        layout.getStyleClass().add("main-container");
        layout.setPrefWidth(500);

        // Header
        HBox header = new HBox();
        header.getStyleClass().add("header-section");
        header.setAlignment(Pos.CENTER);
        Label headerLabel = new Label("Thêm khách hàng mới");
        headerLabel.getStyleClass().add("heading-label");
        header.getChildren().add(headerLabel);
        layout.setTop(header);

        // Form content
        VBox formContent = new VBox(15);
        formContent.setPadding(new Insets(20));
        formContent.getStyleClass().add("settings-section");

        // Form fields
        GridPane formGrid = new GridPane();
        formGrid.setHgap(15);
        formGrid.setVgap(15);
        formGrid.setPrefWidth(460);

        // Configure grid columns to make text fields take full width
        ColumnConstraints labelColumn = new ColumnConstraints();
        labelColumn.setPrefWidth(120);

        ColumnConstraints fieldColumn = new ColumnConstraints();
        fieldColumn.setHgrow(Priority.ALWAYS);
        fieldColumn.setFillWidth(true);

        formGrid.getColumnConstraints().addAll(labelColumn, fieldColumn);

        // Customer name
        Label nameLabel = new Label("Tên khách hàng:");
        formGrid.add(nameLabel, 0, 0);
        TextField nameField = new TextField();
        nameField.setPromptText("Nhập tên khách hàng");
        nameField.setPrefWidth(300);
        formGrid.add(nameField, 1, 0);

        // Phone
        Label phoneLabel = new Label("Số điện thoại:");
        formGrid.add(phoneLabel, 0, 1);
        TextField phoneField = new TextField();
        phoneField.setPromptText("Nhập số điện thoại");
        phoneField.setPrefWidth(300);
        formGrid.add(phoneField, 1, 1);

        // Email
        Label emailLabel = new Label("Email:");
        formGrid.add(emailLabel, 0, 2);
        TextField emailField = new TextField();
        emailField.setPromptText("Nhập email");
        emailField.setPrefWidth(300);
        formGrid.add(emailField, 1, 2);

        // Address
        Label addressLabel = new Label("Địa chỉ:");
        formGrid.add(addressLabel, 0, 3);
        TextField addressField = new TextField();
        addressField.setPromptText("Nhập địa chỉ");
        addressField.setPrefWidth(300);
        formGrid.add(addressField, 1, 3);

        // Error message
        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setVisible(false);

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(15, 0, 0, 0));

        Button cancelButton = new Button("Hủy");
        cancelButton.setFont(new Font(16));
        cancelButton.getStyleClass().add("secondary-button");
        cancelButton.setOnAction(e -> dialogStage.close());

        Button saveButton = new Button("Lưu");
        saveButton.setFont(new Font(16));
        saveButton.getStyleClass().add("primary-button");
        saveButton.setOnAction(e -> {
            try {
                // Validate input
                if (nameField.getText().trim().isEmpty()) {
                    errorLabel.setText("Tên khách hàng không được để trống");
                    errorLabel.setVisible(true);
                    return;
                }

                // Create customer object
                CustomerDTO newCustomer = CustomerDTO.builder()
                        .customerName(nameField.getText().trim())
                        .phone(phoneField.getText().trim())
                        .email(emailField.getText().trim())
                        .address(addressField.getText().trim())
                        .build();

                // Save customer
                if (!customerBUS.insert(newCustomer)) {
                    errorLabel.setText("Lỗi: Không thể thêm khách hàng");
                    errorLabel.setVisible(true);
                    return;
                }

                // Update the selected customer and UI
                selectedCustomer = newCustomer;
                txtCustomer.setText(selectedCustomer.getCustomerName());

                NotificationUtil.showSuccessNotification("Thành công",
                        "Đã thêm khách hàng " + newCustomer.getCustomerName());

                dialogStage.close();

            } catch (Exception ex) {
                errorLabel.setText("Lỗi: " + ex.getMessage());
                errorLabel.setVisible(true);
                log.error("Error adding customer: {}", ex.getMessage(), ex);
            }
        });

        buttonBox.getChildren().addAll(cancelButton, saveButton);

        formContent.getChildren().addAll(formGrid, errorLabel, buttonBox);
        layout.setCenter(formContent);

        // Create scene and show dialog
        Scene scene = new Scene(layout);
        scene.getStylesheets().add(getClass().getResource("/css/main-style.css").toExternalForm());

        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }

    private void createInvoice() {
        if (valid()) {
            InvoiceDTO invoice = createInvoiceFromForm();
            try {
                invoiceBUS.insert(invoice);
                NotificationUtil.showSuccessNotification("Thành công", "Hóa đơn đã được lưu thành công");
                btnClear.fire();
            } catch (InsertFailedException | PermissionDeniedException e) {
                NotificationUtil.showErrorNotification("Lỗi", e.getMessage());
            } catch (Exception e){
                NotificationUtil.showErrorNotification("Lỗi", "Có lỗi không xác định: " + e.getMessage());
            }
        }
    }

    private void filterProducts() {
        String searchText = txtSearchProduct.getText().toLowerCase();
        CategoryDTO selectedCategory = cboSearchType.getSelectionModel().getSelectedItem();

        List<ProductDTO> filteredProducts = productList.stream()
                .filter(product ->
                        (selectedCategory == null || Objects.equals(product.getCategoryId(), selectedCategory.getCategoryId()))
                                && (searchText.isEmpty() || product.getProductName().toLowerCase().contains(searchText))
                )
                .collect(Collectors.toList());

        tblProducts.setItems(FXCollections.observableList(filteredProducts));
        tblProducts.refresh();
    }

    public void setupProductTable() {
    // Set up the product table
    colProductId.setCellValueFactory(cellData ->
        new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getProductId()));

    colProductName.setCellValueFactory(cellData ->
        new javafx.beans.property.SimpleStringProperty(cellData.getValue().getProductName()));

    colQuantity.setCellValueFactory(cellData -> {
        Integer quantity = cellData.getValue().getStockQuantity();
        String unit = cellData.getValue().getUnit();
        return new javafx.beans.property.SimpleStringProperty(quantity + " " + unit);
    });

    colPrice.setCellValueFactory(cellData ->
        new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getPrice()));

    // Add action button for each product
    colAction.setCellFactory(col -> new TableCell<>() {
        private final Button addButton = new Button("+");

        {
            addButton.getStyleClass().add("primary-button");
            addButton.setOnAction(event -> {
                ProductDTO product = getTableView().getItems().get(getIndex());
                addProductToInvoice(product);
            });
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setGraphic(null);
            } else {
                setGraphic(addButton);
                setAlignment(Pos.CENTER_RIGHT);
            }
        }
    });
    colAction.setStyle("-fx-alignment: CENTER;");
    tblProducts.setItems(FXCollections.observableList(productList));
    tblProducts.refresh();

}

    public void setupItemTable() {
    // Product name column
    colItemProductName.setCellValueFactory(cellData ->
        new javafx.beans.property.SimpleStringProperty(
            productList.stream()
                .filter(product -> Objects.equals(product.getProductId(), cellData.getValue().getProductId()))
                .findFirst()
                .map(ProductDTO::getProductName)
                .orElse("Unknown Product")
        )
    );

    // Quantity column with spinner
    colItemQuantity.setStyle("-fx-alignment: CENTER;");
    colItemQuantity.setCellFactory(col -> new TableCell<>() {
        private final Spinner<Integer> spinner;

        {
            spinner = createSpinner();
        }

        private Spinner<Integer> createSpinner() {
            Spinner<Integer> newSpinner = new Spinner<>(1, 999, 1);
            newSpinner.setEditable(true);
            newSpinner.getStyleClass().add("spinner");
            return newSpinner;
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setGraphic(null);
            } else {
                InvoiceDetailDTO invoiceItem = getTableView().getItems().get(getIndex());
                ProductDTO product = productList.stream()
                        .filter(p -> Objects.equals(p.getProductId(), invoiceItem.getProductId()))
                        .findFirst()
                        .orElse(null);

                if (product != null) {
                    SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory =
                            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, product.getStockQuantity(), invoiceItem.getQuantity());
                    spinner.setValueFactory(valueFactory);
                    spinner.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
                        if (!newValue.matches("\\d*")) {
                            spinner.getEditor().setText(oldValue);
                        }
                    });
                    // Add listener after setting value factory
                    spinner.valueProperty().addListener((obs, oldValue, newValue) -> {
                        if (product.getStockQuantity() < newValue) {
                            spinner.getValueFactory().setValue(product.getStockQuantity());
                            return;
                        }
                        invoiceItem.setQuantity(newValue);
                        invoiceItem.setTotal(invoiceItem.getPrice().multiply(BigDecimal.valueOf(newValue)));
                        updateInvoiceTotal();
                        tblInvoiceDetails.refresh();
                    });
                }
                setGraphic(spinner);
                setAlignment(Pos.CENTER);
            }
        }
    });

    // Price and total columns
    colItemPrice.setCellValueFactory(cellData ->
        new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getPrice()));
    colItemTotal.setCellValueFactory(cellData ->
        new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getTotal()));

    // Action column with remove button
    colItemAction.setCellFactory(col -> new TableCell<>() {
        private final Button removeButton;

        {
            removeButton = new Button("-");
            removeButton.getStyleClass().add("primary-button");
            removeButton.setPrefWidth(30);
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setGraphic(null);
            } else {
                removeButton.setOnAction(event -> {
                    InvoiceDetailDTO currentItem = getTableView().getItems().get(getIndex());
                    removeItemFromInvoice(currentItem);
                });
                setGraphic(removeButton);
                setAlignment(Pos.CENTER);
            }
        }
    });
    colItemAction.setStyle("-fx-alignment: CENTER;");
}

    private void removeItemFromInvoice(InvoiceDetailDTO item) {
        // Remove the item from the invoice details
        tblInvoiceDetails.getItems().remove(item);
        // Update the total price
        updateInvoiceTotal();
        // Refresh the table
        tblInvoiceDetails.refresh();
    }

    private void updateInvoiceTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (InvoiceDetailDTO item : tblInvoiceDetails.getItems()) {
            total = total.add(item.getTotal());
        }
        lblTotal.setText(total+"₫");
    }


    private void addProductToInvoice(ProductDTO product) {
    // Check if product already exists in invoice
    boolean exists = false;
    for (InvoiceDetailDTO detail : tblInvoiceDetails.getItems()) {
        if (Objects.equals(detail.getProductId(), product.getProductId())) {
            // Update quantity if product exists
            detail.setQuantity(detail.getQuantity() + 1);
            detail.setTotal(detail.getPrice().multiply(BigDecimal.valueOf(detail.getQuantity())));
            exists = true;
            break;
        }
    }

    // Add new product if it doesn't exist
    if (!exists) {
        InvoiceDetailDTO newDetail = new InvoiceDetailDTO();
        newDetail.setProductId(product.getProductId());
        newDetail.setPrice(product.getPrice());
        newDetail.setQuantity(1);
        newDetail.setTotal(product.getPrice());
        newDetail.setProductName(product.getProductName());
        newDetail.setUnit(product.getUnit());
        tblInvoiceDetails.getItems().add(newDetail);
    }

    // Refresh table and update total
    tblInvoiceDetails.refresh();
    updateInvoiceTotal();
}
    private void openCustomerSelector() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/medicinedistribution/GUI/SubSelect.fxml"));
            CustomerSelectController controller = new CustomerSelectController(busFactory);
            loader.setController(controller);

            Parent root = loader.load();

            // Set up the callback that will be triggered when a customer is selected
            controller.setSelectionHandler(new SelectionHandler<CustomerDTO>() {
                @Override
                public void onItemSelected(CustomerDTO customer) {
                    selectedCustomer = customer;
                    txtCustomer.setText(customer.getCustomerName());
                }
            });

            Stage stage = new Stage();
            stage.setTitle("Chọn khách hàng");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            log.error("Error loading customer selector: ", e);
            // Show error dialog
        }
    }

    public InvoiceDTO createInvoiceFromForm(){
        if (!valid()) {
            return null;
        }
        InvoiceDTO invoice = new InvoiceDTO();
        invoice.setCustomerId(selectedCustomer.getCustomerId());
        invoice.setEmployeeId(userSession.getEmployee().getEmployeeId());
        invoice.setDate(LocalDate.now());
        invoice.setTotal(getInvoiceTotal());
        invoice.setDetails(new ArrayList<>(tblInvoiceDetails.getItems()));
        return invoice;
    }

    public BigDecimal getInvoiceTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (InvoiceDetailDTO item : tblInvoiceDetails.getItems()) {
            total = total.add(item.getTotal());
        }
        return total;
    }

    public boolean valid(){
        if (selectedCustomer == null) {
            NotificationUtil.showErrorNotification("Lỗi", "Vui lòng chọn khách hàng");
            return false;
        }
        if (tblInvoiceDetails.getItems().isEmpty()) {
            NotificationUtil.showErrorNotification("Lỗi", "Vui lòng thêm sản phẩm vào hóa đơn");
            return false;
        }
        if (lblTotal.getText().equals("0")) {
            NotificationUtil.showErrorNotification("Lỗi", "Tổng tiền không thể bằng 0");
            return false;
        }
        for (InvoiceDetailDTO item : tblInvoiceDetails.getItems()) {
            try {
                if(!productBUS.checkStock(item.getProductId(), item.getQuantity())){
                    NotificationUtil.showErrorNotification("Lỗi", "Sản phẩm " + item.getProductId() + " không đủ số lượng trong kho");
                    return false;
                }
            } catch (PermissionDeniedException | IllegalArgumentException e) {
                NotificationUtil.showErrorNotification("Lỗi", e.getMessage());
                return false;
            } catch (Exception e) {
                NotificationUtil.showErrorNotification("Lỗi", "Có lỗi không xác định: " + e.getMessage());
                return false;
            }
        }
        return true;
    }

    private void initInvoiceTab() {
        setupUIInvoiceData();
        setupUIInvoiceTab();
        setUpInvoiceTable();
        loadInvoices();

    }

    private void setupUIInvoiceTab() {
        btnSearchInvoice.setOnAction(event -> loadInvoices());
        btnRefresh.setOnAction(event -> {
            dpFromDate.setValue(null);
            dpToDate.setValue(null);
            txtSearchInvoice.clear();
            setupUIInvoiceData();
            loadInvoices();
        });
    }

    private void setupUIInvoiceData() {
        if (invoiceList != null) {
            invoiceList.clear();
        }
        if (customerList != null) {
            customerList.clear();
        }
        if (employeeList != null) {
            employeeList.clear();
        }
        customerList = new HashMap<>();
        employeeList = new HashMap<>();
        invoiceList = new ArrayList<>(invoiceBUS.findAll());
        for (CustomerDTO customer : customerBUS.findAll()) {
            customerList.put(customer.getCustomerId(), customer);
        }
        for (EmployeeDTO employee : employeeBUS.findAll()) {
            employeeList.put(employee.getEmployeeId(), employee);
        }
        invoiceList = new ArrayList<>(invoiceBUS.findAll());
    }

    private void loadInvoices() {
        LocalDate fromDate = dpFromDate.getValue();
        LocalDate toDate = dpToDate.getValue();
        String searchText = txtSearchInvoice.getText().toLowerCase();

        List<InvoiceDTO> filteredInvoices = invoiceBUS.findAll().stream()
                .filter(invoice -> (fromDate == null || !invoice.getDate().isBefore(fromDate))
                        && (toDate == null || !invoice.getDate().isAfter(toDate))
                        && (searchText.isEmpty() || customerList.get(invoice.getCustomerId()).getCustomerName().toLowerCase().contains(searchText) ||
                        employeeList.get(invoice.getEmployeeId()).getFullName().toLowerCase().contains(searchText)))
                .collect(Collectors.toList());
        //Tìm bằng tên khách hàng

        tblInvoices.setItems(FXCollections.observableList(filteredInvoices));
        tblInvoices.refresh();
    }

    public void setUpInvoiceTable(){
        // Set up the invoice table
        colInvoiceId.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getInvoiceId()));

        colInvoiceDate.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getDate()));

        colInvoiceCustomer.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        customerList.get(cellData.getValue().getCustomerId()).getCustomerName()
                ));

        colInvoiceEmployee.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        employeeList.get(cellData.getValue().getEmployeeId()).getFullName()
                ));

        colInvoiceTotal.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getTotal()));

        // Add action button for each invoice
        colInvoiceActions.setCellFactory(col -> new TableCell<>() {
            private final Button viewButton = new Button("Xem chi tiết");

            {
                viewButton.getStyleClass().add("primary-button");
                viewButton.setOnAction(event -> {
                    InvoiceDTO invoice = getTableView().getItems().get(getIndex());
                    viewInvoiceDetails(invoice);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(viewButton);
                    setAlignment(Pos.CENTER_RIGHT);
                }
            }
        });
        colInvoiceActions.setStyle("-fx-alignment: CENTER;");
    }

    private void viewInvoiceDetails(InvoiceDTO invoice) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("InvoiceDetail.fxml"));
            InvoiceDetailController controller = new InvoiceDetailController(busFactory,invoice);
            loader.setController(controller);

            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Chi tiết hóa đơn");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            log.error("Error loading invoice detail view: ", e);
        }
    }


}
