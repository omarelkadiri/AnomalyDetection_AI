package com.example.anomalydetection.viewController;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {
    private final LocalDateTime startTime = LocalDateTime.now();

    @FXML
    private AnchorPane container;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            openOverView();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void openOverView() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/overview.fxml"));
        try {
            BorderPane borderPane = fxmlLoader.load();
            container.getChildren().add(borderPane);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void openElasticDashboard() throws IOException {
        new Thread(() -> {
            try {
                URI uri = new URI("http://192.168.1.12:5601/app/dashboards");
                Desktop.getDesktop().browse(uri);
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    System.out.println("Error opening browser: " + e.getMessage());
                });
            }
        }).start();
    }


}
