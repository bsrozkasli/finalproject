package com.airline.notification.service;

import com.airline.notification.event.BookingCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

/**
 * Service for sending email notifications.
 * This is a mock implementation that logs emails instead of actually sending
 * them.
 */
@Service
@Slf4j
public class EmailService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    /**
     * Send booking confirmation email.
     */
    public void sendBookingConfirmation(BookingCreatedEvent event) {
        log.info("========================================");
        log.info("SENDING CONFIRMATION EMAIL");
        log.info("========================================");
        log.info("To: {}", event.getUserEmail());
        log.info("Subject: Booking Confirmation - {}", event.getBookingRef());
        log.info("----------------------------------------");
        log.info("Dear Customer,");
        log.info("");
        log.info("Your booking has been confirmed!");
        log.info("");
        log.info("Booking Reference: {}", event.getBookingRef());
        log.info("Flight: {}", event.getFlightCode());
        log.info("Route: {} -> {}", event.getFromAirport(), event.getToAirport());
        log.info("Departure: {}", event.getDepartureTime().format(DATE_FORMATTER));
        log.info("Passengers: {}", event.getPassengerCount());

        if (event.getPassengerNames() != null && !event.getPassengerNames().isEmpty()) {
            log.info("Passenger Names:");
            for (String name : event.getPassengerNames()) {
                log.info("  - {}", name);
            }
        }

        log.info("Total Paid: ${}", event.getPricePaid());
        log.info("");
        log.info("Thank you for choosing our airline!");
        log.info("========================================");
    }

    /**
     * Send booking cancellation email.
     */
    public void sendBookingCancellation(String email, String bookingRef, String flightCode) {
        log.info("========================================");
        log.info("SENDING CANCELLATION EMAIL");
        log.info("========================================");
        log.info("To: {}", email);
        log.info("Subject: Booking Cancelled - {}", bookingRef);
        log.info("----------------------------------------");
        log.info("Dear Customer,");
        log.info("");
        log.info("Your booking {} for flight {} has been cancelled.", bookingRef, flightCode);
        log.info("");
        log.info("If you did not request this cancellation, please contact us immediately.");
        log.info("");
        log.info("Thank you for your understanding.");
        log.info("========================================");
    }

    /**
     * Send flight status update email.
     */
    public void sendFlightStatusUpdate(String email, String flightCode, String status) {
        log.info("========================================");
        log.info("SENDING FLIGHT STATUS UPDATE EMAIL");
        log.info("========================================");
        log.info("To: {}", email);
        log.info("Subject: Flight Status Update - {}", flightCode);
        log.info("----------------------------------------");
        log.info("Dear Customer,");
        log.info("");
        log.info("Your flight {} status has been updated to: {}", flightCode, status);
        log.info("");
        log.info("Please check your booking for more details.");
        log.info("========================================");
    }

    /**
     * Send miles earned notification.
     */
    public void sendMilesEarnedNotification(String email, int miles, String flightCode, int newBalance) {
        log.info("========================================");
        log.info("SENDING MILES EARNED EMAIL");
        log.info("========================================");
        log.info("To: {}", email);
        log.info("Subject: You've earned {} miles!", miles);
        log.info("----------------------------------------");
        log.info("Dear Customer,");
        log.info("");
        log.info("Congratulations! You've earned {} miles for your flight {}.", miles, flightCode);
        log.info("");
        log.info("Your new miles balance: {}", newBalance);
        log.info("");
        log.info("Keep flying with us to earn more rewards!");
        log.info("========================================");
    }
}
