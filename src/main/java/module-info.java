module com.example.anomalydetection {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.httpcomponents.httpcore;
    requires org.apache.httpcomponents.httpasyncclient;
    requires elasticsearch.java;
    requires jakarta.json;
    //requires elasticsearch.rest.high.level.client;
    //requires  org. apache. httpcomponents. httpclient;
    //requires elasticsearch;
    //requires org.elasticsearch.client;
    requires elasticsearch.rest.client;
    requires com.fasterxml.jackson.databind;
    requires org.apache.httpcomponents.httpclient;


    opens com.example.anomalydetection to javafx.fxml;
    exports com.example.anomalydetection;
    exports com.example.anomalydetection.Test;
    opens com.example.anomalydetection.Test to javafx.fxml;
}