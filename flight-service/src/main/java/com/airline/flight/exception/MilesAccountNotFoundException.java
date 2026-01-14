package com.airline.flight.exception;

/**
 * Exception thrown when a miles account is not found.
 */
public class MilesAccountNotFoundException extends RuntimeException {
    public MilesAccountNotFoundException(String message) {
        super(message);
    }
}
