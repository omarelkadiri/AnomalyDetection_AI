package com.example.anomalydetection.Test;

import com.example.anomalydetection.Alerting.Alert;
import com.example.anomalydetection.Elastic.ElasticsearchService;
import com.example.anomalydetection.Structure.AnomalyResult;
import com.example.anomalydetection.Structure.LogEntry;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TestUI {

    public static void main(String[] args) {
        ElasticsearchService elasticsearchService = new ElasticsearchService();
        List<Alert> alerts = new ArrayList<>();
        List<AnomalyResult> anomalyResults = new ArrayList<>();

        for (int i = 1; i <= 3; i++) {
            List<LogEntry> logEntries = elasticsearchService.convertElasticResponseToLogEntries(
                    elasticsearchService.searchLogsFromLastMinute()
            );

            for (LogEntry log : logEntries) {
                double score = 0.32 * i;
                AnomalyResult anomalyResult = new AnomalyResult(log, false, score, LocalDateTime.now());
                anomalyResults.add(anomalyResult);
            }

            Alert.Severity severity;
            switch (i) {
                case 1:
                    severity = Alert.Severity.LOW;
                    break;
                case 2:
                    severity = Alert.Severity.MEDIUM;
                    break;
                case 3:
                    severity = Alert.Severity.HIGH;
                    break;
                default:
                    severity = Alert.Severity.LOW;
            }

            for (AnomalyResult anomalyResult : anomalyResults) {
                String details = "Plus de détails";
                Alert alert = new Alert(anomalyResult, severity, details);
                alerts.add(alert);
            }
        }

        for (Alert alert : alerts) {
            System.out.println(alert);
        }
        elasticsearchService.close();
    }
}
