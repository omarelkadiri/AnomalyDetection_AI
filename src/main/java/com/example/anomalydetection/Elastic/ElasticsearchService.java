package com.example.anomalydetection.Elastic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ElasticsearchService {
    private final ElasticsearchClientSingleton esClientSingleton;

    public ElasticsearchService() {
        this.esClientSingleton = ElasticsearchClientSingleton.getInstance();
    }

    public String searchLogsFromLastMinute() {
        // Définir l'index à interroger
        String endpoint = "/network-logs-syslog-*/_search";

        // Requête JSON pour obtenir les logs des dernières minutes
        String jsonQuery = """
                {
                    "_source": [
                         "source.ip",
                         "destination.ip",
                         "source.port",
                         "destination.port",
                         "network.bytes",
                         "direction"
                    ],
                    "query": {
                        "range": {
                            "@timestamp": {
                                "gte": "now-1m",
                                "lt": "now"
                            }
                        }
                    }
                }
                """;

        // Exécuter la requête Elasticsearch
        try {
            String response = esClientSingleton.executeSearchRequest(endpoint, jsonQuery);

            // Optionnel : analyser la réponse JSON
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonResponse = mapper.readTree(response);
            return jsonResponse.toPrettyString();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la recherche des logs Elasticsearch", e);
        }
    }

    public void close() {
        esClientSingleton.close();
    }
}
