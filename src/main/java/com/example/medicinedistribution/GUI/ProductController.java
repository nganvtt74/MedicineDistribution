package com.example.medicinedistribution.GUI;

import com.example.medicinedistribution.BUS.BUSFactory;
import com.example.medicinedistribution.BUS.Interface.CategoryBUS;
import com.example.medicinedistribution.BUS.Interface.ProductBUS;
import com.example.medicinedistribution.DTO.CategoryDTO;
import com.example.medicinedistribution.DTO.ProductDTO;
import com.example.medicinedistribution.DTO.UserSession;
import com.example.medicinedistribution.Exception.DeleteFailedException;
import com.example.medicinedistribution.Exception.PermissionDeniedException;
import com.example.medicinedistribution.GUI.SubAction.ProductAction;
import com.example.medicinedistribution.Util.NotificationUtil;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ProductController {
    //UI fields
    @FXML
    private TextField txtSearch;

    @FXML
    private ComboBox<CategoryDTO> cboCategory;

    @FXML
    private Button btnRefresh;

    @FXML
    private Button btnAdd;

    @FXML
    private Button btnEdit;

    @FXML
    private Button btnDelete;

    @FXML
    private TableView<ProductDTO> tblProducts;

    @FXML
    private TableColumn<ProductDTO, Integer> colProductId;

    @FXML
    private TableColumn<ProductDTO, String> colProductName;

    @FXML
    private TableColumn<ProductDTO, BigDecimal> colPrice;

    @FXML
    private TableColumn<ProductDTO, String> colUnit;

    @FXML
    private TableColumn<ProductDTO, Integer> colStockQuantity;

    @FXML
    private TableColumn<ProductDTO, String> colStatus;

    @FXML
    private TableColumn<ProductDTO, String> colCategoryId;


    //Functional fields
    private BUSFactory busFactory;
    private ProductBUS productBUS;
    private UserSession userSession;
    private CategoryBUS categoryBUS;
    private ArrayList<ProductDTO> productList;
    private HashMap<Integer, CategoryDTO> categoryMap;


    public ProductController(BUSFactory busFactory) {
        this.busFactory = busFactory;
    }

    public void initialize() {
        productBUS = busFactory.getProductBUS();
        categoryBUS = busFactory.getCategoryBUS();
        userSession = busFactory.getUserSession();
        setupUIData();
        setupUI();
        // Load products into the table
        setupTable();
        loadProducts();

    }

    private void setupTable() {
        colProductId.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getProductId()));
        colProductName.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getProductName()));
        colPrice.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getPrice()));
        colUnit.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getUnit()));
        colStockQuantity.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getStockQuantity()).asObject());
        colStatus.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().isStatus() ? "Đang kinh doanh" : "Ngừng kinh doanh"));

        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Đang kinh doanh".equals(item)) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    }
                }
            }
        });
        colCategoryId.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(categoryMap.containsKey(cellData.getValue().getCategoryId())
                        ? categoryMap.get(cellData.getValue().getCategoryId()).getCategoryName()
                        : "N/A"));
    }

    private void setupUI() {
        // Set up the table columns
        // Set up button actions
        btnAdd.setOnAction(event -> handleAddProduct());
        btnEdit.setOnAction(event -> handleEditProduct());
        btnRefresh.setOnAction(event -> {
            setupUIData();
            loadProducts();
            if (!cboCategory.getItems().isEmpty()) {
                cboCategory.getSelectionModel().select(0);
            }
            txtSearch.clear();

        });
        btnDelete.setOnAction(event -> {
            DeleteHandle();
        });

        List<CategoryDTO> categories = new ArrayList<>(categoryMap.values());
        categories.stream().sorted(
                (c1, c2) -> c1.getCategoryName().compareTo(c2.getCategoryName())
        );
        cboCategory.getItems().clear();
        cboCategory.getItems().add(null);
        cboCategory.getItems().addAll(categories);
        cboCategory.getSelectionModel().selectFirst();
        cboCategory.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            filterProducts();
        });
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filterProducts();
        });


    }
    private void filterProducts() {
        String searchText = txtSearch.getText().toLowerCase();
        CategoryDTO selectedCategory = cboCategory.getSelectionModel().getSelectedItem();

        List<ProductDTO> filteredProducts = productList.stream()
                .filter(product ->
                        (selectedCategory == null || Objects.equals(product.getCategoryId(), selectedCategory.getCategoryId()))
                                && (searchText.isEmpty() || product.getProductName().toLowerCase().contains(searchText))
                )
                .collect(Collectors.toList());

        tblProducts.setItems(FXCollections.observableList(filteredProducts));
        tblProducts.refresh();
    }

    private void DeleteHandle() {
        ProductDTO selectedProduct = tblProducts.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            NotificationUtil.showErrorNotification("Lỗi", "Vui lòng chọn sản phẩm để xóa.");
            return;
        }

        if (!NotificationUtil.showConfirmation("Xác nhận", "Bạn có chắc chắn muốn xóa sản phẩm này?")) {
            return;
        }
        try {
            if(productBUS.delete(selectedProduct.getProductId())){
                NotificationUtil.showSuccessNotification("Thành công", "Xóa sản phẩm thành công.");
                setupUIData();
                loadProducts();
                if (!cboCategory.getItems().isEmpty()) {
                    cboCategory.getSelectionModel().selectFirst();
                }
            }

        } catch (DeleteFailedException | PermissionDeniedException e) {
            NotificationUtil.showErrorNotification("Lỗi",  e.getMessage());
        } catch (Exception e) {
            NotificationUtil.showErrorNotification("Lỗi", "Có lỗi không xác định: " + e.getMessage());
        }
    }

    public void setupUIData(){
        if (productList != null) {
            productList.clear();
        }
        if (categoryMap != null) {
            categoryMap.clear();
        }
        productList = new ArrayList<>(productBUS.findAll());
        categoryMap = new HashMap<>();
        for(CategoryDTO categoryDTO : categoryBUS.findAll()){
            categoryMap.put(categoryDTO.getCategoryId(),categoryDTO);
        }



    }

    public void loadProducts() {
        tblProducts.getItems().clear();
        tblProducts.setItems(FXCollections.observableList(productList));
    }

    @FXML
    private void handleAddProduct() {
        ProductAction.showDialog(busFactory, this, ProductAction.ActionType.ADD);
    }

    @FXML
    private void handleEditProduct() {
        ProductDTO selectedProduct = tblProducts.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            NotificationUtil.showErrorNotification("Lỗi", "Vui lòng chọn sản phẩm để chỉnh sửa.");
            return;
        }
        ProductAction.showDialog(busFactory, this, ProductAction.ActionType.EDIT, selectedProduct);
    }



}
