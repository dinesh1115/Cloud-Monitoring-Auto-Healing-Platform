#!/usr/bin/env python3
"""
AI-Powered Anomaly Detection Service
Uses machine learning for advanced anomaly detection in cloud monitoring metrics
"""

import sys
import json
import numpy as np
import pandas as pd
from sklearn.ensemble import IsolationForest
from sklearn.preprocessing import StandardScaler
import joblib
import os
from datetime import datetime
import logging

# Configure logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

class MLAomalyDetector:
    def __init__(self, model_path='models/anomaly_detector.pkl'):
        self.model_path = model_path
        self.model = None
        self.scaler = None
        self.is_trained = False
        self.load_or_create_model()

    def load_or_create_model(self):
        """Load existing model or create new one"""
        try:
            if os.path.exists(self.model_path):
                self.model = joblib.load(self.model_path)
                self.scaler = joblib.load(self.model_path.replace('.pkl', '_scaler.pkl'))
                self.is_trained = True
                logger.info("Loaded existing ML model")
            else:
                self.model = IsolationForest(
                    n_estimators=100,
                    contamination=0.1,
                    random_state=42
                )
                self.scaler = StandardScaler()
                logger.info("Created new ML model")
        except Exception as e:
            logger.error(f"Error loading model: {e}")
            self.model = IsolationForest(n_estimators=100, contamination=0.1, random_state=42)
            self.scaler = StandardScaler()

    def train(self, metrics_data):
        """Train the anomaly detection model"""
        try:
            if not metrics_data:
                logger.warning("No data provided for training")
                return False

            # Convert to DataFrame
            df = pd.DataFrame(metrics_data)

            # Prepare features
            features = ['cpu', 'temperature']
            X = df[features].values

            # Scale features
            X_scaled = self.scaler.fit_transform(X)

            # Train model
            self.model.fit(X_scaled)

            # Save model
            os.makedirs(os.path.dirname(self.model_path), exist_ok=True)
            joblib.dump(self.model, self.model_path)
            joblib.dump(self.scaler, self.model_path.replace('.pkl', '_scaler.pkl'))

            self.is_trained = True
            logger.info(f"Model trained on {len(metrics_data)} samples")
            return True

        except Exception as e:
            logger.error(f"Error training model: {e}")
            return False

    def detect_anomaly(self, metric):
        """Detect anomaly in a single metric"""
        try:
            if not self.is_trained:
                logger.warning("Model not trained, cannot detect anomalies")
                return {"is_anomaly": False, "score": 0.0, "confidence": 0.0}

            # Prepare feature vector
            features = np.array([[metric['cpu'], metric['temperature']]])

            # Scale features
            features_scaled = self.scaler.transform(features)

            # Predict anomaly
            prediction = self.model.predict(features_scaled)
            scores = self.model.decision_function(features_scaled)

            # Convert prediction to boolean (1 = normal, -1 = anomaly)
            is_anomaly = prediction[0] == -1
            score = float(scores[0])
            confidence = float(abs(scores[0]))  # Higher absolute score = more confident

            return {
                "is_anomaly": is_anomaly,
                "score": score,
                "confidence": confidence,
                "prediction": int(prediction[0])
            }

        except Exception as e:
            logger.error(f"Error detecting anomaly: {e}")
            return {"is_anomaly": False, "score": 0.0, "confidence": 0.0}

    def get_model_info(self):
        """Get information about the current model"""
        return {
            "is_trained": self.is_trained,
            "model_type": "IsolationForest",
            "contamination": 0.1,
            "n_estimators": 100,
            "features": ["cpu", "temperature"]
        }

def main():
    """Main function for command-line usage"""
    if len(sys.argv) < 2:
        print("Usage: python ml_anomaly_detector.py <command> [args...]")
        print("Commands:")
        print("  train <json_file>    - Train model with metrics data")
        print("  detect <json_data>   - Detect anomaly in metric data")
        print("  info                 - Get model information")
        return

    detector = MLAomalyDetector()
    command = sys.argv[1]

    if command == "train":
        if len(sys.argv) < 3:
            print("Error: Please provide path to JSON file with training data")
            return

        json_file = sys.argv[2]
        try:
            with open(json_file, 'r') as f:
                data = json.load(f)

            success = detector.train(data)
            print(f"Training {'successful' if success else 'failed'}")

        except Exception as e:
            print(f"Error training model: {e}")

    elif command == "detect":
        if len(sys.argv) < 3:
            print("Error: Please provide JSON data for detection")
            return

        json_data = sys.argv[2]
        try:
            metric = json.loads(json_data)
            result = detector.detect_anomaly(metric)
            print(json.dumps(result, indent=2))

        except Exception as e:
            print(f"Error detecting anomaly: {e}")

    elif command == "info":
        info = detector.get_model_info()
        print(json.dumps(info, indent=2))

    else:
        print(f"Unknown command: {command}")

if __name__ == "__main__":
    main()