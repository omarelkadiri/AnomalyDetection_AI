package com.example.anomalydetection.IForest;


import com.example.anomalydetection.Structure.AnomalyResult;
import com.example.anomalydetection.Structure.LogEntry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;

public class PretrainedModelService {
    private static final String PYTHON_SCRIPT_PATH = "src/main/java/com/example/anomalydetection/IForest/predict_anomalies.py";
    private final ObjectMapper objectMapper;

    public PretrainedModelService() {
        this.objectMapper = new ObjectMapper();
    }

    public List<AnomalyResult> batchPredict(List<LogEntry> logs) throws IOException {
        // Convertir les logs en JSON
        List<LogEntry> logsIpv4 = IPv6Filter.filterOutIPv6Logs(logs);
        String logsJson = objectMapper.writeValueAsString(logsIpv4);

        // Préparer la commande Python
        ProcessBuilder pb = new ProcessBuilder("python3", PYTHON_SCRIPT_PATH);
        Process process = pb.start();

        // Envoyer les données au script Python
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(process.getOutputStream()))) {
            writer.write(logsJson);
            writer.flush();
        }

        // Lire la sortie du script Python
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }
        }

        // Vérifier les erreurs
        try (BufferedReader errorReader = new BufferedReader(
                new InputStreamReader(process.getErrorStream()))) {
            String line;
            StringBuilder errorOutput = new StringBuilder();
            while ((line = errorReader.readLine()) != null) {
                errorOutput.append(line).append("\n");
            }
            if (errorOutput.length() > 0) {
                throw new RuntimeException("Erreur Python: " + errorOutput.toString());
            }
        }

        // Attendre la fin du processus
        try {
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Le script Python s'est terminé avec le code: " + exitCode);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interruption lors de l'exécution du script Python", e);
        }

        // Convertir les résultats en objets AnomalyResult
        List<PythonPredictionResult> predictionResults = objectMapper.readValue(
                output.toString(),
                new TypeReference<List<PythonPredictionResult>>() {}
        );

        // Transformer les résultats en AnomalyResult
        List<AnomalyResult> anomalyResults = new ArrayList<>();
        for (PythonPredictionResult result : predictionResults) {
            anomalyResults.add(new AnomalyResult(
                    logs.get(result.getIndex()),
                    result.isAnomaly(),
                    result.getAnomaly_score(),
                    LocalDateTime.now()
            ));
        }

       // System.out.println(anomalyResults); //  s'affiche correctement
        return anomalyResults;
    }

    public AnomalyResult predictAnomaly(LogEntry log) throws IOException {
        List<LogEntry> singleLog = List.of(log);
        List<AnomalyResult> results = batchPredict(IPv6Filter.filterOutIPv6Logs(singleLog));
        return results.get(0);
    }

    // Classe interne pour la désérialisation des résultats Python
    private static class PythonPredictionResult {
        private int index;
        private boolean is_anomaly;
        private double anomaly_score;
        private LogEntry original_log;

        // Getters et setters
        public int getIndex() { return index; }
        public void setIndex(int index) { this.index = index; }

        public boolean isAnomaly() {
            return is_anomaly;
        }

        public void setIs_anomaly(boolean is_anomaly) {
            this.is_anomaly = is_anomaly;
        }

        public double getAnomaly_score() {
            return anomaly_score;
        }

        public void setAnomaly_score(double anomaly_score) {
            this.anomaly_score = anomaly_score;
        }

        public LogEntry getOriginal_log() {
            return original_log;
        }

        public void setOriginal_log(LogEntry original_log) {
            this.original_log = original_log;
        }
    }
}
