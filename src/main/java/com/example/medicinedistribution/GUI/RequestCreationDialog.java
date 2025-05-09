package com.example.medicinedistribution.GUI;

import com.example.medicinedistribution.BUS.BUSFactory;
import com.example.medicinedistribution.BUS.Interface.RequestBUS;
import com.example.medicinedistribution.BUS.Interface.RequestTypeBUS;
import com.example.medicinedistribution.DTO.RequestTypeDTO;
import com.example.medicinedistribution.DTO.RequestsDTO;
import com.example.medicinedistribution.Util.NotificationUtil;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

@Slf4j
public class RequestCreationDialog {

    public static void show(BUSFactory busFactory, Window owner) {
        try {
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle("Tạo yêu cầu mới");
            dialogStage.initOwner(owner);
            dialogStage.setResizable(false);
            dialogStage.getIcons().add(new Image(Objects.requireNonNull(RequestCreationDialog.class.getResource("../../../../img/logo.png")).toExternalForm()));

            VBox dialogVbox = new VBox(15);
            dialogVbox.setPadding(new Insets(20));
            dialogVbox.setAlignment(Pos.TOP_CENTER);
            dialogVbox.getStyleClass().add("content-area");

            Label titleLabel = new Label("Tạo yêu cầu mới");
            titleLabel.getStyleClass().add("section-header");

            // Request type selection
            Label typeLabel = new Label("Loại yêu cầu:");
            typeLabel.getStyleClass().add("form-label");

            ComboBox<RequestTypeDTO> typeComboBox = new ComboBox<>();
            typeComboBox.setPrefWidth(250);
            typeComboBox.getStyleClass().add("combo-box");
            RequestTypeBUS requestTypeBUS = busFactory.getRequestTypeBUS();
            List<RequestTypeDTO> requestTypes = requestTypeBUS.findAll();
            typeComboBox.getItems().addAll(requestTypes);

            if (!requestTypes.isEmpty()) {
                typeComboBox.setValue(requestTypes.getFirst());
            }

            // Custom cell factory for the request type combo box
            typeComboBox.setCellFactory(param -> new ListCell<RequestTypeDTO>() {
                @Override
                protected void updateItem(RequestTypeDTO item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getType_name());
                    }
                }
            });

            // Custom string converter for the selected value
            typeComboBox.setButtonCell(new ListCell<RequestTypeDTO>() {
                @Override
                protected void updateItem(RequestTypeDTO item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getType_name());
                    }
                }
            });

            // Date range selection
            Label startDateLabel = new Label("Ngày bắt đầu:");
            startDateLabel.getStyleClass().add("form-label");

            DatePicker startDatePicker = new DatePicker(LocalDate.now());
            startDatePicker.setPrefWidth(250);
            startDatePicker.getStyleClass().add("date-picker");

            Label endDateLabel = new Label("Ngày kết thúc:");
            endDateLabel.getStyleClass().add("form-label");

            DatePicker endDatePicker = new DatePicker(LocalDate.now());
            endDatePicker.setPrefWidth(250);
            endDatePicker.getStyleClass().add("date-picker");

            // Calculate duration automatically
            Label durationLabel = new Label("Thời gian (ngày): 1");
            durationLabel.getStyleClass().addAll("form-label", "label-info");

            IntegerProperty duration = new SimpleIntegerProperty(1);

            startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && endDatePicker.getValue() != null) {
                    long days = ChronoUnit.DAYS.between(newVal, endDatePicker.getValue()) + 1;
                    duration.set((int) days);
                    durationLabel.setText("Thời gian (ngày): " + duration.get());
                }
            });

            endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && startDatePicker.getValue() != null) {
                    long days = ChronoUnit.DAYS.between(startDatePicker.getValue(), newVal) + 1;
                    duration.set((int) days);
                    durationLabel.setText("Thời gian (ngày): " + duration.get());
                }
            });

            // Reason text area
            Label reasonLabel = new Label("Lý do:");
            reasonLabel.getStyleClass().add("form-label");

            TextArea reasonArea = new TextArea();
            reasonArea.setPrefWidth(250);
            reasonArea.setPrefHeight(100);
            reasonArea.setWrapText(true);
            reasonArea.getStyleClass().add("text-area");

            // Submit and cancel buttons
            Button submitButton = new Button("Gửi yêu cầu");
            submitButton.getStyleClass().addAll("button", "primary-button");
            submitButton.setFont(javafx.scene.text.Font.font(14));

            Button cancelButton = new Button("Hủy bỏ");
            cancelButton.getStyleClass().addAll("button", "danger-button");
            cancelButton.setFont(javafx.scene.text.Font.font(14));

            HBox buttonBox = new HBox(15);
            buttonBox.setPadding(new Insets(10, 0, 0, 0));
            buttonBox.setAlignment(Pos.CENTER);

            buttonBox.getChildren().addAll(submitButton, cancelButton);

            // Layout for form elements
            GridPane formGrid = new GridPane();
            formGrid.setHgap(15);
            formGrid.setVgap(15);
            formGrid.setAlignment(Pos.CENTER);
            formGrid.getStyleClass().add("form-section");

            formGrid.add(typeLabel, 0, 0);
            formGrid.add(typeComboBox, 1, 0);
            formGrid.add(startDateLabel, 0, 1);
            formGrid.add(startDatePicker, 1, 1);
            formGrid.add(endDateLabel, 0, 2);
            formGrid.add(endDatePicker, 1, 2);
            formGrid.add(durationLabel, 1, 3);
            formGrid.add(reasonLabel, 0, 4);
            formGrid.add(reasonArea, 1, 4);

            // Add a separator before buttons
            Separator separator = new Separator();
            separator.getStyleClass().add("divider");

            // Action handlers
            cancelButton.setOnAction(e -> dialogStage.close());

            submitButton.setOnAction(e -> {
                if (typeComboBox.getValue() == null) {
                    NotificationUtil.showErrorNotification("Lỗi", "Vui lòng chọn loại yêu cầu");
                    return;
                }

                if (startDatePicker.getValue() == null || endDatePicker.getValue() == null) {
                    NotificationUtil.showErrorNotification("Lỗi", "Vui lòng chọn ngày bắt đầu và kết thúc");
                    return;
                }

                if (duration.get() < 1) {
                    NotificationUtil.showErrorNotification("Lỗi", "Thời gian yêu cầu không hợp lệ");
                    return;
                }

                if (reasonArea.getText().trim().isEmpty()) {
                    NotificationUtil.showErrorNotification("Lỗi", "Vui lòng nhập lý do");
                    return;
                }

                // Create and save the request
                RequestsDTO request = RequestsDTO.builder()
                        .type_id(typeComboBox.getValue().getType_id())
                        .start_date(startDatePicker.getValue())
                        .end_date(endDatePicker.getValue())
                        .duration(duration.get())
                        .employee_id(busFactory.getUserSession().getEmployee().getEmployeeId())
                        .reason(reasonArea.getText().trim())
                        .status("PENDING")
                        .created_at(LocalDate.now())
                        .build();

                try {
                    RequestBUS requestBUS = busFactory.getRequestBUS();
                    boolean success = requestBUS.insert(request);
                    if (success) {
                        NotificationUtil.showSuccessNotification("Thành công", "Yêu cầu đã được gửi thành công");
                        dialogStage.close();
                    } else {
                        NotificationUtil.showErrorNotification("Lỗi", "Không thể gửi yêu cầu");
                    }
                } catch (Exception ex) {
                    log.error("Error submitting request", ex);
                    NotificationUtil.showErrorNotification("Lỗi", "Không thể gửi yêu cầu: " + ex.getMessage());
                }
            });

            dialogVbox.getChildren().addAll(titleLabel, formGrid, separator, buttonBox);
            Scene dialogScene = new Scene(dialogVbox, 450, 500);
            dialogScene.getStylesheets().add(RequestCreationDialog.class.getResource("/css/main-style.css").toExternalForm());

            dialogStage.setScene(dialogScene);
            dialogStage.show();
        } catch (Exception e) {
            log.error("Error opening request form", e);
            NotificationUtil.showErrorNotification("Lỗi", "Không thể mở form tạo yêu cầu: " + e.getMessage());
        }
    }
}