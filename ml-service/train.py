"""
Flight Price Prediction - Model Training Script
Uses Kaggle dataset to train a RandomForest model
"""

import os
import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestRegressor
from sklearn.preprocessing import LabelEncoder
from sklearn.metrics import mean_absolute_error, mean_squared_error, r2_score
import joblib
import kagglehub

# Create directories
os.makedirs('models', exist_ok=True)
os.makedirs('data', exist_ok=True)

print("=" * 50)
print("Flight Price Prediction - Model Training")
print("=" * 50)

# Download dataset from Kaggle
print("\n[1/6] Downloading dataset from Kaggle...")

csv_file = None

# First check if local file exists
local_files = ['data/Clean_Dataset.csv', 'data/flight_price.csv', 'data/clean_dataset.csv']
for local_file in local_files:
    if os.path.exists(local_file):
        csv_file = local_file
        print(f"Found local file: {csv_file}")
        break

if csv_file is None:
    try:
        # Try kagglehub
        import kagglehub
        try:
            path = kagglehub.dataset_download("shubhambathwal/flight-price-prediction")
        except AttributeError:
            # Try alternative method
            from kagglehub import datasets
            path = datasets.download("shubhambathwal/flight-price-prediction")
        
        print(f"Dataset downloaded to: {path}")
        
        # Find the CSV file
        for file in os.listdir(path):
            if file.endswith('.csv') and 'Clean' in file:
                csv_file = os.path.join(path, file)
                break
        
        if csv_file is None:
            for file in os.listdir(path):
                if file.endswith('.csv'):
                    csv_file = os.path.join(path, file)
                    break
        
    except Exception as e:
        print(f"Error with kagglehub: {e}")
        print("\nPlease download the dataset manually:")
        print("1. Go to: https://www.kaggle.com/datasets/shubhambathwal/flight-price-prediction")
        print("2. Download and extract the CSV file")
        print("3. Place it in: ml-service/data/Clean_Dataset.csv")
        print("4. Run this script again")
        exit(1)

if csv_file is None:
    print("No CSV file found!")
    print("Please place Clean_Dataset.csv in the data/ folder")
    exit(1)

print(f"Using file: {csv_file}")

# Load dataset
print("\n[2/6] Loading dataset...")
df = pd.read_csv(csv_file)
print(f"Dataset shape: {df.shape}")
print(f"Columns: {list(df.columns)}")

# Display sample
print("\nSample data:")
print(df.head())

# Data preprocessing
print("\n[3/6] Preprocessing data...")

# Check for the target column (price)
price_col = None
for col in df.columns:
    if 'price' in col.lower():
        price_col = col
        break

if price_col is None:
    print("Available columns:", df.columns.tolist())
    raise ValueError("Cannot find price column")

print(f"Target column: {price_col}")

# Drop unnecessary columns
columns_to_drop = ['Unnamed: 0', 'flight'] if 'Unnamed: 0' in df.columns else []
columns_to_drop = [c for c in columns_to_drop if c in df.columns]
if columns_to_drop:
    df = df.drop(columns=columns_to_drop)

# Handle missing values
df = df.dropna()

# Identify categorical and numerical columns
categorical_cols = df.select_dtypes(include=['object']).columns.tolist()
if price_col in categorical_cols:
    categorical_cols.remove(price_col)

numerical_cols = df.select_dtypes(include=['int64', 'float64']).columns.tolist()
if price_col in numerical_cols:
    numerical_cols.remove(price_col)

print(f"Categorical columns: {categorical_cols}")
print(f"Numerical columns: {numerical_cols}")

# Encode categorical variables
label_encoders = {}
for col in categorical_cols:
    le = LabelEncoder()
    df[col] = le.fit_transform(df[col].astype(str))
    label_encoders[col] = le
    print(f"  Encoded {col}: {len(le.classes_)} unique values")

# Prepare features and target
X = df.drop(columns=[price_col])
y = df[price_col]

print(f"\nFeatures shape: {X.shape}")
print(f"Target shape: {y.shape}")
print(f"Price range: {y.min():.2f} - {y.max():.2f}")

# Split data
print("\n[4/6] Splitting data...")
X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.2, random_state=42
)
print(f"Training set: {X_train.shape[0]} samples")
print(f"Test set: {X_test.shape[0]} samples")

# Train model
print("\n[5/6] Training RandomForest model...")
model = RandomForestRegressor(
    n_estimators=100,
    max_depth=20,
    min_samples_split=5,
    min_samples_leaf=2,
    random_state=42,
    n_jobs=-1,
    verbose=1
)

model.fit(X_train, y_train)
print("Training complete!")

# Evaluate model
print("\n[6/6] Evaluating model...")
y_pred = model.predict(X_test)

mae = mean_absolute_error(y_test, y_pred)
rmse = np.sqrt(mean_squared_error(y_test, y_pred))
r2 = r2_score(y_test, y_pred)

print(f"\nModel Performance:")
print(f"  MAE (Mean Absolute Error): {mae:.2f}")
print(f"  RMSE (Root Mean Squared Error): {rmse:.2f}")
print(f"  R² Score: {r2:.4f}")

# Feature importance
print("\nTop 10 Feature Importance:")
feature_importance = pd.DataFrame({
    'feature': X.columns,
    'importance': model.feature_importances_
}).sort_values('importance', ascending=False)

for idx, row in feature_importance.head(10).iterrows():
    print(f"  {row['feature']}: {row['importance']:.4f}")

# Save model and encoders
print("\n" + "=" * 50)
print("Saving model and encoders...")

model_data = {
    'model': model,
    'label_encoders': label_encoders,
    'feature_columns': list(X.columns),
    'categorical_columns': categorical_cols,
    'numerical_columns': numerical_cols,
    'metrics': {
        'mae': mae,
        'rmse': rmse,
        'r2': r2
    }
}

joblib.dump(model_data, 'models/price_model.pkl')
print("Model saved to: models/price_model.pkl")

print("\n" + "=" * 50)
print("Training completed successfully!")
print("=" * 50)
