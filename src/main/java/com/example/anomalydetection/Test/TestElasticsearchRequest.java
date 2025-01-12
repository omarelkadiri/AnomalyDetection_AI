package com.example.anomalydetection.Test;

import com.example.anomalydetection.Elastic.ElasticsearchClientSingleton;

public class TestElasticsearchRequest {
    public static void main(String[] args) {
        String query = """
                {
                        "size": 0,
                        "query": {
                            "range": {
                                "@timestamp": {
                                    "gte": "now-1m",
                                    "lte": "now"
                                }
                            }
                        },
                        "aggs": {
                            "targeted_ips": {
                                "terms": {
                                    "field": "destination.ip",
                                    "size": 10
                                },
                                "aggs": {
                                    "top_attackers": {
                                        "terms": {
                                            "field": "source.ip",
                                            "size": 10
                                        }
                                    }
                                }
                            }
                        }
                    }
                """;

        String endpoint = "/network-logs-syslog-auth-*/_search";

        try {
            ElasticsearchClientSingleton clientSingleton = ElasticsearchClientSingleton.getInstance();
            String response = clientSingleton.executeSearchRequest(endpoint, query);
            System.out.println("Réponse Elasticsearch :");
            System.out.println(response);

            // Fermer la connexion
            clientSingleton.close();
        } catch (Exception e) {
            System.out.println("Erreur lors de l'exécution de la requête Elasticsearch:");
            e.printStackTrace();
        }
    }
}
