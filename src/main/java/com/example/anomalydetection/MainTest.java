package com.example.anomalydetection;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.cluster.HealthResponse;

public class MainTest {

    public static void main(String[] args) {
        ElasticsearchConnection connection = null;
        try {
            // Obtention de l'instance unique de la connexion Elasticsearch
            connection = ElasticsearchConnection.getInstance();

            // Récupération du client Elasticsearch
            ElasticsearchClient client = connection.getClient();

            // Test de la connexion
            HealthResponse health = client.cluster().health();

            // Affichage des informations du cluster
            System.out.println("Connexion réussie !");
            System.out.println("Status du cluster: " + health.status());
            System.out.println("Nombre de noeuds: " + health.numberOfNodes());
        } catch (Exception e) {
            System.out.println("Erreur lors de la connexion à Elasticsearch:");
            e.printStackTrace();
        } finally {
            // Fermeture de la connexion
            if (connection != null) {
                connection.closeConnection();
            }
        }
    } 
}
