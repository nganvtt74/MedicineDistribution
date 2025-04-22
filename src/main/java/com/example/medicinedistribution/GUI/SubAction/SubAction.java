package com.example.medicinedistribution.GUI.SubAction;

import com.example.medicinedistribution.BUS.BUSFactory;
import com.example.medicinedistribution.Util.NotificationUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public abstract class SubAction<T, U> {
    protected BUSFactory busFactory;
    protected T parentController;
    protected U selectedData;
    @FXML
    protected Button btnSubmit;
    @FXML
    protected Button btnCancel;
    @FXML
    protected VBox formContent;
    @FXML
    protected ScrollPane scrollPane;
    @FXML
    protected HBox buttonBar;

    @Getter
    public enum ActionType {
        ADD,
        EDIT
    }

    public SubAction(BUSFactory busFactory, T parentController) {
        this.busFactory = busFactory;
        this.parentController = parentController;
    }

    public SubAction(BUSFactory busFactory, T parentController, U selectedData) {
        this(busFactory, parentController);
        this.selectedData = selectedData;
    }

    /**
     * Generic method to show a dialog based on the provided controller type
     *
     * @param <C> Controller type extending SubAction
     * @param controller Controller instance
     * @param title Dialog title
     */
    public static <C extends SubAction<?, ?>> void showDialog(C controller, String title) {
        try {
            String fxmlPath = "../SubAction.fxml";
            FXMLLoader loader = new FXMLLoader(SubAction.class.getResource(fxmlPath));
            loader.setController(controller);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.DECORATED);
            stage.setTitle(title);
            stage.setScene(new Scene(loader.load()));
            stage.setResizable(false);
            stage.showAndWait();
        } catch (IOException e) {
            log.error("Error loading dialog: ", e);
            NotificationUtil.showErrorNotification("Lỗi", "Không thể tải giao diện: " + e.getMessage());
        }
    }

    // Abstract methods that subclasses need to implement
    protected abstract void createFormFields();
    protected abstract boolean validateForm();
    protected abstract void handleSubmit();
    protected void closeDialog(){
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

}