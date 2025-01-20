package com.example.anomalydetection.Test;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SlackAlertSender {

    private static final String SLACK_WEBHOOK_URL = "https://hooks.slack.com/services/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";

    public static void main(String[] args) {


        String message = "Test Siem ";
        sendSlackAlert(message);
    }

    public static void sendSlackAlert(String message) {
        try {
            // Préparation de la requête
            URL url = new URL(SLACK_WEBHOOK_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setDoOutput(true);

            // Structure du payload JSON
            String payload = String.format("{\"text\": \"%s\"}", message);

            // Envoi des données
            try (OutputStream outputStream = connection.getOutputStream()) {
                byte[] input = payload.getBytes("utf-8");
                outputStream.write(input, 0, input.length);
            }

            // Lecture de la réponse
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                System.out.println("Message envoyé avec succès à Slack.");
            } else {
                System.out.println("Erreur lors de l'envoi du message. Code de réponse : " + responseCode);
            }

        } catch (Exception e) {
            System.err.println("Une erreur est survenue : " + e.getMessage());
            e.printStackTrace();
        }
    }
}

