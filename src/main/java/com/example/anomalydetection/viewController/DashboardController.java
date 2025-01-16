package com.example.anomalydetection.viewController;

import com.example.anomalydetection.Alerting.Alert;
import com.example.anomalydetection.Alerting.AlertObserver;
import com.example.anomalydetection.Structure.AnomalyResult;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
                    // Show error on JavaFX thread if needed
                    System.out.println("Error opening browser: " + e.getMessage());
                });
            }
        }).start();
    }


}
