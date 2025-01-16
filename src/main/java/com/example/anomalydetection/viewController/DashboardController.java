package com.example.anomalydetection.viewController;

import com.example.anomalydetection.Alerting.Alert;
import com.example.anomalydetection.Alerting.AlertObserver;
import com.example.anomalydetection.Structure.AnomalyResult;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class DashboardController implements Initializable, AlertObserver {
    private final LocalDateTime startTime = LocalDateTime.now();

    @FXML
    private VBox alertLayout;

    @FXML
    private Label status;

    private List<Alert> alerts = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        alerts = TestUI.getAlerts();

        alerts.forEach(alert -> {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/alert-item.fxml"));
            try {
                HBox hbox = fxmlLoader.load();
                AlertItemController alertItemController = fxmlLoader.getController();
                alertItemController.setData(alert);
                alertItemController.setStartTime(startTime);
                alertLayout.getChildren().add(hbox);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void update(Alert newAlert) {
        alerts.addFirst(newAlert);
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/alert-item.fxml"));
        try {
            HBox hbox = fxmlLoader.load();
            AlertItemController alertItemController = fxmlLoader.getController();
            alertItemController.setData(newAlert);
            alertItemController.setStartTime(startTime);
            alertLayout.getChildren().addFirst(hbox);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        status.setText("");
    }
}
