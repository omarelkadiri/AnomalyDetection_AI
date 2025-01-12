package com.example.anomalydetection.Test;

import com.example.anomalydetection.Elastic.ElasticsearchService;

public class TestElastic2 {
        public static void main(String[] args) {
            ElasticsearchService service = new ElasticsearchService();

            try {
                String logs = service.searchLogsFromLastMinute();
                System.out.println("Résultats des logs des dernières minutes :");
                System.out.println(logs);
            } finally {
                service.close();
            }
        }
    }

