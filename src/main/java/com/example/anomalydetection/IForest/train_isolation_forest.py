from elasticsearch import Elasticsearch
import pandas as pd
import numpy as np
from sklearn.ensemble import IsolationForest
from sklearn.preprocessing import StandardScaler
import joblib
import json
import urllib3
import warnings
from typing import Dict, Any

# Désactiver les avertissements SSL
urllib3.disable_warnings()
warnings.filterwarnings('ignore')

# Configuration Elasticsearch
ELASTIC_URL = "https://localhost:9200"
ELASTIC_API_KEY = "ZUdDc1VKUUJXUEhKR0N5eXF1Rng6c1NTOU4xN29SZXFWMHA4eDhRWnNjdw=="

# Création du client Elasticsearch
es = Elasticsearch(
    ELASTIC_URL,
    api_key=ELASTIC_API_KEY,
    verify_certs=False,
    ssl_show_warn=False
)

def convert_to_serializable(obj: Any) -> Any:
    """Convertit les types numpy en types Python standards."""
    if isinstance(obj, np.integer):
        return int(obj)
    elif isinstance(obj, np.floating):
        return float(obj)
    elif isinstance(obj, np.ndarray):
        return obj.tolist()
    elif isinstance(obj, dict):
        return {key: convert_to_serializable(value) for key, value in obj.items()}
    return obj

def extract_nested_fields(doc: Dict) -> Dict:
    """Extrait les champs imbriqués du document."""
    return {
        'source_ip': doc.get('source', {}).get('ip'),
        'source_port': doc.get('source', {}).get('port'),
        'dest_ip': doc.get('destination', {}).get('ip'),
        'dest_port': doc.get('destination', {}).get('port'),
        'bytes': doc.get('network', {}).get('bytes'),
        'direction': doc.get('direction')
    }

def fetch_data_from_es():
    try:
        if not es.ping():
            raise ValueError("Connexion à Elasticsearch échouée")
            
        query = {
            "_source": [
                "source.ip",
                "destination.ip",
                "source.port",
                "destination.port",
                "network.bytes",
                "direction"
            ],
            "query": {
                "bool": {
                    "must": [
                        {
                            "range": {
                                "@timestamp": {
                                    "gte": "2024-11-15T00:00:00",
                                    "lte": "2024-11-30T23:59:59",
                                    "format": "yyyy-MM-dd'T'HH:mm:ss"
                                }
                            }
                        }
                    ]
                }
            },
            "size": 10000
        }

        print("Exécution de la requête Elasticsearch...")
        response = es.search(
            index="network-logs-syslog-*",
            body=query,
            scroll='5m'
        )
        
        scroll_id = response['_scroll_id']
        hits = response['hits']['hits']
        results = []

        while len(hits) > 0:
            for hit in hits:
                results.append(extract_nested_fields(hit['_source']))
            response = es.scroll(scroll_id=scroll_id, scroll='5m')
            hits = response['hits']['hits']

        print(f"Nombre total de documents récupérés : {len(results)}")
        return results

    except Exception as e:
        print(f"Erreur lors de la récupération des données : {str(e)}")
        raise

def ip_to_int(ip: str) -> int:
    """Convertit une adresse IP en entier."""
    if not ip:
        return 0
    try:
        return int(''.join([f"{int(n):03d}" for n in ip.split('.')]))
    except:
        return 0

def preprocess_data(data):
    print("Début du prétraitement des données...")
    df = pd.DataFrame(data)
    
    # Conversion des adresses IP
    df['source_ip_num'] = df['source_ip'].apply(ip_to_int)
    df['dest_ip_num'] = df['dest_ip'].apply(ip_to_int)
    
    # Conversion des ports et bytes en numériques
    df['source_port'] = pd.to_numeric(df['source_port'], errors='coerce').fillna(0)
    df['dest_port'] = pd.to_numeric(df['dest_port'], errors='coerce').fillna(0)
    df['bytes'] = pd.to_numeric(df['bytes'], errors='coerce').fillna(0)
    
    # Encodage de la direction
    direction_factorized = pd.factorize(df['direction'])
    df['direction_num'] = direction_factorized[0]
    
    # Création des mappings avec conversion en types standards Python
    direction_mapping = dict(zip(direction_factorized[1], range(len(direction_factorized[1]))))
    
    features = [
        'source_ip_num',
        'dest_ip_num',
        'source_port',
        'dest_port',
        'bytes',
        'direction_num'
    ]
    
    X = df[features].copy()
    
    # Convertir le mapping en types standards Python
    mappings = {
        'direction_mapping': convert_to_serializable(direction_mapping),
        'feature_names': features
    }
    
    print("Prétraitement terminé.")
    return X, mappings

def train_isolation_forest():
    try:
        print("Récupération des données depuis Elasticsearch...")
        data = fetch_data_from_es()
        
        print("Prétraitement des données...")
        X, mappings = preprocess_data(data)
        
        print("\nDimensions des données d'entraînement:", X.shape)
        
        # Normalisation
        scaler = StandardScaler()
        X_scaled = scaler.fit_transform(X)
        
        print("\nEntraînement du modèle IsolationForest...")
        model = IsolationForest(
            n_estimators=100,
            contamination=0.1,
            random_state=42
        )
        
        model.fit(X_scaled)
        
        # Sauvegarde
        print("Sauvegarde du modèle et des configurations...")
        joblib.dump(model, 'isolation_forest_model.joblib')
        joblib.dump(scaler, 'scaler.joblib')
        
        # Sauvegarder les mappings après conversion en types standards Python
        with open('feature_mappings.json', 'w') as f:
            json.dump(convert_to_serializable(mappings), f, indent=2)
        
        # Test des prédictions
        predictions = model.predict(X_scaled)
        anomalies = (predictions == -1).sum()
        print(f"\nNombre d'anomalies détectées: {anomalies} ({(anomalies/len(predictions)*100):.2f}%)")
        
        print("\nModèle et configurations sauvegardés avec succès!")
        
        # Sauvegarder quelques statistiques importantes
        stats = {
            'total_samples': len(X),
            'anomalies_detected': int(anomalies),
            'anomaly_percentage': float((anomalies/len(predictions)*100)),
            'feature_means': dict(zip(X.columns, X.mean().tolist())),
            'feature_stds': dict(zip(X.columns, X.std().tolist()))
        }
        
        with open('model_stats.json', 'w') as f:
            json.dump(convert_to_serializable(stats), f, indent=2)
        
    except Exception as e:
        print(f"Erreur lors de l'entraînement : {str(e)}")
        raise

if __name__ == "__main__":
    train_isolation_forest()