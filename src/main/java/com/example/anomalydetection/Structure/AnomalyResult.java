package com.example.anomalydetection.Structure;

import java.time.LocalDateTime;

public class AnomalyResult {
    private LogEntry logEntry;
    private boolean isAnomaly;
    private double anomalyScore;
    private LocalDateTime timestamp;

    // Constructeur
    public AnomalyResult(LogEntry logEntry, boolean isAnomaly, double anomalyScore,
                         LocalDateTime timestamp) {
        this.logEntry = logEntry;
        this.isAnomaly = isAnomaly;
        this.anomalyScore = anomalyScore;
        this.timestamp = timestamp;
    }

    // Constructeur par défaut pour Jackson
    public AnomalyResult() {}

    // Getters et Setters
    public LogEntry getLogEntry() { return logEntry; }
    public void setLogEntry(LogEntry logEntry) { this.logEntry = logEntry; }

    public boolean isAnomaly() { return isAnomaly; }
    public void setAnomaly(boolean anomaly) { isAnomaly = anomaly; }

    public double getAnomalyScore() { return anomalyScore; }
    public void setAnomalyScore(double anomalyScore) { this.anomalyScore = anomalyScore; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return String.format(
                "AnomalyResult{logEntry=%s, isAnomaly=%s, anomalyScore=%.3f, timestamp=%s}",
                logEntry, isAnomaly, anomalyScore, timestamp
        );
    }
}
