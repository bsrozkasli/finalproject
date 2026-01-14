package com.airline.flight.exception;

/**
 * Exception thrown when a flight is not found.
 */
public class FlightNotFoundException extends RuntimeException {
    public FlightNotFoundException(String message) {
        super(message);
    }
}
