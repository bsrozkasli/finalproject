package com.airline.flight.exception;

/**
 * Exception thrown when user has insufficient miles.
 */
public class InsufficientMilesException extends RuntimeException {
    public InsufficientMilesException(String message) {
        super(message);
    }
}
