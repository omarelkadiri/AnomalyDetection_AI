package com.example.anomalydetection.Alerting;

import com.example.anomalydetection.Structure.AnomalyResult;
import java.util.ArrayList;
import java.util.List;

public class AlertManager {
    private final List<AlertObserver> observers;

    public AlertManager() {
        this.observers = new ArrayList<>();
    }

    public void attach(AlertObserver observer) {
        observers.add(observer);
    }

    public void detach(AlertObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers(Alert alert) {
        for (AlertObserver observer : observers) {
            observer.update(alert);
        }
    }

    public void createAlert(AnomalyResult anomaly) {
        // Logique pour déterminer la sévérité basée sur le score d'anomalie
        Alert.Severity severity;
        if (anomaly.getAnomalyScore() >= 0.9) {
            severity = Alert.Severity.CRITICAL;
        } else if (anomaly.getAnomalyScore() >= 0.8) {
            severity = Alert.Severity.HIGH;
        } else if (anomaly.getAnomalyScore() >= 0.7) {
            severity = Alert.Severity.MEDIUM;
        } else {
            severity = Alert.Severity.LOW;
        }

        String description = String.format(
                "Anomalie détectée - Score: %.2f - Source IP: %s - Destination IP: %s",
                anomaly.getAnomalyScore(),
                anomaly.getLogEntry().getSource_ip(),
                anomaly.getLogEntry().getDest_ip()
        );

        Alert alert = new Alert(anomaly, severity, description);
        //System.out.println("Test creation alerte : " + alert.getDetails());  // test valid : s'affiche correctement
        notifyObservers(alert);
    }
}