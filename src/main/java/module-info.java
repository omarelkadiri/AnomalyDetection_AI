module com.example.anomalydetection {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.httpcomponents.httpcore;
    requires org.apache.httpcomponents.httpasyncclient;
    requires elasticsearch.java;
    requires jakarta.json;
    requires elasticsearch.rest.client;
    requires com.fasterxml.jackson.databind;
    requires org.apache.httpcomponents.httpclient;
    requires weka.stable;
    requires java.mail;

    opens com.example.anomalydetection to javafx.fxml, com.fasterxml.jackson.databind;
    exports com.example.anomalydetection;
    exports com.example.anomalydetection.Test;
    opens com.example.anomalydetection.Test to javafx.fxml;
    exports com.example.anomalydetection.Elastic;
    opens com.example.anomalydetection.Elastic to javafx.fxml;
    exports com.example.anomalydetection.Structure to com.fasterxml.jackson.databind;
    exports com.example.anomalydetection.IForest;
    opens com.example.anomalydetection.IForest to com.fasterxml.jackson.databind, javafx.fxml;

}