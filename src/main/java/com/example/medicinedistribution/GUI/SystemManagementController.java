package com.example.medicinedistribution.GUI;

import com.example.medicinedistribution.BUS.BUSFactory;
import com.example.medicinedistribution.DTO.ComponentInfo;
import com.example.medicinedistribution.DTO.UserSession;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
@Slf4j
public class SystemManagementController extends ManagementController {

    @FXML
    private Button btnHelp;

    @FXML
    private Button btnRoleManagement;

    @FXML
    private Button btnSystemSettings;

    @FXML
    private Button btnUserAccounts;

    @FXML
    private VBox button_group_vbox;



    @FXML
    private Label headerUserName;



    @FXML
    private ImageView logoImage;

    @FXML
    private VBox parentVBOX;
    @FXML
    private Label lblSetting;
    @FXML
    private VBox vBoxSystemSetting;

    private final int permissionCount;
    private UserSession userSession;

    public SystemManagementController(BUSFactory busFactory , int permissionCount) {
        super(busFactory);
        this.permissionCount = permissionCount;
    }
    @FXML
    public void initialize() {
        userSession = busFactory.getUserSession();
        setup();
        setupButtonMap();
        if (permissionCount > 1) {
            setupBtnBack();
        }else {
            btnBack.setVisible(false);
            btnBack.setManaged(false);
        }
        setupHelpButton();

        btnLogout.setOnAction(event -> {
            logout();
        });
    }

    @Override
    public void setup() {
        headerUserName.setText(userSession.getAccount().getUsername());
    }

    @Override
    public void setupButtonMap() {
            componentInfoList = new ArrayList<>();
            componentInfoList.add(new ComponentInfo(btnUserAccounts, "MANAGE_ACCOUNT", "Account.fxml", new AccountController(busFactory)));
            componentInfoList.add(new ComponentInfo(btnRoleManagement, "MANAGE_ROLE", "Role.fxml", new RoleController(busFactory)));
            componentInfoList.add(new ComponentInfo(btnSystemSettings, "MANAGE_SYSTEM_SETTINGS", "SystemConfig.fxml", new SystemConfigController()));

            log.info("Component info list size: {}", componentInfoList.size());

            boolean defaultLoad = false;
            // Set up action handlers for each component in the list
            for (ComponentInfo info : componentInfoList) {
                if (userSession.hasPermission(info.getPermission())) {
                    Button button = (Button) info.getButton();
                    button.setOnAction(event -> loadFxml(info.getFxmlPath(), info.getController()));
                    if (!defaultLoad){
                        loadFxml(info.getFxmlPath(), info.getController());
                        defaultLoad = true;
                    }
                }else {
                    button_group_vbox.getChildren().remove(info.getButton());
                }
            }



            if (!userSession.hasPermission("MANAGE_SYSTEM_SETTINGS")) {
                parentVBOX.getChildren().removeAll(vBoxSystemSetting, lblSetting);
            }

    }

    private void setupHelpButton() {
        btnHelp.setOnAction(event -> showContactInformation());
        headerUserName.setOnMouseClicked(event -> {
           UserProfileStage profileStage = new UserProfileStage(busFactory);
           profileStage.showAndWait();
        });
    }

    private void showContactInformation() {
        javafx.scene.control.Dialog<Void> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Thông tin liên hệ");
        dialog.setHeaderText("Liên hệ với chúng tôi");
        ImageView logo = new ImageView(getClass().getResource("../../../../img/logo.png").toString());
        logo.setFitWidth(50);
        logo.setFitHeight(50);
        dialog.setGraphic(logo);

        //Dialog icon bar
        ((Stage) dialog.getDialogPane().getScene().getWindow()).getIcons().add(
                new javafx.scene.image.Image(getClass().getResource("../../../../img/logo.png").toString())
        );
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("../../../../css/dialog-style.css").toString()
        );

        // Create a VBox for the content
        VBox content = new VBox(10);
        content.setPadding(new javafx.geometry.Insets(20));
        content.getStyleClass().add("contact-content");
        // Add contact details
        content.getChildren().addAll(
                createContactField("Name:", "Gia Uy"),
                createContactField("Email:", "magiauy46@gmail.com"),
                createContactField("Phone:", "+84 0339702531"),
                createContactField("GitHub:", "github.com/magiauy")
        );


        // Set the content
        dialog.getDialogPane().setContent(content);

        // Add OK button
        dialog.getDialogPane().getButtonTypes().add(javafx.scene.control.ButtonType.OK);

        // Set minimum width
        dialog.getDialogPane().setMinWidth(400);

        // Show the dialog
        dialog.showAndWait();
    }
    private HBox createContactField(String fieldName, String value) {
        HBox field = new HBox(10);
        field.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label fieldLabel = new Label(fieldName);
        fieldLabel.getStyleClass().add("contact-field");
        fieldLabel.setMinWidth(80);

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("contact-value");

        field.getChildren().addAll(fieldLabel, valueLabel);
        return field;
    }



}
