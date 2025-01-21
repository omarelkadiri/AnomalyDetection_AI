package com.example.anomalydetection.Service;

import com.example.anomalydetection.Alerting.AlertManager;
import com.example.anomalydetection.Alerting.EmailAlertObserver;
import com.example.anomalydetection.Alerting.SlackAlertObserver;
import com.example.anomalydetection.Elastic.ElasticsearchService;
import com.example.anomalydetection.IForest.IPv6Filter;
import com.example.anomalydetection.IForest.PretrainedModelService;
import com.example.anomalydetection.Structure.AnomalyResult;
import com.example.anomalydetection.Structure.LogEntry;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class AnomalyDetectionService {
    private final PretrainedModelService modelService;
    private final ElasticsearchService esService;
    private final AlertManager alertManager;
    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> detectionTask;

    // Statistiques pour le monitoring
    private final AtomicInteger totalLogsProcessed;
    private final AtomicInteger totalAnomaliesDetected;
    private LocalDateTime lastProcessingTime;

    public AnomalyDetectionService() {
        modelService = new PretrainedModelService();
        esService = new ElasticsearchService();
        alertManager = new AlertManager();
        scheduler = Executors.newScheduledThreadPool(1);
        totalLogsProcessed = new AtomicInteger(0);
        totalAnomaliesDetected = new AtomicInteger(0);


        // Configuration des observateurs
        EmailAlertObserver emailObserver = new EmailAlertObserver();
        SlackAlertObserver slackObserver = new SlackAlertObserver();

        alertManager.attach(emailObserver);
        alertManager.attach(slackObserver);
    }

    public AnomalyDetectionService(
            PretrainedModelService modelService,
            ElasticsearchService esService,
            AlertManager alertManager) {
        this.modelService = modelService;
        this.esService = esService;
        this.alertManager = alertManager;
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.totalLogsProcessed = new AtomicInteger(0);
        this.totalAnomaliesDetected = new AtomicInteger(0);
    }


    private void displayCurrentStatsConsole() {
        AnomalyDetectionService.AnomalyStats stats = getAnomalyStats();
        System.out.println("\n=== Statistiques de monitoring (" +
                java.time.LocalDateTime.now() + ") ===");
        System.out.println("Logs traités: " + stats.getTotalLogsProcessed());
        System.out.println("Anomalies détectées: " + stats.getTotalAnomaliesDetected());
        System.out.println("Dernier traitement: " + stats.getLastProcessingTime());
    }

    public void start() {
        // Démarrer la détection périodique
        detectionTask = scheduler.scheduleAtFixedRate(
                this::processNewLogs,
                0,
                1,
                TimeUnit.MINUTES
        );
    }

    public void stop() {
        if (detectionTask != null) {
            detectionTask.cancel(false);
        }
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        esService.close();
    }

    public void processNewLogs() {
        try {
            // Récupérer les logs de la dernière minute
            String elasticResponse = esService.searchLogsFromLastMinute();
            List<LogEntry> allLogs = esService.convertElasticResponseToLogEntries(elasticResponse);

            if (allLogs.isEmpty()) {
                System.out.println("Aucun nouveau log à analyser");
                return;
            }

            // Filtrer les logs IPv6
            List<LogEntry> ipv4Logs = IPv6Filter.filterOutIPv6Logs(allLogs);

            // Mettre à jour les statistiques
            totalLogsProcessed.addAndGet(ipv4Logs.size());
            lastProcessingTime = LocalDateTime.now();

            if (ipv4Logs.isEmpty()) {
                return;
            }

            // Détecter les anomalies
            List<AnomalyResult> anomalyResults = modelService.batchPredict(ipv4Logs);

            // Traiter les anomalies détectées
            processAnomalyResults(anomalyResults);

        } catch (Exception e) {
            System.err.println("Erreur lors du traitement des logs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void processAnomalyResults(List<AnomalyResult> anomalyResults) {
        int newAnomaliesDetected = 0;
        for (AnomalyResult anomalyResult : anomalyResults) {
            if (anomalyResult.isAnomaly()){
              //  System.out.println(anomalyResult);  // test valide : s'affiche correctement
                createAlertForAnomaly(anomalyResult);
                newAnomaliesDetected++;
            }
        }

        totalAnomaliesDetected.addAndGet(newAnomaliesDetected);
    }

    private void createAlertForAnomaly(AnomalyResult anomalyResult) {
        // Déléguer la création d'alerte à l'AlertManager
        alertManager.createAlert(anomalyResult);
    }

    // Méthodes pour obtenir les statistiques de détection
    public AnomalyStats getAnomalyStats() {
        return new AnomalyStats(
                totalLogsProcessed.get(),
                totalAnomaliesDetected.get(),
                lastProcessingTime
        );
    }

    // Classe interne pour les statistiques
    public static class AnomalyStats {
        private final int totalLogsProcessed;
        private final int totalAnomaliesDetected;
        private final LocalDateTime lastProcessingTime;

        public AnomalyStats(int totalLogsProcessed, int totalAnomaliesDetected, LocalDateTime lastProcessingTime) {
            this.totalLogsProcessed = totalLogsProcessed;
            this.totalAnomaliesDetected = totalAnomaliesDetected;
            this.lastProcessingTime = lastProcessingTime;
        }

        public int getTotalLogsProcessed() {
            return totalLogsProcessed;
        }

        public int getTotalAnomaliesDetected() {
            return totalAnomaliesDetected;
        }

        public LocalDateTime getLastProcessingTime() {
            return lastProcessingTime;
        }

        @Override
        public String toString() {
            return String.format(
                    "Statistiques de détection:\n" +
                            "- Logs traités: %d\n" +
                            "- Anomalies détectées: %d\n" +
                            "- Dernier traitement: %s",
                    totalLogsProcessed,
                    totalAnomaliesDetected,
                    lastProcessingTime
            );
        }
    }

    public AlertManager getAlertManager() {
        return alertManager;
    }
}