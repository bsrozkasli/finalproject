package com.airline.flight.exception;

/**
 * Exception thrown when a booking is not found.
 */
public class BookingNotFoundException extends RuntimeException {
    public BookingNotFoundException(String message) {
        super(message);
    }
}
