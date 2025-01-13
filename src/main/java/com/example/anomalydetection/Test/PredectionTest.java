package com.example.anomalydetection.Test;

import com.example.anomalydetection.PretrainedModelService;
import com.example.anomalydetection.Structure.AnomalyResult;
import com.example.anomalydetection.Structure.LogEntry;

import java.io.IOException;

public class PredectionTest {
    public static void main(String[] args) {
        PretrainedModelService modelService = new PretrainedModelService();

        // Prédiction unique
        LogEntry log = new LogEntry(
                "10.0.2.15",
                "10.0.2.3",
                35377,
                53		,
                70,
                "out"
        );

        try {
            AnomalyResult result = modelService.predictAnomaly(log);
            System.out.println("Résultat de la prédiction : " + result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
