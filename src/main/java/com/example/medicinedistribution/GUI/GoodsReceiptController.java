package com.example.medicinedistribution.GUI;

    import com.example.medicinedistribution.BUS.BUSFactory;
    import com.example.medicinedistribution.BUS.Interface.*;
    import com.example.medicinedistribution.DTO.*;
    import com.example.medicinedistribution.Exception.InsertFailedException;
    import com.example.medicinedistribution.Exception.PermissionDeniedException;
    import com.example.medicinedistribution.GUI.SubSelect.ManufacturerSelectController;
    import com.example.medicinedistribution.GUI.SubSelect.SelectionHandler;
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
    import javafx.scene.image.ImageView;
    import javafx.scene.layout.*;
    import javafx.stage.Modality;
    import javafx.stage.Stage;
    import lombok.extern.slf4j.Slf4j;

    import java.io.IOException;
    import java.math.BigDecimal;
    import java.math.RoundingMode;
    import java.time.LocalDate;
    import java.util.*;
    import java.util.stream.Collectors;

    @Slf4j
    public class GoodsReceiptController {

        //UI components
        @FXML
        private Button btnAddManufacturer;

        @FXML
        private Button btnAddManufacturer1;

        @FXML
        private Button btnClear;

        @FXML
        private Button btnPrint;

        @FXML
        private Button btnSave;

        @FXML
        private Button btnSearchGoodsReceipt;

        @FXML
        private ComboBox<CategoryDTO> cboSearchType;

        @FXML
        private TableColumn<ProductDTO, Void> colAction;

        @FXML
        private TableColumn<ProductDTO, String> colCategory;

        @FXML
        private TableColumn<GoodsReceiptDTO, Void> colGoodsReceiptActions;

        @FXML
        private TableColumn<GoodsReceiptDTO, LocalDate> colGoodsReceiptDate;

        @FXML
        private TableColumn<GoodsReceiptDTO, String> colGoodsReceiptEmployee;

        @FXML
        private TableColumn<GoodsReceiptDTO, Integer> colGoodsReceiptId;

        @FXML
        private TableColumn<GoodsReceiptDTO, String> colGoodsReceiptManufacturer;

        @FXML
        private TableColumn<GoodsReceiptDTO, BigDecimal> colGoodsReceiptTotal;

        @FXML
        private TableColumn<GoodsReceiptDetailDTO, Void> colItemAction;

        @FXML
        private TableColumn<GoodsReceiptDetailDTO, BigDecimal> colItemPrice;

        @FXML
        private TableColumn<GoodsReceiptDetailDTO, String> colItemProductName;

        @FXML
        private TableColumn<GoodsReceiptDetailDTO, Integer> colItemQuantity;

        @FXML
        private TableColumn<GoodsReceiptDetailDTO, BigDecimal> colItemTotal;

        @FXML
        private TableColumn<ProductDTO, Integer> colProductId;

        @FXML
        private TableColumn<ProductDTO, String> colProductName;

        @FXML
        private TableColumn<ProductDTO, String> colUnit;

        @FXML
        private DatePicker dpFromDate;

        @FXML
        private DatePicker dpToDate;

        @FXML
        private Label lblTotal;

        @FXML
        private TableView<GoodsReceiptDetailDTO> tblGoodsReceiptDetails;

        @FXML
        private TableView<GoodsReceiptDTO> tblGoodsReceipts;

        @FXML
        private TableView<ProductDTO> tblProducts;

        @FXML
        private TextField txtSearchGoodsReceipt;

        @FXML
        private TextField txtSearchProduct;

        @FXML
        private TextField txtManufacturer;

        @FXML
        private Tab tabGoodsReceipt;

        // Business logic fields
        private final BUSFactory busFactory;
        private GoodsReceiptBUS goodsReceiptBUS;
        private ProductBUS productBUS;
        private CategoryBUS categoryBUS;
        private ManufacturerBUS manufacturerBUS;
        private EmployeeBUS employeeBUS;
        private UserSession userSession;

        private ArrayList<GoodsReceiptDetailDTO> receiptDetails;
        private ArrayList<ProductDTO> productList;
        private ArrayList<CategoryDTO> categoryList;
        private HashMap<Integer, ManufacturerDTO> manufacturerList;
        private HashMap<Integer, EmployeeDTO> employeeList;
        private ArrayList<GoodsReceiptDTO> receiptList;

        private ManufacturerDTO selectedManufacturer;

        public GoodsReceiptController(BUSFactory busFactory) {
            this.busFactory = busFactory;
        }

        public void initialize() {
            goodsReceiptBUS = busFactory.getGoodsReceiptBUS();
            productBUS = busFactory.getProductBUS();
            categoryBUS = busFactory.getCategoryBUS();
            manufacturerBUS = busFactory.getManufacturerBUS();
            employeeBUS = busFactory.getEmployeeBUS();
            userSession = busFactory.getUserSession();

            setupUIData();
            setupUI();
            setupProductTable();
            setupItemTable();
        }

        private void setupUIData() {
            if (productList != null) {
                productList.clear();
            }
            if (categoryList != null) {
                categoryList.clear();
            }

            productList = new ArrayList<>(productBUS.getAllActiveProducts());

            for (ProductDTO product : productList) {
                product.setPrice(product.getPrice().multiply(BigDecimal.valueOf(0.60)));
            }


            categoryList = new ArrayList<>(categoryBUS.findAll());
            receiptDetails = new ArrayList<>();
        }

        private void setupUI() {
            // Initialize combo box
            cboSearchType.getItems().add(null);
            cboSearchType.getItems().addAll(categoryList);
            cboSearchType.getSelectionModel().select(0);

            // Set up event handlers
            cboSearchType.setOnAction(event -> filterProducts());
            txtSearchProduct.textProperty().addListener((observable, oldValue, newValue) -> filterProducts());

            btnAddManufacturer1.setOnAction(event -> openManufacturerSelector());
            btnAddManufacturer.setOnAction(event -> showAddManufacturerDialog());

            btnClear.setOnAction(event -> {
                selectedManufacturer = null;
                txtSearchProduct.clear();
                txtManufacturer.setText("Nhập nhà sản xuất");
                tblGoodsReceiptDetails.getItems().clear();
                tblProducts.getItems().clear();
                setupUIData();
                cboSearchType.getSelectionModel().select(0);
                lblTotal.setText("0₫");
                setupProductTable();
            });

            btnSave.setOnAction(event -> createGoodsReceipt());
            btnPrint.setOnAction(event -> printGoodsReceipt());

            tabGoodsReceipt.setOnSelectionChanged(event -> {
                initGoodsReceiptTab();
            });
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
            colProductId.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getProductId()));

            colProductName.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getProductName()));

            colCategory.setCellValueFactory(cellData -> {
                CategoryDTO category = categoryList.stream()
                        .filter(c -> c.getCategoryId().equals(cellData.getValue().getCategoryId()))
                        .findFirst()
                        .orElse(null);
                return new javafx.beans.property.SimpleStringProperty(
                        category != null ? category.getCategoryName() : "");
            });

            colUnit.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getUnit()));

            colAction.setCellFactory(col -> new TableCell<>() {
                private final Button addButton = new Button("+");

                {
                    addButton.getStyleClass().add("primary-button");
                    addButton.setOnAction(event -> {
                        ProductDTO product = getTableView().getItems().get(getIndex());
                        addProductToReceipt(product);
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(addButton);
                        setAlignment(Pos.CENTER);
                    }
                }
            });
            colAction.setStyle("-fx-alignment: CENTER;");
            tblProducts.setItems(FXCollections.observableList(productList));
        }

        public void setupItemTable() {
            colItemProductName.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                    productList.stream()
                        .filter(product -> Objects.equals(product.getProductId(), cellData.getValue().getProductId()))
                        .findFirst()
                        .map(ProductDTO::getProductName)
                        .orElse("Unknown Product")
                )
            );

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
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        GoodsReceiptDetailDTO receiptItem = getTableView().getItems().get(getIndex());

                        SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory =
                                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, receiptItem.getQuantity());
                        spinner.setValueFactory(valueFactory);
                        spinner.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
                            if (!newValue.matches("\\d*")) {
                                spinner.getEditor().setText(oldValue);
                            }
                        });

                        spinner.valueProperty().addListener((obs, oldValue, newValue) -> {
                            receiptItem.setQuantity(newValue);
                            receiptItem.setTotal(receiptItem.getPrice().multiply(BigDecimal.valueOf(newValue)));
                            updateReceiptTotal();
                            tblGoodsReceiptDetails.refresh();
                        });

                        setGraphic(spinner);
                        setAlignment(Pos.CENTER);
                    }
                }
            });

           colItemPrice.setCellFactory(col -> new TableCell<GoodsReceiptDetailDTO, BigDecimal>() {
                private final TextField textField = new TextField();
                private GoodsReceiptDetailDTO currentItem;

                {
                    textField.setStyle("-fx-height: 26px; -fx-font-size: 12px;" +
                            "-fx-padding: 4 5 ; -fx-border-radius: 1px");
                    // Allow only numeric input with decimal point
                    textField.textProperty().addListener((observable, oldValue, newValue) -> {
                        if (!newValue.matches("\\d*(\\.\\d*)?")) {
                            textField.setText(oldValue);
                        }
                    });

                    // Update price and total when focus is lost
                    textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                        if (!isNowFocused && currentItem != null) { // focus lost
                            commitEdit();
                        }
                    });

                    // Also update on Enter key
                    textField.setOnAction(e -> commitEdit());
                }

                private void commitEdit() {
                    try {
                        BigDecimal newPrice = new BigDecimal(textField.getText());
                        currentItem.setPrice(newPrice);
                        currentItem.setTotal(newPrice.multiply(BigDecimal.valueOf(currentItem.getQuantity())));
                        updateReceiptTotal();
                        getTableView().refresh();
                    } catch (NumberFormatException ex) {
                        textField.setText("0");
                        currentItem.setPrice(BigDecimal.ZERO);
                        currentItem.setTotal(BigDecimal.ZERO);
                        updateReceiptTotal();
                        getTableView().refresh();
                    }
                }

                @Override
                public void updateItem(BigDecimal price, boolean empty) {
                    super.updateItem(price, empty);

                    if (empty) {
                        setGraphic(null);
                        currentItem = null;
                    } else {
                        int index = getIndex();
                        // Check for valid index
                        if (index >= 0 && index < getTableView().getItems().size()) {
                            currentItem = getTableView().getItems().get(index);

                            // Set default of 0 when price is null
                            textField.setText(currentItem.getPrice() != null ?
                                    currentItem.getPrice().toString() : "0");
                            setGraphic(textField);
                        } else {
                            setGraphic(null);
                            currentItem = null;
                        }
                    }
                }
            });

            colItemTotal.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getTotal()));

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
                            GoodsReceiptDetailDTO currentItem = getTableView().getItems().get(getIndex());
                            removeItemFromReceipt(currentItem);
                        });
                        setGraphic(removeButton);
                        setAlignment(Pos.CENTER);
                    }
                }
            });
            colItemAction.setStyle("-fx-alignment: CENTER;");
        }

        private void removeItemFromReceipt(GoodsReceiptDetailDTO item) {
            tblGoodsReceiptDetails.getItems().remove(item);
            updateReceiptTotal();
            tblGoodsReceiptDetails.refresh();
        }

        private void updateReceiptTotal() {
            BigDecimal total = BigDecimal.ZERO;
            for (GoodsReceiptDetailDTO item : tblGoodsReceiptDetails.getItems()) {
                total = total.add(item.getTotal());
            }
            lblTotal.setText(total + "₫");
        }

        private void addProductToReceipt(ProductDTO product) {
            boolean exists = false;
            for (GoodsReceiptDetailDTO detail : tblGoodsReceiptDetails.getItems()) {
                if (Objects.equals(detail.getProductId(), product.getProductId())) {
                    detail.setQuantity(detail.getQuantity() + 1);
                    detail.setTotal(detail.getPrice().multiply(BigDecimal.valueOf(detail.getQuantity())));
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                GoodsReceiptDetailDTO newDetail = new GoodsReceiptDetailDTO();
                newDetail.setProductId(product.getProductId());
                newDetail.setPrice(product.getPrice());
                newDetail.setQuantity(1);
                newDetail.setTotal(product.getPrice());

                if (tblGoodsReceiptDetails.getItems() == null) {
                    tblGoodsReceiptDetails.setItems(FXCollections.observableArrayList());
                }

                tblGoodsReceiptDetails.getItems().add(newDetail);
            }

            tblGoodsReceiptDetails.refresh();
            updateReceiptTotal();
        }

        private void openManufacturerSelector() {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/medicinedistribution/GUI/SubSelect.fxml"));
                ManufacturerSelectController controller = new ManufacturerSelectController(busFactory);
                loader.setController(controller);

                Parent root = loader.load();

                controller.setSelectionHandler(new SelectionHandler<ManufacturerDTO>() {
                    @Override
                    public void onItemSelected(ManufacturerDTO selectedItem) {
                        selectedManufacturer = selectedItem;
                        txtManufacturer.setText(selectedItem.getManufacturerName());
                    }
                });

                Stage stage = new Stage();
                stage.setTitle("Chọn nhà sản xuất");
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setScene(new Scene(root));
                stage.showAndWait();

            } catch (IOException e) {
                log.error("Error loading manufacturer selector: ", e);
            }
        }
        private void showAddManufacturerDialog() {
            // Create dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Thêm nhà sản xuất mới");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setResizable(false);

            // Create layout
            BorderPane layout = new BorderPane();
            layout.getStyleClass().add("main-container");
            layout.setPrefWidth(500);

            // Header
            HBox header = new HBox();
            header.getStyleClass().add("header-section");
            header.setAlignment(Pos.CENTER);
            Label headerLabel = new Label("Thêm nhà sản xuất mới");
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

            // Manufacturer name
            formGrid.add(new Label("Tên nhà sản xuất:"), 0, 0);
            TextField nameField = new TextField();
            nameField.setPromptText("Nhập tên nhà sản xuất");
            nameField.setPrefWidth(300);
            formGrid.add(nameField, 1, 0);

            // Country
            formGrid.add(new Label("Quốc gia:"), 0, 1);
            TextField countryField = new TextField();
            countryField.setPromptText("Nhập quốc gia");
            countryField.setPrefWidth(300);
            formGrid.add(countryField, 1, 1);

            // Phone
            formGrid.add(new Label("Số điện thoại:"), 0, 2);
            TextField phoneField = new TextField();
            phoneField.setPromptText("Nhập số điện thoại");
            phoneField.setPrefWidth(300);
            formGrid.add(phoneField, 1, 2);

            // Email
            formGrid.add(new Label("Email:"), 0, 3);
            TextField emailField = new TextField();
            emailField.setPromptText("Nhập email");
            emailField.setPrefWidth(300);
            formGrid.add(emailField, 1, 3);

            // Address
            formGrid.add(new Label("Địa chỉ:"), 0, 4);
            TextField addressField = new TextField();
            addressField.setPromptText("Nhập địa chỉ");
            addressField.setPrefWidth(300);
            formGrid.add(addressField, 1, 4);

            // Description
            formGrid.add(new Label("Mô tả:"), 0, 5);
            TextArea descriptionField = new TextArea();
            descriptionField.setPromptText("Nhập mô tả");
            descriptionField.setPrefHeight(60);
            descriptionField.setPrefWidth(300);
            formGrid.add(descriptionField, 1, 5);

            // Error message
            Label errorLabel = new Label();
            errorLabel.getStyleClass().add("error-label");
            errorLabel.setVisible(false);

            // Buttons
            HBox buttonBox = new HBox(10);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
            buttonBox.setPadding(new Insets(15, 0, 0, 0));

            Button cancelButton = new Button("Hủy");
            cancelButton.getStyleClass().add("secondary-button");
            cancelButton.setOnAction(e -> dialogStage.close());

            Button saveButton = new Button("Lưu");
            saveButton.getStyleClass().add("success-button");
            saveButton.setOnAction(e -> {
                try {
                    // Validate input
                    if (nameField.getText().trim().isEmpty()) {
                        errorLabel.setText("Tên nhà sản xuất không được để trống");
                        errorLabel.setVisible(true);
                        return;
                    }

                    if (countryField.getText().trim().isEmpty()) {
                        errorLabel.setText("Quốc gia không được để trống");
                        errorLabel.setVisible(true);
                        return;
                    }

                    if (addressField.getText().trim().isEmpty()) {
                        errorLabel.setText("Địa chỉ không được để trống");
                        errorLabel.setVisible(true);
                        return;
                    }

                    // Validate email format if provided
                    String email = emailField.getText().trim();
                    if (!email.isEmpty() && !email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                        errorLabel.setText("Email không hợp lệ");
                        errorLabel.setVisible(true);
                        return;
                    }

                    // Validate phone number if provided
                    String phone = phoneField.getText().trim();
                    if (!phone.isEmpty() && !phone.matches("^(?:(?:0|\\+84)\\d{9}|\\+\\d{1,3}(?:[ \\-]\\d{1,4}){2,4})$")) {
                        errorLabel.setText("Số điện thoại không hợp lệ");
                        errorLabel.setVisible(true);
                        return;
                    }

                    // Create manufacturer object
                    ManufacturerDTO newManufacturer = ManufacturerDTO.builder()
                            .manufacturerName(nameField.getText().trim())
                            .country(countryField.getText().trim())
                            .phone(phoneField.getText().trim())
                            .email(emailField.getText().trim())
                            .address(addressField.getText().trim())
                            .description(descriptionField.getText().trim())
                            .build();

                    // Save manufacturer
                    if (!manufacturerBUS.insert(newManufacturer)) {
                        NotificationUtil.showErrorNotification("Lỗi", "Không thể thêm nhà sản xuất");
                    }

                    // Update the selected manufacturer and UI
                    selectedManufacturer = newManufacturer;
                    txtManufacturer.setText(selectedManufacturer.getManufacturerName());

                    // Update manufacturer list

                    NotificationUtil.showSuccessNotification("Thành công",
                            "Đã thêm nhà sản xuất " + newManufacturer.getManufacturerName());

                    dialogStage.close();

                } catch (Exception ex) {
                    errorLabel.setText("Lỗi: " + ex.getMessage());
                    errorLabel.setVisible(true);
                    log.error("Error adding manufacturer: {}", ex.getMessage(), ex);
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

        private void createGoodsReceipt() {
            if (valid()) {
                GoodsReceiptDTO receipt = createReceiptFromForm();
                try {
                    goodsReceiptBUS.insert(receipt);
                    NotificationUtil.showSuccessNotification("Thành công", "Phiếu nhập đã được lưu thành công");
                    btnClear.fire();
                } catch (InsertFailedException | PermissionDeniedException e) {
                    NotificationUtil.showErrorNotification("Lỗi", e.getMessage());
                } catch (Exception e){
                    NotificationUtil.showErrorNotification("Lỗi", "Có lỗi không xác định: " + e.getMessage());
                }
            }
        }

        private void printGoodsReceipt() {
            if (!valid()) {
                return;
            }

            GoodsReceiptDTO receipt = createReceiptFromForm();
            try {
                // First save the receipt if it's not already saved
                if (receipt.getGoodsReceiptId() == null) {
                    goodsReceiptBUS.insert(receipt);
                }

                // Then print it
                viewReceiptDetails(receipt);
            } catch (Exception e) {
                NotificationUtil.showErrorNotification("Lỗi", "Không thể in phiếu nhập: " + e.getMessage());
            }
        }

        public GoodsReceiptDTO createReceiptFromForm() {
            GoodsReceiptDTO receipt = new GoodsReceiptDTO();
            receipt.setManufacturerId(selectedManufacturer.getManufacturerId());
            receipt.setEmployeeId(userSession.getEmployee().getEmployeeId());
            receipt.setDate(LocalDate.now());
            receipt.setTotal(getReceiptTotal());
            receipt.setDetails(new ArrayList<>(tblGoodsReceiptDetails.getItems()));
            return receipt;
        }

        public BigDecimal getReceiptTotal() {
            BigDecimal total = BigDecimal.ZERO;
            for (GoodsReceiptDetailDTO item : tblGoodsReceiptDetails.getItems()) {
                total = total.add(item.getTotal());
            }
            return total;
        }

        public boolean valid() {
            if (selectedManufacturer == null) {
                NotificationUtil.showErrorNotification("Lỗi", "Vui lòng chọn nhà sản xuất");
                return false;
            }

            if (tblGoodsReceiptDetails.getItems() == null || tblGoodsReceiptDetails.getItems().isEmpty()) {
                NotificationUtil.showErrorNotification("Lỗi", "Vui lòng thêm sản phẩm vào phiếu nhập");
                return false;
            }

            if (lblTotal.getText().equals("0₫")) {
                NotificationUtil.showErrorNotification("Lỗi", "Tổng tiền không thể bằng 0");
                return false;
            }

            for (GoodsReceiptDetailDTO item : tblGoodsReceiptDetails.getItems()) {
                if (item.getQuantity() <= 0) {
                    NotificationUtil.showErrorNotification("Lỗi", "Số lượng sản phẩm không thể bằng 0");
                    return false;
                }
                if (item.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                    NotificationUtil.showErrorNotification("Lỗi", "Giá sản phẩm không thể bằng 0");
                    return false;
                }
                if (item.getTotal().compareTo(BigDecimal.ZERO) <= 0) {
                    NotificationUtil.showErrorNotification("Lỗi", "Tổng tiền sản phẩm không thể bằng 0");
                    return false;
                }
            }



            return true;
        }

        private void initGoodsReceiptTab() {
            setupUIReceiptData();
            setupUIReceiptTab();
            setUpReceiptTable();
            loadReceipts();
        }

        private void setupUIReceiptTab() {
            btnSearchGoodsReceipt.setOnAction(event -> loadReceipts());
        }

        private void setupUIReceiptData() {
            if (receiptList != null) {
                receiptList.clear();
            }
            if (manufacturerList != null) {
                manufacturerList.clear();
            }
            if (employeeList != null) {
                employeeList.clear();
            }

            manufacturerList = new HashMap<>();
            employeeList = new HashMap<>();

            receiptList = new ArrayList<>(goodsReceiptBUS.findAll());

            for (ManufacturerDTO manufacturer : manufacturerBUS.findAll()) {
                manufacturerList.put(manufacturer.getManufacturerId(), manufacturer);
            }

            for (EmployeeDTO employee : employeeBUS.findAll()) {
                employeeList.put(employee.getEmployeeId(), employee);
            }
        }

        private void loadReceipts() {
            LocalDate fromDate = dpFromDate.getValue();
            LocalDate toDate = dpToDate.getValue();
            String searchText = txtSearchGoodsReceipt.getText().toLowerCase();

            List<GoodsReceiptDTO> filteredReceipts = goodsReceiptBUS.findAll().stream()
                    .filter(receipt -> (fromDate == null || !receipt.getDate().isBefore(fromDate))
                            && (toDate == null || !receipt.getDate().isAfter(toDate))
                            && (searchText.isEmpty()
                               || receipt.getGoodsReceiptId().toString().contains(searchText)
                                || employeeList.get(receipt.getEmployeeId()).getFullName().contains(searchText)
                               || manufacturerList.get(receipt.getManufacturerId()).getManufacturerName().toLowerCase().contains(searchText)))
                    .collect(Collectors.toList());

            tblGoodsReceipts.setItems(FXCollections.observableList(filteredReceipts));
            tblGoodsReceipts.refresh();
        }

        public void setUpReceiptTable() {
            colGoodsReceiptId.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getGoodsReceiptId()));

            colGoodsReceiptDate.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getDate()));

            colGoodsReceiptManufacturer.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(
                            manufacturerList.get(cellData.getValue().getManufacturerId()).getManufacturerName()
                    ));

            colGoodsReceiptEmployee.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(
                            employeeList.get(cellData.getValue().getEmployeeId()).getFullName()
                    ));

            colGoodsReceiptTotal.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getTotal()));

            colGoodsReceiptActions.setCellFactory(col -> new TableCell<>() {
                private final Button viewButton = new Button("Xem chi tiết");

                {
                    viewButton.getStyleClass().add("primary-button");
                    viewButton.setOnAction(event -> {
                        GoodsReceiptDTO receipt = getTableView().getItems().get(getIndex());
                        viewReceiptDetails(receipt);
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(viewButton);
                        setAlignment(Pos.CENTER);
                    }
                }
            });
            colGoodsReceiptActions.setStyle("-fx-alignment: CENTER;");
        }

        private void viewReceiptDetails(GoodsReceiptDTO receipt) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("GoodsReceiptDetail.fxml"));
                GoodsReceiptDetailController controller = new GoodsReceiptDetailController(busFactory, receipt);
                loader.setController(controller);
                ImageView icon = new ImageView(new Image(Objects.requireNonNull(getClass().getResource("../../../../img/logo.png"))
                        .toExternalForm()));

                Parent root = loader.load();
                Stage stage = new Stage();
                stage.getIcons().add(icon.getImage());
                stage.setTitle("Chi tiết phiếu nhập");
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setScene(new Scene(root));
                stage.showAndWait();

            } catch (IOException e) {
                log.error("Error loading goods receipt detail view: ", e);
            }
        }
    }