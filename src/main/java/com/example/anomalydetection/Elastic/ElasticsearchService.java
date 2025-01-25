package com.example.anomalydetection.Elastic;

import com.example.anomalydetection.Alerting.Alert;
import com.example.anomalydetection.Structure.LogEntry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

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
                                "gte": "now-2m",
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

    public List<LogEntry> convertElasticResponseToLogEntries(String elasticResponse) {
        List<LogEntry> logEntries = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
            try {
                JsonNode root = objectMapper.readTree(elasticResponse);
                JsonNode hits = root.path("hits").path("hits");

                for (JsonNode hit : hits) {
                    JsonNode source = hit.path("_source");

                    LogEntry logEntry = new LogEntry();
                    logEntry.setSource_ip(source.path("source").path("ip").asText());
                    logEntry.setSource_port(source.path("source").path("port").asInt());
                    logEntry.setDest_ip(source.path("destination").path("ip").asText());
                    logEntry.setDest_port(source.path("destination").path("port").asInt());
                    logEntry.setBytes(source.path("network").path("bytes").asInt());
                    logEntry.setDirection(source.path("direction").asText());

                    logEntries.add(logEntry);
                }
            } catch (Exception e) {
                System.err.println("Erreur lors de la conversion des logs: " + e.getMessage());
            }
            return logEntries;
        }

    public void close() {
        esClientSingleton.close();
    }
    public void storeAlertInElastic(Alert alert){
        esClientSingleton.storeAlertInIndex(alert);
    }
}
