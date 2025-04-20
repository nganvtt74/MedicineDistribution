package com.example.medicinedistribution.GUI.SubAction;

import com.example.medicinedistribution.BUS.BUSFactory;
import com.example.medicinedistribution.DTO.CategoryDTO;
import com.example.medicinedistribution.DTO.ProductDTO;
import com.example.medicinedistribution.GUI.ProductController;
import com.example.medicinedistribution.Util.NotificationUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Locale.filter;

@Slf4j
public class ProductAction extends SubAction<ProductController, ProductDTO> {
    @FXML
    private Label lblHeader;

    @FXML
    private VBox formContent;

    @FXML
    private Button btnSubmit;
    @FXML
    private Button btnCancel;
    private ComboBox<CategoryDTO> categoryComboBox;
    private ComboBox<Boolean> statusComboBox;

    private final Map<String, TextField> formFields = new HashMap<>();
    private final ActionType actionType;
    private List<CategoryDTO> categories;



    public ProductAction(BUSFactory busFactory, ProductController parentController, ActionType actionType, ProductDTO selectedData) {
        super(busFactory, parentController, selectedData);
        this.actionType = actionType;
    }

    @FXML
    public void initialize() {
        // Set the header text based on action type
        lblHeader.setText(actionType == ActionType.ADD ? "Thêm sản phẩm" : "Cập nhật sản phẩm");

        // Clear any sample fields that might be in the FXML
        formContent.getChildren().clear();
        categories = busFactory.getCategoryBUS().findAll();

        // Build the form based on action type
        createFormFields();

        // Set button text based on action type
        btnSubmit.setText(actionType == ActionType.ADD ? "Thêm" : "Cập nhật");

        // Setup button action
        btnSubmit.setOnAction(event -> handleSubmit());
    }

    @Override
    protected void createFormFields() {
        // Create form fields for product
        addFormField("productName", "Tên sản phẩm:",
                selectedData != null ? selectedData.getProductName() : "", "Nhập tên sản phẩm");
        addFormField("price", "Giá:",
                selectedData != null ? selectedData.getPrice().toString() : "","Nhập giá sản phẩm");
        addFormField("unit", "Đơn vị:",
                selectedData != null ? selectedData.getUnit() : "","Nhập đơn vị sản phẩm");
        addFormField("stockQuantity", "Số lượng tồn:",
                selectedData != null ? String.valueOf(selectedData.getStockQuantity()) : "","Nhập số lượng tồn");
        addComboBoxField("category", "Danh mục:",
                selectedData != null && selectedData.getProductId() != null ?
                        selectedData.getCategoryId() : null, "Chọn danh mục");
        // Add cancel button action
        if (actionType == ActionType.EDIT) {
            addStatusComboBox("status", "Trạng thái:",
                    selectedData == null || selectedData.isStatus(), "Chọn trạng thái");
        }
        btnCancel.setOnAction(event -> ((Stage) btnCancel.getScene().getWindow()).close());
    }

    private void addStatusComboBox(String fieldName, String labelText, Boolean defaultValue, String placeholder) {
        HBox fieldContainer = new HBox(10);
        fieldContainer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label label = new Label(labelText);
        label.setPrefWidth(150);
        label.setFont(new Font(14));

        statusComboBox = new ComboBox<>();
        statusComboBox.setPromptText(placeholder);
        statusComboBox.setPrefWidth(200);
        statusComboBox.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(statusComboBox, javafx.scene.layout.Priority.ALWAYS);

        // Only allow selection, not free text entry
        statusComboBox.setEditable(false);

        // Add true and false options
        statusComboBox.getItems().addAll(true, false);

        // Set default value if available
        statusComboBox.setValue(defaultValue != null ? defaultValue : true);
        statusComboBox.setCellFactory(listView -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label statusLabel = new Label(item ? "Đang kinh doanh" : "Ngừng kinh doanh");
                    statusLabel.setFont(Font.font(statusLabel.getFont().getFamily(), javafx.scene.text.FontWeight.BOLD, 12));

                    if (item) {
                        statusLabel.setTextFill(javafx.scene.paint.Color.GREEN);
                    } else {
                        statusLabel.setTextFill(javafx.scene.paint.Color.RED);
                    }

                    setGraphic(statusLabel);
                    setText(null);
                }
            }
        });

        // Also update the button area to show colored text
        statusComboBox.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label statusLabel = new Label(item ? "Đang kinh doanh" : "Ngừng kinh doanh");
                    statusLabel.setFont(Font.font(statusLabel.getFont().getFamily(), javafx.scene.text.FontWeight.BOLD, 12));

                    if (item) {
                        statusLabel.setTextFill(javafx.scene.paint.Color.GREEN);
                    } else {
                        statusLabel.setTextFill(javafx.scene.paint.Color.RED);
                    }

                    setGraphic(statusLabel);
                    setText(null);
                }
            }
        });
        // Configure display of boolean values
        statusComboBox.setConverter(new javafx.util.StringConverter<Boolean>() {
            @Override
            public String toString(Boolean status) {
                return status != null ? (status ? "Đang kinh doanh" : "Ngừng kinh doanh") : "";
            }

            @Override
            public Boolean fromString(String string) {
                if ("Đang kinh doanh".equals(string)) return true;
                if ("Ngừng kinh doanh".equals(string)) return false;
                return null;
            }
        });

        fieldContainer.getChildren().addAll(label, statusComboBox);
        formContent.getChildren().add(fieldContainer);
    }

    private void addComboBoxField(String fieldName, String labelText, Integer defaultValue, String placeholder) {
     HBox fieldContainer = new HBox(10);
     fieldContainer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

     Label label = new Label(labelText);
     label.setPrefWidth(150);
     label.setFont(new Font(14));

     categoryComboBox = new ComboBox<>();
     categoryComboBox.setPromptText(placeholder);
     categoryComboBox.setPrefWidth(200);
     categoryComboBox.setMaxWidth(Double.MAX_VALUE);
     HBox.setHgrow(categoryComboBox, javafx.scene.layout.Priority.ALWAYS);

     // Only allow selection, not free text entry
     categoryComboBox.setEditable(false);

     // Configure display and conversion of categories
     categoryComboBox.setConverter(new javafx.util.StringConverter<CategoryDTO>() {
         @Override
         public String toString(CategoryDTO category) {
             return category != null ? category.getCategoryName() : "";
         }

         @Override
         public CategoryDTO fromString(String string) {
             return categories.stream()
                     .filter(category -> category.getCategoryName().equals(string))
                     .findFirst()
                     .orElse(null);
         }
     });

     // Ensure categories is not null before adding
     if (categories != null && !categories.isEmpty()) {
         categoryComboBox.getItems().addAll(categories);

         // Set default value if available
         if (defaultValue != null) {
             CategoryDTO defaultCategory = categories.stream()
                     .filter(category -> category.getCategoryId().equals(defaultValue))
                     .findFirst()
                     .orElse(null);
             categoryComboBox.setValue(defaultCategory);
         }
     }

     fieldContainer.getChildren().addAll(label, categoryComboBox);
     formContent.getChildren().add(fieldContainer);
 }

    private void addFormField(String fieldName, String labelText, String defaultValue ,String placeholder) {
        HBox fieldContainer = new HBox(10);
        fieldContainer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label label = new Label(labelText);
        label.setPrefWidth(150);
        label.setFont(new Font(14));

        TextField textField = new TextField(defaultValue);
        textField.setFont(new Font(14));
        textField.setPromptText(placeholder);
        HBox.setHgrow(textField, javafx.scene.layout.Priority.ALWAYS);

        fieldContainer.getChildren().addAll(label, textField);
        formContent.getChildren().add(fieldContainer);

        formFields.put(fieldName, textField);
    }
    @Override
    protected void handleSubmit() {
        if (validateForm()) {
            try {
                if (actionType == ActionType.ADD) {
                    addProduct();
                } else {
                    updateProduct();
                }

                // Close the window after successful operation
                ((Stage) btnSubmit.getScene().getWindow()).close();

                // Refresh parent view
                parentController.setupUIData();
                parentController.loadProducts();
            } catch (Exception e) {
                log.error("Error during product action: ", e);
                NotificationUtil.showErrorNotification("Lỗi", "Có lỗi xảy ra trong quá trình xử lý: " + e.getMessage());
            }
        }
    }

    @Override
    protected boolean validateForm() {
        // Basic validation
        for (Map.Entry<String, TextField> entry : formFields.entrySet()) {
            if (entry.getValue().getText().trim().isEmpty()) {
                NotificationUtil.showErrorNotification("Lỗi", entry.getKey() + " không được để trống!");
                entry.getValue().requestFocus();
                return false;
            }
        }

        // Validate price is a number
        try {
            Double.parseDouble(formFields.get("price").getText());
        } catch (NumberFormatException e) {
            NotificationUtil.showErrorNotification("Lỗi", "Giá sản phẩm phải là số!");
            formFields.get("price").requestFocus();
            return false;
        }

        // Validate stock quantity is an integer
        try {
            Integer.parseInt(formFields.get("stockQuantity").getText());
        } catch (NumberFormatException e) {
            NotificationUtil.showErrorNotification("Lỗi", "Số lượng tồn phải là số nguyên!");
            formFields.get("stockQuantity").requestFocus();
            return false;
        }
        categories = busFactory.getCategoryBUS().findAll();


        return true;
    }

    private void addProduct() {
        // Implementation for adding a product goes here
        // Example:
        ProductDTO newProduct = createProductFromForm();
        busFactory.getProductBUS().insert(newProduct);
    }

    private void updateProduct() {
        // Implementation for updating a product goes here
        // Example:
        ProductDTO updatedProduct = createProductFromForm();
        updatedProduct.setProductId(selectedData.getProductId());
        busFactory.getProductBUS().update(updatedProduct);
    }

    private ProductDTO createProductFromForm() {
        // Create and return a ProductDTO from form data
        ProductDTO product = new ProductDTO();
        product.setProductName(formFields.get("productName").getText());
        product.setPrice(java.math.BigDecimal.valueOf(Double.parseDouble(formFields.get("price").getText())));
        product.setUnit(formFields.get("unit").getText());
        product.setStockQuantity(Integer.parseInt(formFields.get("stockQuantity").getText()));

        // Set category (would need to retrieve the actual CategoryDTO in real implementation)
        CategoryDTO selectedCategory = categoryComboBox.getValue();
        if (selectedCategory != null) {
            product.setCategoryId(selectedCategory.getCategoryId());
        }

        if(ActionType.ADD.equals(actionType)) {
            product.setStatus(true);
        }

        return product;
    }

        // Static helper methods for showing ProductAction dialogs
    public static void showDialog(BUSFactory busFactory, ProductController parentController, ActionType actionType) {
        showDialog(busFactory, parentController, actionType, null);
    }

    public static void showDialog(BUSFactory busFactory, ProductController parentController, ActionType actionType, ProductDTO selectedItem) {
        ProductAction controller = new ProductAction(busFactory, parentController, actionType, selectedItem);
        SubAction.showDialog(controller, actionType == ActionType.ADD ? "Thêm sản phẩm" : "Cập nhật sản phẩm");
    }
}