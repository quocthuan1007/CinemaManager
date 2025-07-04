package com.utc2.cinema.view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class test1 extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            // Tải tệp FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/Login.fxml"));
            // Tải nội dung từ FXML và thiết lập giao diện
            AnchorPane root = loader.load();

            // Thiết lập cảnh (Scene)
            Scene scene = new Scene(root, 1200, 900);
            primaryStage.setTitle("Cinema Manager");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
