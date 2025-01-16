package com.example.anomalydetection.Alerting;

import com.example.anomalydetection.Structure.AnomalyResult;
import com.example.anomalydetection.Structure.LogEntry;

import java.time.LocalDateTime;
import java.util.Date;

public class Alert {
    public enum Severity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    private AnomalyResult anomaly;
    private Severity severity;
    private LocalDateTime timestamp;
    private String description;
    private boolean acknowledged;

    // Constructeur
    public Alert(AnomalyResult anomaly, Severity severity, String description) {
        this.anomaly = anomaly;
        this.severity = severity;
        this.timestamp = LocalDateTime.now();
        this.description = description;
        this.acknowledged = false;
    }

    // Constructeur par défaut pour Jackson
    public Alert() {}

    // Getters et Setters
    public AnomalyResult getAnomaly() { return anomaly; }
    public void setAnomaly(AnomalyResult anomaly) { this.anomaly = anomaly; }

    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isAcknowledged() { return acknowledged; }
    public void setAcknowledged(boolean acknowledged) { this.acknowledged = acknowledged; }

    // Méthode utilitaire pour générer une description détaillée de l'alerte
    public String getDetails() {
        LogEntry log = anomaly.getLogEntry();
        return String.format(
                "Alert{severity=%s, timestamp=%s, source=%s:%d, destination=%s:%d, " +
                        "anomalyScore=%.3f, description='%s', acknowledged=%s}",
                severity, timestamp, log.getSource_ip(), log.getSource_port(),
                log.getDest_ip(), log.getDest_port(), anomaly.getAnomalyScore(),
                description, acknowledged
        );
    }

    @Override
    public String toString() {
        return getDetails();
    }
}