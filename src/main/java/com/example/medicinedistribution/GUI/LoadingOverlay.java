package com.example.medicinedistribution.GUI;

import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoadingOverlay extends StackPane {

    public LoadingOverlay() {
        // Create loading indicator and label
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(100, 100);

        Label loadingLabel = new Label("Đang tải dữ liệu...");
        loadingLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: black;");

        VBox vbox = new VBox(10, progressIndicator, loadingLabel);
        vbox.setAlignment(Pos.CENTER);

        // Style the overlay
//        this.setStyle("-fx-background-color: #f5f5f5;");
        this.getStyleClass().add("loading-overlay");
        this.getChildren().add(vbox);
        this.setAlignment(Pos.CENTER);
    }

    public void hide() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(800), this);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> this.setVisible(false));
        fadeOut.play();
    }
}