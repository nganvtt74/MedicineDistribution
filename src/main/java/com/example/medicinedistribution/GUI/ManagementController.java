package com.example.medicinedistribution.GUI;

import com.example.medicinedistribution.BUS.BUSFactory;
import com.example.medicinedistribution.DTO.ComponentInfo;
import com.example.medicinedistribution.Util.NotificationUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Slf4j
public abstract class ManagementController {
    @Setter
    private Runnable backFunction;

    @FXML
    protected Button btnBack;
    @FXML
    protected Button btnLogout;
    @FXML
    protected ImageView logoImage;

    protected BUSFactory busFactory;

    @FXML
    protected StackPane contentArea;
    protected Button activeButton;
    protected List<ComponentInfo> componentInfoList;
    @FXML
    private StackPane logoContainer;


    public abstract void setup();
    public abstract void setupButtonMap() ;

    public ManagementController(BUSFactory busFactory) {
        // Constructor to initialize the controller with the BUSFactory
        // You can use this factory to access your business logic
        this.busFactory = busFactory;
    }
    public void handleBackButton() {
        if (backFunction != null) {
            backFunction.run();
        }
    }

    protected void setupBtnBack() {
        Image backImage = new Image(Objects.requireNonNull(getClass().getResource("../../../../icon/Undo.png")).toExternalForm());
        ImageView backIcon = new ImageView(backImage);
        backIcon.setFitHeight(20);
        backIcon.setFitWidth(20);
        backIcon.setPreserveRatio(true);

        btnBack.setText("Quay lại");
        btnBack.setGraphic(backIcon);
        btnBack.getStyleClass().clear();
        btnBack.getStyleClass().addAll("primary-button");

        btnBack.setStyle("-fx-graphic-text-gap: 4; -fx-padding: 8 15 8 8;" +
                "-fx-border-radius: 5; -fx-background-radius: 5 ; -fx-cursor: hand" );


        btnBack.setOnAction(event -> {
            handleBackButton();
        });
    }
    protected void logout() {
        // Perform logout logic here
        // For example, clear user session, redirect to login screen, etc.
        if (NotificationUtil.showConfirmation("Đăng xuất", "Bạn có chắc chắn muốn đăng xuất không?")) {
            busFactory.getUserSession().clearSession();
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
                loader.setController(new LoginController(busFactory));
                HBox loginScreen = loader.load();
                Stage currentStage = (Stage) btnLogout.getScene().getWindow();
                Stage newStage = new Stage();
                newStage.setTitle("Medicine Distribution");
                newStage.getIcons().add(logoImage.getImage());
                newStage.setScene(new Scene(loginScreen));
                newStage.setResizable(false);
                newStage.show();
                currentStage.close();


            } catch (IOException e) {
                log.error("Error loading Login screen: ", e);
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
                    logoContainer.setDisable(false);

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
