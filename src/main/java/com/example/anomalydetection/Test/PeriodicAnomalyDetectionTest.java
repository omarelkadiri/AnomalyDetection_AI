package com.example.anomalydetection.Test;


import com.example.anomalydetection.Alerting.Alert;
import com.example.anomalydetection.Alerting.AlertManager;
import com.example.anomalydetection.Alerting.AlertObserver;
import com.example.anomalydetection.Alerting.EmailAlertObserver;
import com.example.anomalydetection.Elastic.ElasticsearchService;
import com.example.anomalydetection.Service.AnomalyDetectionService;
import com.example.anomalydetection.IForest.PretrainedModelService;
import com.example.anomalydetection.Structure.LogEntry;

public class PeriodicAnomalyDetectionTest implements AlertObserver {
    private final AnomalyDetectionService detectionService;

    public PeriodicAnomalyDetectionTest() {
        // Initialisation des services
        PretrainedModelService modelService = new PretrainedModelService();
        ElasticsearchService esService = new ElasticsearchService();
        AlertManager alertManager = new AlertManager();

        // Configuration des observateurs
        EmailAlertObserver emailObserver = new EmailAlertObserver();
        alertManager.attach(emailObserver);
        alertManager.attach(this); // Ajouter this comme observateur pour le monitoring

        // Création du service de détection
        this.detectionService = new AnomalyDetectionService(
                modelService,
                esService,
                alertManager
        );
    }

    public void startMonitoring() {
        System.out.println("Démarrage du monitoring des anomalies...");

        // Démarrer le service (qui lance automatiquement la détection périodique)
        detectionService.start();

        // Ajouter un hook d'arrêt
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nArrêt du monitoring...");
            detectionService.stop();
        }));

        // Démarrer le monitoring périodique des statistiques
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // Afficher les statistiques courantes
                displayCurrentStats();

                // Attendre 1 minute avant la prochaine mise à jour
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void displayCurrentStats() {
        AnomalyDetectionService.AnomalyStats stats = detectionService.getAnomalyStats();
        System.out.println("\n=== Statistiques de monitoring (" +
                java.time.LocalDateTime.now() + ") ===");
        System.out.println("Logs traités: " + stats.getTotalLogsProcessed());
        System.out.println("Anomalies détectées: " + stats.getTotalAnomaliesDetected());
        System.out.println("Dernier traitement: " + stats.getLastProcessingTime());
    }

    @Override
    public void update(Alert alert) {
        // Afficher les détails de l'alerte détectée
        System.out.println("\n=== Nouvelle Alerte Détectée ===");
        System.out.println("Sévérité: " + alert.getSeverity());

        LogEntry log = alert.getAnomaly().getLogEntry();
        System.out.println("Score d'anomalie: " + alert.getAnomaly().getAnomalyScore());
        System.out.println("IP Source: " + log.getSource_ip());
        System.out.println("Port Source: " + log.getSource_port());
        System.out.println("IP Destination: " + log.getDest_ip());
        System.out.println("Port Destination: " + log.getDest_port());
        System.out.println("Bytes: " + log.getBytes());
        System.out.println("Direction: " + log.getDirection());
        System.out.println("Description: " + alert.getDescription());
    }

    public static void main(String[] args) {
        PeriodicAnomalyDetectionTest monitor = new PeriodicAnomalyDetectionTest();
        monitor.startMonitoring();
    }
}