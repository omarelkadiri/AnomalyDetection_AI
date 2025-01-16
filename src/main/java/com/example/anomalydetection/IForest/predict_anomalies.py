import joblib
import pandas as pd
import numpy as np
import json
import sys
import os
import warnings

# Ignorer les avertissements de version
warnings.filterwarnings('ignore', category=UserWarning)

# Obtenir le chemin absolu du répertoire du script
ScriptPath = os.path.dirname(os.path.abspath(__file__)) + '/'

def load_models_and_mappings():
    """Charge le modèle, le scaler et les mappings"""
    try:
        model = joblib.load(os.path.join(ScriptPath, 'isolation_forest_model.joblib'))
        scaler = joblib.load(os.path.join(ScriptPath, 'scaler.joblib'))

        with open(os.path.join(ScriptPath, 'feature_mappings.json'), 'r') as f:
            mappings = json.load(f)

        return model, scaler, mappings
    except Exception as e:
        print(f"Erreur lors du chargement des modèles et mappings: {str(e)}", file=sys.stderr)
        sys.exit(1)

def preprocess_log(log_dict, feature_names):
    """Prétraite un log pour la prédiction"""
    try:
        # Convertir les IPs en valeurs numériques
        source_ip_num = int(''.join([f"{int(x):03d}" for x in log_dict['source_ip'].split('.')]))
        dest_ip_num = int(''.join([f"{int(x):03d}" for x in log_dict['dest_ip'].split('.')]))

        # Créer un DataFrame avec les bonnes colonnes
        data = {
            'source_ip_num': source_ip_num,
            'dest_ip_num': dest_ip_num,
            'source_port': log_dict['source_port'],
            'dest_port': log_dict['dest_port'],
            'bytes': log_dict['bytes'],
            'direction_num': 1 if log_dict['direction'] == 'in' else 0
        }

        return pd.DataFrame([data], columns=feature_names)
    except Exception as e:
        print(f"Erreur lors du prétraitement: {str(e)}", file=sys.stderr)
        sys.exit(1)

def predict_anomalies(logs_json):
    """Prédit les anomalies pour une liste de logs"""
    try:
        # Charger le modèle, le scaler et les mappings
        model, scaler, mappings = load_models_and_mappings()
        feature_names = mappings['feature_names']

        # Convertir le JSON en liste de dictionnaires
        logs = json.loads(logs_json)

        # Prétraiter les logs et créer un DataFrame
        all_features = pd.concat([preprocess_log(log, feature_names) for log in logs])

        # Normaliser les features
        features_scaled = scaler.transform(all_features)

        # Prédire les anomalies
        scores = model.score_samples(features_scaled)
        predictions = model.predict(features_scaled)

        # Préparer les résultats
        results = [
            {
                'index': i,
                'is_anomaly': bool(pred == -1),
                'anomaly_score': float(abs(score)),
                'original_log': log
            }
            for i, (log, pred, score) in enumerate(zip(logs, predictions, scores))
        ]

        return json.dumps(results)
    except Exception as e:
        print(f"Erreur lors de la prédiction: {str(e)}", file=sys.stderr)
        sys.exit(1)

if __name__ == "__main__":
    # Lire les logs depuis l'entrée standard
    input_json = sys.stdin.read()
    try:
        results = predict_anomalies(input_json)
        print(results)
    except Exception as e:
        print(f"Erreur lors de la prédiction: {str(e)}", file=sys.stderr)
        sys.exit(1)