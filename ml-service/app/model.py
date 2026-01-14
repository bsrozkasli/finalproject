"""
ML Model loading and prediction
"""

import os
import joblib
import pandas as pd
import numpy as np
from typing import Dict, Any, Optional
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


class PricePredictor:
    """Flight Price Prediction Model"""
    
    def __init__(self, model_path: str = "models/price_model.pkl"):
        self.model_path = model_path
        self.model = None
        self.label_encoders = None
        self.feature_columns = None
        self.categorical_columns = None
        self.numerical_columns = None
        self.metrics = None
        self.is_loaded = False
        
    def load_model(self) -> bool:
        """Load the trained model and encoders"""
        try:
            if not os.path.exists(self.model_path):
                logger.warning(f"Model file not found: {self.model_path}")
                return False
            
            logger.info(f"Loading model from {self.model_path}")
            model_data = joblib.load(self.model_path)
            
            self.model = model_data['model']
            self.label_encoders = model_data['label_encoders']
            self.feature_columns = model_data['feature_columns']
            self.categorical_columns = model_data['categorical_columns']
            self.numerical_columns = model_data['numerical_columns']
            self.metrics = model_data.get('metrics', {})
            
            self.is_loaded = True
            logger.info("Model loaded successfully!")
            logger.info(f"Feature columns: {self.feature_columns}")
            logger.info(f"Model metrics: {self.metrics}")
            
            return True
            
        except Exception as e:
            logger.error(f"Error loading model: {e}")
            return False
    
    def predict(self, features: Dict[str, Any]) -> Optional[float]:
        """
        Predict flight price
        
        Args:
            features: Dictionary with flight features
            
        Returns:
            Predicted price or None if prediction fails
        """
        if not self.is_loaded:
            logger.error("Model not loaded")
            return None
            
        try:
            # Create dataframe with single row
            df = pd.DataFrame([features])
            
            # Map input field names to model feature names
            column_mapping = {
                'flight_class': 'class',
                'source_city': 'source_city',
                'destination_city': 'destination_city',
            }
            
            for old_name, new_name in column_mapping.items():
                if old_name in df.columns and new_name not in df.columns:
                    df[new_name] = df[old_name]
                    if old_name != new_name:
                        df = df.drop(columns=[old_name])
            
            # Encode categorical variables
            for col in self.categorical_columns:
                if col in df.columns and col in self.label_encoders:
                    le = self.label_encoders[col]
                    # Handle unseen categories
                    value = str(df[col].iloc[0])
                    if value in le.classes_:
                        df[col] = le.transform([value])[0]
                    else:
                        # Use most common class as fallback
                        logger.warning(f"Unknown category '{value}' for {col}, using default")
                        df[col] = 0
            
            # Ensure all feature columns exist
            for col in self.feature_columns:
                if col not in df.columns:
                    logger.warning(f"Missing column {col}, using 0")
                    df[col] = 0
            
            # Select only required columns in correct order
            X = df[self.feature_columns]
            
            # Make prediction
            prediction = self.model.predict(X)[0]
            
            # Ensure non-negative price
            prediction = max(0, prediction)
            
            return float(prediction)
            
        except Exception as e:
            logger.error(f"Prediction error: {e}")
            return None
    
    def get_confidence(self) -> float:
        """Get model confidence (R² score)"""
        if self.metrics and 'r2' in self.metrics:
            return self.metrics['r2']
        return 0.0
    
    def get_metrics(self) -> Dict[str, Any]:
        """Get model metrics"""
        return self.metrics or {}


# Global predictor instance
predictor = PricePredictor()
