package com.example.anomalydetection.Test;

import com.example.anomalydetection.Alerting.Alert;
import com.example.anomalydetection.Alerting.AlertManager;
import com.example.anomalydetection.Alerting.AlertObserver;
import com.example.anomalydetection.Alerting.EmailAlertObserver;
import com.example.anomalydetection.Elastic.ElasticsearchService;
import com.example.anomalydetection.IForest.PretrainedModelService;
import com.example.anomalydetection.Service.AnomalyDetectionService;
import com.example.anomalydetection.Structure.AnomalyResult;
import com.example.anomalydetection.Structure.LogEntry;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class AnomalyDetectionServiceTest {
    private static List<Alert> capturedAlerts = new ArrayList<>();
    private static CountDownLatch alertLatch;

    public static void main(String[] args) {
        System.out.println("Démarrage des tests du service de détection d'anomalies...");

        // Test complet du service
        testFullServiceIntegration();
    }

    private static void testFullServiceIntegration() {
        try {
            // Initialisation des services
            PretrainedModelService modelService = new PretrainedModelService();
            ElasticsearchService esService = new ElasticsearchService();
            AlertManager alertManager = new AlertManager();

            // Configuration de l'observateur d'email
            EmailAlertObserver emailObserver = new EmailAlertObserver();

            // Ajout d'un observateur de test pour capturer les alertes
            TestAlertObserver testObserver = new TestAlertObserver();
            alertManager.attach(emailObserver);
            alertManager.attach(testObserver);

            // Création du service de détection
            AnomalyDetectionService detectionService = new AnomalyDetectionService(
                    modelService,
                    esService,
                    alertManager
            );

            // Configuration du latch pour les tests
            alertLatch = new CountDownLatch(1);
            capturedAlerts.clear();

            System.out.println("\n1. Démarrage du service...");
            detectionService.start();

            System.out.println("2. Attente du traitement des premiers logs...");
            // Attendre que le premier cycle de détection soit terminé
            Thread.sleep(70000); // 70 secondes pour être sûr d'avoir un cycle complet

            // Vérification des statistiques
            AnomalyDetectionService.AnomalyStats stats = detectionService.getAnomalyStats();
            System.out.println("\n3. Statistiques après le premier cycle:");
            System.out.println(stats);

            // Vérification des alertes
            System.out.println("\n4. Alertes capturées:");
            if (!capturedAlerts.isEmpty()) {
                for (Alert alert : capturedAlerts) {
                    System.out.println("- Alerte: " + alert.getDetails());
                }
            } else {
                System.out.println("Aucune alerte générée pendant le test");
            }

            // Arrêt du service
            System.out.println("\n5. Arrêt du service...");
            detectionService.stop();

            // Vérification finale
            verifyTestResults(stats);

        } catch (Exception e) {
            System.err.println("Erreur pendant les tests: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void verifyTestResults(AnomalyDetectionService.AnomalyStats stats) {
        System.out.println("\n=== Résultats des tests ===");

        // Vérification du traitement des logs
        boolean logsProcessed = stats.getTotalLogsProcessed() > 0;
        System.out.println("Traitement des logs: " +
                (logsProcessed ? "SUCCÈS" : "ÉCHEC") +
                " (" + stats.getTotalLogsProcessed() + " logs traités)");

        // Vérification du timestamp de dernier traitement
        boolean recentProcessing = stats.getLastProcessingTime() != null &&
                stats.getLastProcessingTime().isAfter(LocalDateTime.now().minusMinutes(2));
        System.out.println("Dernier traitement récent: " +
                (recentProcessing ? "SUCCÈS" : "ÉCHEC") +
                " (" + stats.getLastProcessingTime() + ")");

        // Vérification des alertes
        System.out.println("Génération d'alertes: " +
                (!capturedAlerts.isEmpty() ? "SUCCÈS" : "INFO - Aucune anomalie détectée") +
                " (" + capturedAlerts.size() + " alertes)");
    }

    // Observateur de test pour capturer les alertes
    private static class TestAlertObserver implements AlertObserver {
        @Override
        public void update(Alert alert) {
            capturedAlerts.add(alert);
            alertLatch.countDown();
        }
    }
}