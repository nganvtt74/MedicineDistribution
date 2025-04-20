package com.example.medicinedistribution.GUI;

import com.example.medicinedistribution.BUS.BUSFactory;
import com.example.medicinedistribution.BUS.Interface.*;
import com.example.medicinedistribution.DTO.ComponentInfo;
import com.example.medicinedistribution.DTO.UserSession;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@Slf4j
public class SalesManagementController {
    //UI fields
    @FXML
    public Label headerUserName;
    @FXML
    public ImageView logoImage;
    @FXML
    public VBox button_group_vbox;
    @FXML
    public Button btnInvoice ,
            btnCustomer,
            btnProduct,
            btnGoodsReceipt,
            btnManufacturer,
            btnLogout,
            btnStatistics,
            btnSettings;

    @FXML
    public StackPane contentArea;

    private Button activeButton;

    //Functional fields
    private final BUSFactory busFactory;
    private InvoiceBUS invoiceBUS;
    private CustomerBUS customerBUS;
    private ProductBUS productBUS;
    private GoodsReceiptBUS goodsReceiptBUS;
    private ManufacturerBUS manufacturerBUS;
    private UserSession userSession;
    private List<ComponentInfo> componentInfoList;
    
    public SalesManagementController(BUSFactory busFactory) {
        this.busFactory = busFactory;
    }

    public void initialize() {
        invoiceBUS = busFactory.getInvoiceBUS();
        customerBUS = busFactory.getCustomerBUS();
        productBUS = busFactory.getProductBUS();
        goodsReceiptBUS = busFactory.getGoodsReceiptBUS();
        manufacturerBUS = busFactory.getManufacturerBUS();
        userSession = busFactory.getUserSession();
        setup();
        setupButtonMap();
    }

    public void setup(){
        headerUserName.setText(userSession.getAccount().getUsername());



    }

    public void setupButtonMap() {

        componentInfoList = new ArrayList<>();
        componentInfoList.add(new ComponentInfo(btnInvoice,"MANAGE_INVOICE","Invoice.fxml",new InvoiceController(busFactory)));
        componentInfoList.add( new ComponentInfo(btnCustomer,"MANAGE_CUSTOMER","Customer.fxml",new CustomerController(busFactory)));
        componentInfoList.add( new ComponentInfo(btnProduct,"MANAGE_PRODUCT","Product.fxml",new ProductController(busFactory)));
        componentInfoList.add( new ComponentInfo(btnGoodsReceipt,"MANAGE_GOODS_RECEIPT","GoodsReceipt.fxml",new GoodsReceiptController(busFactory)));
        componentInfoList.add( new ComponentInfo(btnManufacturer,"MANAGE_MANUFACTURER","Manufacturer.fxml",new ManufacturerController(busFactory)));
        componentInfoList.add( new ComponentInfo(btnStatistics,"MANAGE_STATISTICS","SalesStatistic.fxml",new SalesStatisticController(busFactory)));
        for (ComponentInfo componentInfo : componentInfoList) {
            if(!userSession.hasPermission(componentInfo.getPermission())) {
                button_group_vbox.getChildren().remove(componentInfo.getButton());
            }else {
                componentInfo.getButton().setOnAction(event -> {
                    String fxmlFile = componentInfo.getFxmlPath();
                    if (fxmlFile != null) {
                        loadFxml(fxmlFile,  componentInfo.getController());
                    }
                });
            }
        }

    }

    public void loadFxml(String fxmlFile,Object controller) {
        try {
            if (activeButton != null) {
                // Remove active class from previous button
                activeButton.getStyleClass().remove("active-button");
                // Re-enable the previous active button
                activeButton.setDisable(false);
            }

            // Get and update the currently clicked button
            Button clickedButton = null;
            for (ComponentInfo info : componentInfoList) {
                if (info.getFxmlPath().equals(fxmlFile)) {
                    clickedButton = info.getButton();
                    break;
                }
            }

            if (clickedButton != null) {
                // Set the new button as active
                clickedButton.getStyleClass().add("active-button");
                // Disable it temporarily to prevent double-clicking
                clickedButton.setDisable(true);
                activeButton = clickedButton;
            }
            // Create a new FXMLLoader

            contentArea.getChildren().clear();

            LoadingOverlay loadingOverlay = new LoadingOverlay();
            contentArea.getChildren().add(loadingOverlay);

            // Load the content in a background thread
            try {
                // Create a new FXMLLoader
                FXMLLoader loader = new FXMLLoader();
                // Set the URL for the FXMLLoader to point to the new FXML file
                loader.setLocation(getClass().getResource(fxmlFile));
                loader.setController(controller);

                // Load the FXML content
                AnchorPane newContent = loader.load();

                // Simulate some loading time if needed
                Thread.sleep(100);

                // Update UI on JavaFX application thread
                Platform.runLater(() -> {
                    // Add the new content behind the overlay
                    contentArea.getChildren().addFirst(newContent);

                    // Hide loading overlay with smooth transition
                    loadingOverlay.hide();

                });
            } catch (Exception e) {
                log.error("Error loading FXML: ", e);
                javafx.application.Platform.runLater(() -> {
                    // Show error message in UI
                    Label errorLabel = new Label("Không thể tải giao diện: " + e.getMessage());
//                    errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");
                    //Set Style class
                    errorLabel.getStyleClass().add("error-label");
                    contentArea.getChildren().clear();
                    contentArea.getChildren().add(errorLabel);
                });
            }

        } catch (ClassCastException e) {
            log.error("ClassCastException: ", e);
        }
    }

}
