package com.airline.flight.exception;

/**
 * Exception thrown when a flight is full.
 */
public class FlightFullException extends RuntimeException {
    public FlightFullException(String message) {
        super(message);
    }
}
