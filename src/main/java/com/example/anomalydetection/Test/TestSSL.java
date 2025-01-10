package com.example.anomalydetection.Test;
/*
import javax.net.ssl.*;
import java.security.cert.X509Certificate;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStreamReader;
import java.io.BufferedReader;

public class TestSSL {

    // Méthode pour désactiver la vérification SSL
    public static void disableSSLVerification() throws Exception {
        // Créer un trust manager qui ne valide pas les chaînes de certificats
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
        };

        // Installer le trust manager qui fait confiance à tous les certificats
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // Désactiver la vérification de l'hôte
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
    }

    public static void main(String[] args) {
        try {
            // Désactiver la vérification SSL
            disableSSLVerification();

            // URL de l'API avec certificat SSL
            String url = "https://localhost:9200/";

            // Créer l'objet URL
            URL obj = new URL(url);

            // Ouvrir la connexion HTTPS
            HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

            // Ajouter le header avec la clé d'API (remplacez "votre-cle-api" par la clé réelle)
            String apiKey = "ZUdDc1VKUUJXUEhKR0N5eXF1Rng6c1NTOU4xN29SZXFWMHA4eDhRWnNjdw==";  // Remplacer par votre clé API
            con.setRequestProperty("Authorization", "ApiKey " + apiKey);

            // Spécifier la méthode de requête
            con.setRequestMethod("GET");

            // Lire la réponse de l'API
            int responseCode = con.getResponseCode();
            System.out.println("Code de Réponse : " + responseCode);

            // Lire la réponse
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Afficher la réponse
            System.out.println("Réponse de l'API : " + response.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}*/

