package com.example.anomalydetection.Alerting;

import com.example.anomalydetection.Elastic.ElasticsearchService;

import java.security.Provider;

public class ElasticAlertObserver implements AlertObserver {
    ElasticsearchService elasticsearchService = new ElasticsearchService();
    @Override
    public void update(Alert alert) {
        elasticsearchService.storeAlertInElastic(alert);
    }
}
