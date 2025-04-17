package com.example.medicinedistribution;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;


public class App extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("Login.fxml"));
        //bo tròn góc

        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Medicine Distribution");
        stage.setScene(scene);
        stage.show();

    }
}
