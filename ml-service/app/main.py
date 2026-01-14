"""
FastAPI application for Flight Price Prediction
"""

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from contextlib import asynccontextmanager
import logging

from app.schemas import (
    PredictionRequest, 
    PredictionResponse, 
    HealthResponse,
    BatchPredictionRequest,
    BatchPredictionResponse
)
from app.model import predictor

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Load model on startup"""
    logger.info("Starting ML Service...")
    success = predictor.load_model()
    if not success:
        logger.warning("Model not loaded - predictions will fail until model is trained")
    yield
    logger.info("Shutting down ML Service...")


app = FastAPI(
    title="Flight Price Prediction API",
    description="ML-powered flight price prediction service using RandomForest",
    version="1.0.0",
    lifespan=lifespan
)

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/", tags=["Root"])
async def root():
    """Root endpoint"""
    return {
        "service": "Flight Price Prediction API",
        "version": "1.0.0",
        "status": "running"
    }


@app.get("/health", response_model=HealthResponse, tags=["Health"])
async def health_check():
    """Health check endpoint"""
    return HealthResponse(
        status="healthy" if predictor.is_loaded else "degraded",
        model_loaded=predictor.is_loaded,
        model_metrics=predictor.get_metrics() if predictor.is_loaded else None
    )


@app.post("/predict", response_model=PredictionResponse, tags=["Prediction"])
async def predict_price(request: PredictionRequest):
    """
    Predict flight price based on input features
    
    - **airline**: Airline name (e.g., Indigo, Air India, Vistara)
    - **source_city**: Departure city
    - **destination_city**: Arrival city
    - **departure_time**: Time of departure (Morning, Afternoon, Evening, Night)
    - **arrival_time**: Time of arrival
    - **stops**: Number of stops (zero, one, two_or_more)
    - **class**: Flight class (Economy, Business)
    - **duration**: Flight duration in hours
    - **days_left**: Days until departure
    """
    if not predictor.is_loaded:
        raise HTTPException(
            status_code=503,
            detail="Model not loaded. Please train the model first by running train.py"
        )
    
    # Prepare features for prediction
    features = {
        "airline": request.airline,
        "source_city": request.source_city,
        "destination_city": request.destination_city,
        "departure_time": request.departure_time,
        "arrival_time": request.arrival_time,
        "stops": request.stops,
        "class": request.flight_class,
        "duration": request.duration,
        "days_left": request.days_left
    }
    
    # Make prediction
    predicted_price = predictor.predict(features)
    
    if predicted_price is None:
        raise HTTPException(
            status_code=500,
            detail="Prediction failed. Check server logs for details."
        )
    
    return PredictionResponse(
        predicted_price=round(predicted_price, 2),
        currency="INR",
        confidence=round(predictor.get_confidence(), 4),
        model_version="1.0"
    )


@app.post("/predict/batch", response_model=BatchPredictionResponse, tags=["Prediction"])
async def predict_batch(request: BatchPredictionRequest):
    """Batch prediction for multiple flights"""
    if not predictor.is_loaded:
        raise HTTPException(
            status_code=503,
            detail="Model not loaded. Please train the model first."
        )
    
    predictions = []
    for flight in request.flights:
        features = {
            "airline": flight.airline,
            "source_city": flight.source_city,
            "destination_city": flight.destination_city,
            "departure_time": flight.departure_time,
            "arrival_time": flight.arrival_time,
            "stops": flight.stops,
            "class": flight.flight_class,
            "duration": flight.duration,
            "days_left": flight.days_left
        }
        
        predicted_price = predictor.predict(features)
        
        predictions.append(PredictionResponse(
            predicted_price=round(predicted_price, 2) if predicted_price else 0,
            currency="INR",
            confidence=round(predictor.get_confidence(), 4),
            model_version="1.0"
        ))
    
    return BatchPredictionResponse(
        predictions=predictions,
        total_count=len(predictions)
    )


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8090)
