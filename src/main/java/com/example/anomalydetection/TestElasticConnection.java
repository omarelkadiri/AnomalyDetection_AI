/*package com.example.anomalydetection;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.*;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

public class TestElasticConnection {

    public static void main(String[] args) {
        try {
            // Configuration SSL pour ignorer la vérification
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(
                    null,
                    new TrustManager[]{new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() { return null; }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                    }},
                    new java.security.SecureRandom()
            );

            // Création du client
            RestHighLevelClient client = new RestHighLevelClient(
                    RestClient.builder(
                                    new HttpHost("localhost", 9200, "https"))
                            .setHttpClientConfigCallback(httpClientBuilder -> {
                                httpClientBuilder.setSSLContext(sslContext);
                                httpClientBuilder.setSSLHostnameVerifier((hostname, session) -> true);
                                return httpClientBuilder;
                            })
            );

            // Test de la connexion en récupérant la liste des indices
            GetIndexResponse indices = client.indices().get(
                    new GetIndexRequest("*"),
                    RequestOptions.DEFAULT
            );

            System.out.println("Connexion réussie !");
            System.out.println("\nListe des indices:");
            for (String index : indices.getIndices()) {
                System.out.println("- " + index);
            }

            // Fermeture du client
            client.close();

        } catch (Exception e) {
            System.out.println("Erreur lors de la connexion à Elasticsearch:");
            e.printStackTrace();
        }
    }
}
*/