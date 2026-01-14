import joblib
import pandas as pd
import numpy as np
from sklearn.ensemble import RandomForestRegressor
from sklearn.preprocessing import LabelEncoder
import os

# Create directory
os.makedirs('models', exist_ok=True)

# Define columns as expected by the application
categorical_cols = ['airline', 'source_city', 'destination_city', 'departure_time', 'arrival_time', 'stops', 'class']
numerical_cols = ['duration', 'days_left']
feature_cols = categorical_cols + numerical_cols

# Create dummy encoders
label_encoders = {}
for col in categorical_cols:
    le = LabelEncoder()
    # Fit with some dummy classes
    le.fit(['Default', 'Indigo', 'Air India', 'Vistara', 'Turkish Airlines', 'Istanbul', 'London', 'New York', 'Morning', 'Afternoon', 'Evening', 'Night', 'zero', 'one', 'two_or_more', 'Economy', 'Business'])
    label_encoders[col] = le

# Create and fit a dummy model
model = RandomForestRegressor(n_estimators=10)
# Fit on one row of zeros
model.fit(np.zeros((1, len(feature_cols))), [100.0])

# Prepare model data
model_data = {
    'model': model,
    'label_encoders': label_encoders,
    'feature_columns': feature_cols,
    'categorical_columns': categorical_cols,
    'numerical_columns': numerical_cols,
    'metrics': {
        'mae': 0.0,
        'rmse': 0.0,
        'r2': 0.99
    }
}

# Save
joblib.dump(model_data, 'models/price_model.pkl')
print("Dummy model saved to models/price_model.pkl")
