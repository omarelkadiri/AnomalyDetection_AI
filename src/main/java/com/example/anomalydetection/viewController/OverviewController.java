package com.example.anomalydetection.viewController;

import com.example.anomalydetection.Alerting.Alert;
import com.example.anomalydetection.Alerting.AlertObserver;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class OverviewController implements Initializable, AlertObserver {
    private final LocalDateTime startTime = LocalDateTime.now();

    @FXML
    private VBox alertLayout;

    @FXML
    private Label status;

    @FXML
    private ComboBox<String> filterBySeverity, filterByState;

    private List<Alert> alerts = new ArrayList<>();
    private Map<String, List<Alert>> filteredAlerts = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        alerts = TestUI.getAlerts();
        filteredAlerts = getFilteredAlerts();
        System.out.println(filteredAlerts.values());
        displayStatus();
        filterBySeverity.getItems().addAll("ALL", Alert.Severity.CRITICAL.toString(), Alert.Severity.HIGH.toString(), Alert.Severity.MEDIUM.toString(),Alert.Severity.LOW.toString());
        filterByState.getItems().addAll("TREATED", "NOT TREATED");
        displayAlerts(alerts);
    }

    @Override
    public void update(Alert newAlert) {
        alerts.addFirst(newAlert);
        filteredAlerts = getFilteredAlerts();
        displayStatus();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/alert-item.fxml"));
        try {
            HBox hbox = fxmlLoader.load();
            AlertItemController alertItemController = fxmlLoader.getController();
            alertItemController.setData(newAlert);
            alertItemController.setStartTime(startTime);
            displayAlerts(alerts);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        status.setText("");
    }

    private void displayAlerts(List<Alert> filteredAlerts) {
        filteredAlerts.forEach(alert -> {
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

    private Map<String, List<Alert>> getFilteredAlerts() {
        Map<String, List<Alert>> filteredAlerts = new HashMap<>();
        filteredAlerts.put("ALL", alerts);

        List<Alert> treatedAlerts = alerts.stream()
                .filter(Alert::isAcknowledged)
                .collect(Collectors.toList());
        filteredAlerts.put("TREATED", treatedAlerts);

        List<Alert> notTreatedAlerts = alerts.stream()
                .filter(alert -> !alert.isAcknowledged())
                .collect(Collectors.toList());
        filteredAlerts.put("NOT TREATED", notTreatedAlerts);

        List<Alert> criticalSeverityAlerts = alerts.stream()
                .filter(alert -> alert.getSeverity() == Alert.Severity.CRITICAL)
                .collect(Collectors.toList());
        filteredAlerts.put(Alert.Severity.CRITICAL.toString(), criticalSeverityAlerts);

        List<Alert> highSeverity = alerts.stream()
                .filter(alert -> alert.getSeverity() == Alert.Severity.HIGH)
                .collect(Collectors.toList());
        filteredAlerts.put(Alert.Severity.HIGH.toString(), highSeverity);

        List<Alert> mediumSeverityAlerts = alerts.stream()
                .filter(alert -> alert.getSeverity() == Alert.Severity.MEDIUM)
                .collect(Collectors.toList());
        filteredAlerts.put(Alert.Severity.MEDIUM.toString(), mediumSeverityAlerts);

        List<Alert> lowSeverityAlerts = alerts.stream()
                .filter(alert -> alert.getSeverity() == Alert.Severity.LOW)
                .collect(Collectors.toList());
        filteredAlerts.put(Alert.Severity.LOW.toString(), lowSeverityAlerts);

        return filteredAlerts;
    }

    @FXML
    void handleSeverityFilter(ActionEvent event) {
        String selectedItem = filterBySeverity.getSelectionModel().getSelectedItem();
        System.out.println(selectedItem.toString());
        String severityKey = switch (selectedItem) {
            case "ALL", "CRITICAL", "HIGH", "MEDIUM", "LOW" -> selectedItem;
            default -> "ALL";
        };
        displayAlerts(getFilteredAlerts().get(severityKey));
    }


    @FXML
    void handleStateFilter(ActionEvent event) {
        String selectedItem = filterBySeverity.getSelectionModel().getSelectedItem();
        String severityKey = switch (selectedItem) {
            case "TREATED","NOT TREATED"-> selectedItem;
            default -> "ALL";
        };
        displayAlerts(getFilteredAlerts().get(severityKey));
    }

    private void displayStatus() {
        if (!getFilteredAlerts().get(Alert.Severity.CRITICAL.toString()).isEmpty()) {
            status.setText("Danger");
            status.setStyle("-fx-text-fill: darkRed");
        } else if (!getFilteredAlerts().get(Alert.Severity.HIGH.toString()).isEmpty()) {
            status.setText("Danger");
            status.setStyle("-fx-text-fill: darkRed");
        } else if (getFilteredAlerts().get(Alert.Severity.MEDIUM.toString()).size() > 3) {
            status.setText("Warnings");
            status.setStyle("-fx-text-fill: orange");
        } else {
            status.setText("Good");
            status.setStyle("-fx-text-fill: green");
        }
    }
}
