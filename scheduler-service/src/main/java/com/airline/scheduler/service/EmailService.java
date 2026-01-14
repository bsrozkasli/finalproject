package com.airline.scheduler.service;

import com.airline.scheduler.event.BookingCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

/**
 * Email service with async processing support.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender emailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    /**
     * Send a booking confirmation email.
     * Logs errors but does not throw exceptions to prevent blocking the booking
     * process.
     */
    public void sendBookingConfirmationEmail(BookingCreatedEvent event) {
        log.info("Sending booking confirmation email to: {}", event.getUserEmail());

        if (event.getUserEmail() == null || event.getUserEmail().trim().isEmpty()) {
            log.error("❌ Cannot send email: recipient email is null or empty!");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(event.getUserEmail());
            message.setSubject("✈️ Booking Confirmed - " + event.getBookingRef());

            StringBuilder passengersBuilder = new StringBuilder();
            for (int i = 0; i < event.getPassengerNames().size(); i++) {
                passengersBuilder.append(String.format("   %d. %s\n", i + 1, event.getPassengerNames().get(i)));
            }

            String departureDate = event.getDepartureTime() != null
                    ? event.getDepartureTime().format(DATE_FORMATTER)
                    : "TBD";

            String emailBody = String.format("""
                    Dear Traveler,

                    Your flight booking is confirmed! Here are your reservation details:

                    ══════════════════════════════════════
                    RESERVATION: %s
                    ══════════════════════════════════════

                    FLIGHT INFORMATION
                    ──────────────────────────────────────
                    Flight Number: %s
                    From:         %s
                    To:           %s
                    Departure:    %s

                    PASSENGERS (%d)
                    ──────────────────────────────────────
                    %s
                    PAYMENT DETAILS
                    ──────────────────────────────────────
                    Total Amount: $%.2f (Paid)

                    ══════════════════════════════════════

                    Important Information:
                    - Please arrive at the airport at least 2 hours before departure.
                    - Have your booking reference and passport/ID ready for check-in.
                    - You can manage your booking on our website.

                    Thank you for flying with us!

                    Safe travels,
                    Airline Team

                    ──────────────────────────────────────
                    This is an automated email. Please do not reply.
                    """,
                    event.getBookingRef(),
                    event.getFlightCode(),
                    event.getFromAirport(),
                    event.getToAirport(),
                    departureDate,
                    event.getPassengerCount(),
                    passengersBuilder.toString(),
                    event.getPricePaid());

            message.setText(emailBody);

            emailSender.send(message);
            log.info("✅ Booking confirmation email sent successfully to {}", event.getUserEmail());
        } catch (Exception e) {
            log.error("❌ Failed to send booking confirmation email to {}: {}", event.getUserEmail(), e.getMessage());
            log.debug("Full exception details: ", e);
            // Don't throw exception - email failure should not block booking process
        }
    }

    /**
     * Send a welcome email to a new member (async).
     */
    @Async("emailExecutor")
    public CompletableFuture<Void> sendWelcomeEmailAsync(String to, String memberNumber) {
        return CompletableFuture.runAsync(() -> {
            sendWelcomeEmail(to, memberNumber);
        });
    }

    /**
     * Send a welcome email to a new member (synchronous fallback).
     * Logs errors but does not throw exceptions.
     */
    public void sendWelcomeEmail(String to, String memberNumber) {
        log.info("Sending welcome email to {}", to);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Welcome to Miles&Smiles!");
            message.setText("Dear Member,\n\n" +
                    "Welcome to Miles&Smiles program. Your member number is: " + memberNumber + "\n\n" +
                    "Start flying and earning miles today!\n\n" +
                    "Best regards,\nAirline Team");

            emailSender.send(message);
            log.info("Welcome email sent successfully to {}", to);
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}", to, e.getMessage(), e);
            // Don't throw exception - email failure should not block account creation
        }
    }

    /**
     * Send an email notification about earned miles (async).
     */
    @Async("emailExecutor")
    public CompletableFuture<Void> sendMilesUpdateEmailAsync(String to, int milesEarned, int newBalance,
            String flightCode) {
        return CompletableFuture.runAsync(() -> {
            sendMilesUpdateEmail(to, milesEarned, newBalance, flightCode);
        });
    }

    /**
     * Send an email notification about earned miles (synchronous fallback).
     * Logs errors but does not throw exceptions.
     */
    public void sendMilesUpdateEmail(String to, int milesEarned, int newBalance, String flightCode) {
        log.info("Sending miles update email to {}", to);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("You Earned Miles!");
            message.setText("Dear Member,\n\n" +
                    "Congratulations! You earned " + milesEarned + " miles from your recent flight " + flightCode
                    + ".\n" +
                    "Your new total balance is: " + newBalance + " miles.\n\n" +
                    "Keep flying with us!\n\n" +
                    "Best regards,\nAirline Team");

            emailSender.send(message);
            log.info("Miles update email sent successfully to {}", to);
        } catch (Exception e) {
            log.error("Failed to send miles update email to {}: {}", to, e.getMessage(), e);
            // Don't throw exception - email failure should not block miles processing
        }
    }
}
