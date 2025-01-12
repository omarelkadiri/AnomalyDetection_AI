package com.example.anomalydetection.Elastic;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ElasticsearchService2 {
    private final ElasticsearchClientSingleton esClient;
    private static final ObjectMapper mapper = new ObjectMapper();

    public ElasticsearchService2() {
        this.esClient = ElasticsearchClientSingleton.getInstance();
    }

    public String searchLogs(String indexName, long startTime, long endTime) {
        String query = buildTimeRangeQuery(indexName, startTime, endTime);
        return esClient.executeSearchRequest("/logs/_search", query);
    }

    public String searchMetrics(String indexName, String metricName, long startTime, long endTime) {
        String query = buildMetricQuery(indexName, metricName, startTime, endTime);
        return esClient.executeSearchRequest("/metrics/_search", query);
    }

    private String buildTimeRangeQuery(String indexName, long startTime, long endTime) {
        return String.format("""
            {
              "query": {
                "bool": {
                  "must": [
                    {
                      "range": {
                        "timestamp": {
                          "gte": %d,
                          "lte": %d
                        }
                      }
                    }
                  ]
                }
              }
            }""", startTime, endTime);
    }

    private String buildMetricQuery(String indexName, String metricName, long startTime, long endTime) {
        return String.format("""
            {
              "query": {
                "bool": {
                  "must": [
                    {
                      "term": {
                        "metric_name": "%s"
                      }
                    },
                    {
                      "range": {
                        "timestamp": {
                          "gte": %d,
                          "lte": %d
                        }
                      }
                    }
                  ]
                }
              }
            }""", metricName, startTime, endTime);
    }

    public void close() {
        esClient.close();
    }
}