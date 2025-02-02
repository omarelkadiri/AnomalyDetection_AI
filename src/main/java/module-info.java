module com.example.anomalydetection {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.httpcomponents.httpcore;
    requires org.apache.httpcomponents.httpasyncclient;
    requires elasticsearch.java;
    requires jakarta.json;
    requires elasticsearch.rest.client;
    requires org.apache.httpcomponents.httpclient;
    //requires weka.stable;
    requires java.mail;
    requires java.desktop;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.databind;

    opens com.example.anomalydetection to javafx.fxml, com.fasterxml.jackson.databind;
    exports com.example.anomalydetection;
    exports com.example.anomalydetection.Test;
    opens com.example.anomalydetection.Test to javafx.fxml;
    exports com.example.anomalydetection.Elastic;
    opens com.example.anomalydetection.Elastic to javafx.fxml;
    exports com.example.anomalydetection.Structure to com.fasterxml.jackson.databind;
    exports com.example.anomalydetection.IForest;
    exports com.example.anomalydetection.viewController;
    opens com.example.anomalydetection.IForest to com.fasterxml.jackson.databind, javafx.fxml;
    opens com.example.anomalydetection.viewController to com.fasterxml.jackson.databind, javafx.fxml;
    exports com.example.anomalydetection.Alerting to com.fasterxml.jackson.databind;

}