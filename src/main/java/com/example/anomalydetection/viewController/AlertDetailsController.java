package com.example.anomalydetection.viewController;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;

public class AlertDetailsController implements Initializable {

    @FXML
    private Text details;

    private String details1;

    public void setDetails(String details) {
        this.details1 = details;
        this.details.setText(details);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        details.setText(details1);
    }
}
