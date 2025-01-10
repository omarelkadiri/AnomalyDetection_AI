package com.example.anomalydetection;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.cluster.HealthResponse;
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

public class TestElasticConnectionSimple {

    public static void main(String[] args) {
        try {
            // Configuration SSL pour ignorer la vérification (comme dans votre exemple)
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(
                    null,
                    new TrustManager[]{new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }},
                    new java.security.SecureRandom()
            );

            // Configuration de l'API key
            String apiKey = "ZUdDc1VKUUJXUEhKR0N5eXF1Rng6c1NTOU4xN29SZXFWMHA4eDhRWnNjdw==";

            // Création du client REST avec SSL désactivé
            RestClient restClient = RestClient.builder(
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

            // Création du transport avec Jackson mapper
            ElasticsearchTransport transport = new RestClientTransport(
                    restClient,
                    new JacksonJsonpMapper()
            );

            // Création du client Elasticsearch
            ElasticsearchClient client = new ElasticsearchClient(transport);

            // Test de la connexion en récupérant l'état du cluster
            HealthResponse health = client.cluster().health();

            // Affichage des informations du cluster
            System.out.println("Connexion réussie !");
            System.out.println("Status du cluster: " + health.status());
            System.out.println("Nombre de noeuds: " + health.numberOfNodes());
            System.out.println("Nombre d'indices: " + health.indices().size());

            // Fermeture propre des ressources
            transport.close();
            restClient.close();

        } catch (Exception e) {
            System.out.println("Erreur lors de la connexion à Elasticsearch:");
            e.printStackTrace();
        }
    }
}