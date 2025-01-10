package com.example.anomalydetection.Test;

import co.elastic.clients.util.ContentType;
import org.apache.http.HttpHost;
import org.apache.http.entity.StringEntity;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class MainTest {

    public static void main(String[] args) {
        String apiKey = System.getenv("elastic_API_KEY"); // API Key Elasticsearch
        String elasticUrl = System.getenv("elastic_URL");   // URL Elasticsearch

        if (apiKey == null || elasticUrl == null) {
            System.out.println("Les variables d'environnement elastic_API_KEY ou elastic_URL ne sont pas définies.");
            return;
        }

        try (RestClient restClient = RestClient.builder(
                        HttpHost.create(elasticUrl))
                .setDefaultHeaders(new org.apache.http.Header[]{
                        new org.apache.http.message.BasicHeader("Authorization", "ApiKey " + apiKey)
                })
                .build()) {

            // Corps de la requête JSON
            String jsonQuery = """
                    {
                      "size": 0,
                      "query": {
                        "bool": {
                          "must": [
                            {
                              "term": {
                                "auth.result.keyword": "could not authenticate"
                              }
                            },
                            {
                              "range": {
                                "@timestamp": {
                                  "gte": "now-1m",
                                  "lte": "now"
                                }
                              }
                            }
                          ]
                        }
                      },
                      "aggs": {
                        "failed_user_count": {
                          "terms": {
                            "field": "auth.user.keyword",
                            "size": 10
                          },
                          "aggs": {
                            "targeted_services": {
                              "terms": {
                                "field": "auth.service.keyword",
                                "size": 1
                              }
                            }
                          }
                        }
                      }
                    }
                    """;

            // Création de la requête HTTP
            Request request = new Request("GET", "/network-logs-syslog-auth-*/_search");
            request.setEntity(new StringEntity(jsonQuery, ContentType.APPLICATION_JSON));

            // Envoi de la requête et récupération de la réponse
            Response response = restClient.performRequest(request);

            // Lecture et affichage de la réponse
            String responseBody = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))
                    .lines()
                    .collect(Collectors.joining("\n"));

            System.out.println("Réponse Elasticsearch :");
            System.out.println(responseBody);

        } catch (Exception e) {
            System.out.println("Erreur lors de l'exécution de la requête Elasticsearch:");
            e.printStackTrace();
        }
    }
}
