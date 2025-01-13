package com.example.anomalydetection.Test;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.example.anomalydetection.Elastic.ElasticsearchService;
import com.example.anomalydetection.IForest.IPv6Filter;
import com.example.anomalydetection.Structure.AnomalyResult;
import com.example.anomalydetection.Structure.LogEntry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.anomalydetection.IForest.PretrainedModelService;


import java.util.regex.Pattern;

public class AnomalyDetectionTest {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Regex pour détecter les adresses IPv6
    private static final Pattern IPV6_PATTERN = Pattern.compile(
            "^(?:" +
                    "(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}|" +
                    "(?:[0-9a-fA-F]{1,4}:){1,7}:|" +
                    "(?:[0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|" +
                    "(?:[0-9a-fA-F]{1,4}:){1,5}(?::[0-9a-fA-F]{1,4}){1,2}|" +
                    "(?:[0-9a-fA-F]{1,4}:){1,4}(?::[0-9a-fA-F]{1,4}){1,3}|" +
                    "(?:[0-9a-fA-F]{1,4}:){1,3}(?::[0-9a-fA-F]{1,4}){1,4}|" +
                    "(?:[0-9a-fA-F]{1,4}:){1,2}(?::[0-9a-fA-F]{1,4}){1,5}|" +
                    "[0-9a-fA-F]{1,4}:(?:(?::[0-9a-fA-F]{1,4}){1,6})|" +
                    ":(?:(?::[0-9a-fA-F]{1,4}){1,7}|:)|" +
                    "fe80:(?::[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|" +
                    "::(?:ffff(?::0{1,4}){0,1}:){0,1}(?:(?:25[0-5]|(?:2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(?:25[0-5]|(?:2[0-4]|1{0,1}[0-9]){0,1}[0-9])|" +
                    "(?:[0-9a-fA-F]{1,4}:){1,4}:(?:(?:25[0-5]|(?:2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(?:25[0-5]|(?:2[0-4]|1{0,1}[0-9]){0,1}[0-9])" +
                    ")$"
    );

    public static void main(String[] args) {
        ElasticsearchService esService = new ElasticsearchService();
        PretrainedModelService modelService = new PretrainedModelService();

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        Runnable task = () -> {
            try {
                // Récupérer et analyser les logs
                List<LogEntry> allLogs = esService.convertElasticResponseToLogEntries(esService.searchLogsFromLastMinute());

                if (allLogs.isEmpty()) {
                    System.out.println("Aucun log trouvé pour la dernière minute");
                    return;
                }

                // Filtrer les logs contenant des IPv6
                List<LogEntry> ipv4Logs = IPv6Filter.filterOutIPv6Logs(allLogs);

                System.out.println("\n=== Logs analysés (" +
                        java.time.LocalDateTime.now() + ") ===");
                System.out.println("Nombre total de logs: " + allLogs.size());
                System.out.println("Logs avec IPv6 ignorés: " + (allLogs.size() - ipv4Logs.size()));
                System.out.println("Logs IPv4 à analyser: " + ipv4Logs.size());

                if (ipv4Logs.isEmpty()) {
                    System.out.println("Aucun log IPv4 à analyser");
                    return;
                }

                // Prédire les anomalies seulement pour les logs IPv4
                List<AnomalyResult> anomalyResults = modelService.batchPredict(allLogs);

                // Afficher les résultats
                long anomalyCount = anomalyResults.stream()
                        .filter(AnomalyResult::isAnomaly)
                        .count();

                System.out.println("\nNombre d'anomalies détectées: " + anomalyCount);

                // Afficher les détails des anomalies détectées
                anomalyResults.stream()
                        .filter(AnomalyResult::isAnomaly)
                        .forEach(result -> {
                            LogEntry log = result.getLogEntry();
                            System.out.println("\nAnomalie détectée:");
                            System.out.println("Score: " + result.getAnomalyScore());
                            System.out.println("IP Source: " + log.getSource_ip());
                            System.out.println("Port Source: " + log.getSource_port());
                            System.out.println("IP Destination: " + log.getDest_ip());
                            System.out.println("Port Destination: " + log.getDest_port());
                            System.out.println("Bytes: " + log.getBytes());
                            System.out.println("Direction: " + log.getDirection());
                        });

            } catch (Exception e) {
                System.err.println("Erreur lors de l'analyse des logs: " + e.getMessage());
                e.printStackTrace();
            }
        };

        scheduler.scheduleAtFixedRate(task, 0, 1, TimeUnit.MINUTES);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Arrêt de l'application...");
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
            }
            esService.close();
        }));
    }


}