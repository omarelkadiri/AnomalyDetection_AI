package com.example.anomalydetection.viewController;

import com.example.anomalydetection.Alerting.Alert;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class AlertItemController {
    private static int id_alert = 1;
    private LocalDateTime startTime;

    @FXML
    private Label id, severity, lastSeen;

    @FXML
    private ImageView severityIcon;

    @FXML
    private ComboBox<String> stateComboBox;


    private Alert alert;

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setData(Alert alert) {
        this.alert = alert;
        id.setText(String.valueOf(id_alert++));
        severity.setText(alert.getSeverity().toString());
        switch(alert.getSeverity()) {
            case LOW -> severity.setStyle("-fx-text-fill: #00FF00;");
            case MEDIUM -> severity.setStyle("-fx-text-fill: #FFA500;");
            case HIGH -> severity.setStyle("-fx-text-fill: #ff0000;");
            case CRITICAL -> severity.setStyle("-fx-text-fill: #8B0000;");
        };

        switch(alert.getSeverity()) {
            case LOW -> severityIcon.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/view/images/alert_low.png"))));
            case MEDIUM -> severityIcon.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/view/images/alert_medium.png"))));
            case HIGH -> severityIcon.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/view/images/alert_high.png"))));
            case CRITICAL -> severityIcon.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/view/images/alert_critical.png"))));
        };
        long secondsDifference = Duration.between(startTime, alert.getTimestamp()).getSeconds();
        lastSeen.setText(Long.toString(secondsDifference));
        // stateComboBox.s
    }

    @FXML
    public void openDetails() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/alert-details.fxml"));
            Parent root = loader.load();

            AlertDetailsController controller = loader.getController();
            controller.setDetails(alert.getDetails());

            Stage stage = new Stage();
            stage.setTitle("Alert details");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}