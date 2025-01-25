package com.example.anomalydetection.Elastic;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.example.anomalydetection.Alerting.Alert;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.ssl.SSLContextBuilder;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.MapperFeature;

public class ElasticsearchClientSingleton {
    private static ElasticsearchClientSingleton instance;
    private final ElasticsearchClient client;
    private final RestClient restClient;

    private ElasticsearchClientSingleton() {
        String elastic_URL = "https://localhost:9200";
        int elastic_port = 9200;
        String elastic_host = "localhost";
        String elastic_apiKey = "ZUdDc1VKUUJXUEhKR0N5eXF1Rng6c1NTOU4xN29SZXFWMHA4eDhRWnNjdw==";

        try {
            // Création d'un SSLContext pour ignorer les certificats
            SSLContextBuilder sslContextBuilder = SSLContextBuilder.create()
                    .loadTrustMaterial((chain, authType) -> true); // Ignorer la validation des certificats

            // Création du client RestClient avec désactivation des certificats
            this.restClient = RestClient.builder(
                            new org.apache.http.HttpHost(elastic_host, elastic_port, "https"))
                    .setHttpClientConfigCallback(httpClientBuilder -> {
                        try {
                            // Désactivation du vérificateur de nom d'hôte
                            return httpClientBuilder
                                    .setSSLContext(sslContextBuilder.build())
                                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
                        } catch (Exception e) {
                            throw new RuntimeException("Erreur lors de la configuration du SSLContext", e);
                        }
                    })
                    .setDefaultHeaders(new org.apache.http.Header[]{
                            new org.apache.http.message.BasicHeader("Authorization", "ApiKey " + elastic_apiKey)
                    })
                    .build();

            // Création du transport pour Elasticsearch
            ElasticsearchTransport transport = new RestClientTransport(
                    restClient,
                    new JacksonJsonpMapper()
            );

            // Création du client Elasticsearch
            this.client = new ElasticsearchClient(transport);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la configuration du client Elasticsearch", e);
        }
    }

    public static synchronized ElasticsearchClientSingleton getInstance() {
        if (instance == null) {
            instance = new ElasticsearchClientSingleton();
        }
        return instance;
    }

    public ElasticsearchClient getClient() {
        return client;
    }

    public String executeSearchRequest(String endpoint, String jsonQuery) {
        try {
            Request request = new Request("GET", endpoint);
            request.setEntity(new org.apache.http.entity.StringEntity(jsonQuery, org.apache.http.entity.ContentType.APPLICATION_JSON));
            Response response = restClient.performRequest(request);

            // Lire et retourner la réponse Elasticsearch
            return new BufferedReader(new InputStreamReader(response.getEntity().getContent()))
                    .lines()
                    .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'exécution de la requête Elasticsearch", e);
        }
    }

    public void storeAlertInIndex(Alert alert) {
        try {
            ObjectMapper objectMapper = new ObjectMapper()
                    .registerModule(new JavaTimeModule())
                    //.configure(MapperFeature.PROPAGATE_TRANSIENT_HINT, false)
                    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

            // Generate index name with current date
            String indexName = String.format("alerts-ai-syslog-ipv4-%s",
                    new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()));

            // Convert alert to JSON
            String jsonAlert = objectMapper.writeValueAsString(alert);

            // Create index if it doesn't exist
            if ( !indexIsExist(indexName) ){
                Request createIndexRequest = new Request("PUT", "/" + indexName);
                restClient.performRequest(createIndexRequest);
            }


            // Index the alert document
            Request indexRequest = new Request("POST", "/" + indexName + "/_doc");
            indexRequest.setEntity(new org.apache.http.entity.StringEntity(jsonAlert,
                    org.apache.http.entity.ContentType.APPLICATION_JSON));

            Response response = restClient.performRequest(indexRequest);
        } catch (Exception e) {
           // throw new RuntimeException("Erreur lors du stockage de l'alerte dans Elasticsearch", e);
            e.printStackTrace();
        }
    }

    public boolean indexIsExist(String indexName) {
        try {
            Request request = new Request("HEAD", "/" + indexName);
            Response response = restClient.performRequest(request);

            // Check response status
            return (response.getStatusLine().getStatusCode() == 200);
        } catch (Exception e) {
            System.err.println("Error checking index: " + e.getMessage());
            return false;
        }
    }

    public void close() {
        try {
            restClient.close();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la fermeture du client Elasticsearch", e);
        }
    }
}
