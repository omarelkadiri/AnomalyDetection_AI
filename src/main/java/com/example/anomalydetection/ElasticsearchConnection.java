package com.example.anomalydetection;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

public class ElasticsearchConnection {

    // Instance unique de la classe
    private static ElasticsearchConnection instance;

    // Instances du client Elasticsearch et des ressources associées
    private ElasticsearchClient client;
    private RestClient restClient;
    private ElasticsearchTransport transport;

    // Constructeur privé pour empêcher l'instanciation directe
    private ElasticsearchConnection() {
        initializeClient();
    }

    // Méthode statique pour obtenir l'instance unique
    public static synchronized ElasticsearchConnection getInstance() {
        if (instance == null) {
            instance = new ElasticsearchConnection();
        }
        return instance;
    }

    // Méthode pour initialiser le client Elasticsearch
    private void initializeClient() {
        try {
            // Configuration SSL pour ignorer la vérification
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(
                    null,
                    new TrustManager[]{new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                    }},
                    new java.security.SecureRandom()
            );

            // API Key pour l'authentification
            String apiKey = "ZUdDc1VKUUJXUEhKR0N5eXF1Rng6c1NTOU4xN29SZXFWMHA4eDhRWnNjdw==";

            // Création du client REST
            restClient = RestClient.builder(
                            new HttpHost("localhost", 9200, "https"))
                    .setDefaultHeaders(new Header[]{
                            new BasicHeader("Authorization", "ApiKey " + apiKey)
                    })
                    .setHttpClientConfigCallback(httpClientBuilder -> {
                        httpClientBuilder.setSSLContext(sslContext);
                        httpClientBuilder.setSSLHostnameVerifier((hostname, session) -> true);
                        return httpClientBuilder;
                    })
                    .build();

            // Création du transport
            transport = new RestClientTransport(
                    restClient,
                    new JacksonJsonpMapper()
            );

            // Initialisation du client Elasticsearch
            client = new ElasticsearchClient(transport);

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'initialisation du client Elasticsearch", e);
        }
    }

    // Méthode pour obtenir le client Elasticsearch
    public ElasticsearchClient getClient() {
        return client;
    }

    // Méthode pour fermer les ressources proprement
    public void closeConnection() {
        try {
            if (transport != null) {
                transport.close();
            }
            if (restClient != null) {
                restClient.close();
            }
            System.out.println("Connexion Elasticsearch fermée avec succès.");
        } catch (Exception e) {
            System.err.println("Erreur lors de la fermeture de la connexion Elasticsearch:");
            e.printStackTrace();
        }
    }
}
