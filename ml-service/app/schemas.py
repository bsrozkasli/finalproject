"""
Pydantic schemas for API request/response models
"""

from pydantic import BaseModel, Field
from typing import Optional, List
from enum import Enum


class DepartureTime(str, Enum):
    EARLY_MORNING = "Early_Morning"
    MORNING = "Morning"
    AFTERNOON = "Afternoon"
    EVENING = "Evening"
    NIGHT = "Night"
    LATE_NIGHT = "Late_Night"


class Stops(str, Enum):
    ZERO = "zero"
    ONE = "one"
    TWO_OR_MORE = "two_or_more"


class FlightClass(str, Enum):
    ECONOMY = "Economy"
    BUSINESS = "Business"


class PredictionRequest(BaseModel):
    """Request model for price prediction"""
    airline: str = Field(..., description="Airline name", example="Indigo")
    source_city: str = Field(..., description="Departure city", example="Delhi")
    destination_city: str = Field(..., description="Arrival city", example="Mumbai")
    departure_time: str = Field(..., description="Time of departure", example="Morning")
    arrival_time: str = Field(default="Morning", description="Time of arrival", example="Afternoon")
    stops: str = Field(default="zero", description="Number of stops", example="zero")
    flight_class: str = Field(default="Economy", alias="class", description="Flight class", example="Economy")
    duration: float = Field(..., description="Flight duration in hours", example=2.5, ge=0)
    days_left: int = Field(default=15, description="Days until departure", example=15, ge=0)

    class Config:
        populate_by_name = True


class PredictionResponse(BaseModel):
    """Response model for price prediction"""
    predicted_price: float = Field(..., description="Predicted flight price")
    currency: str = Field(default="INR", description="Currency of the price")
    confidence: float = Field(..., description="Model confidence (R² score)")
    model_version: str = Field(default="1.0", description="Model version")


class HealthResponse(BaseModel):
    """Response model for health check"""
    status: str
    model_loaded: bool
    model_metrics: Optional[dict] = None


class BatchPredictionRequest(BaseModel):
    """Request model for batch predictions"""
    flights: List[PredictionRequest]


class BatchPredictionResponse(BaseModel):
    """Response model for batch predictions"""
    predictions: List[PredictionResponse]
    total_count: int
