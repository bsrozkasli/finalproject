package com.airline.notification.service;

import com.airline.notification.event.BookingCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

/**
 * Service for sending email notifications via SMTP.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender emailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @org.springframework.beans.factory.annotation.Autowired
    private org.springframework.core.env.Environment environment;

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

        if (event == null) {
            log.error("❌ Cannot send email: event is null!");
            return;
        }

        if (event.getUserEmail() == null || event.getUserEmail().trim().isEmpty()) {
            log.error("❌ Cannot send email: recipient email is null or empty!");
            return;
        }

        if (fromEmail == null || fromEmail.trim().isEmpty() || fromEmail.contains("your_email")) {
            log.warn("⚠️ SMTP not configured - falling back to log-only mode");
            logEmailContent(event);
            return;
        }

        // Verify email configuration
        String mailPassword = environment.getProperty("spring.mail.password", "");
        if (mailPassword == null || mailPassword.isEmpty() || mailPassword.contains("your_app_password")) {
            log.warn("⚠️ SMTP password not configured - falling back to log-only mode");
            log.warn("   Set SMTP_PASSWORD environment variable to enable real email sending");
            logEmailContent(event);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(event.getUserEmail());
            message.setSubject("✈️ Booking Confirmed - " + event.getBookingRef());

            StringBuilder passengersBuilder = new StringBuilder();
            if (event.getPassengerNames() != null && !event.getPassengerNames().isEmpty()) {
                for (int i = 0; i < event.getPassengerNames().size(); i++) {
                    String passengerName = event.getPassengerNames().get(i);
                    if (passengerName != null && !passengerName.trim().isEmpty()) {
                        passengersBuilder.append(String.format("   %d. %s\n", i + 1, passengerName));
                    }
                }
            } else {
                passengersBuilder.append("   (Passenger names not available)\n");
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
                    SkyWings Airline Team

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

            log.info("Attempting to send email via SMTP...");
            log.info("  From: {}", fromEmail);
            log.info("  To: {}", event.getUserEmail());

            emailSender.send(message);
            log.info("✅ Booking confirmation email sent successfully to {}", event.getUserEmail());

        } catch (org.springframework.mail.MailAuthenticationException e) {
            log.error("❌ SMTP Authentication failed! Check your email configuration:");
            log.error("   - Gmail App Password is required (not regular password)");
            log.error("   - Set SMTP_PASSWORD environment variable");
            log.error("   - Get App Password from: https://myaccount.google.com/apppasswords");
            log.error("   - Error: {}", e.getMessage());
            logEmailContent(event);
        } catch (org.springframework.mail.MailSendException e) {
            log.error("❌ Failed to send email due to SMTP error:");
            log.error("   - Check SMTP server connection (smtp.gmail.com:587)");
            log.error("   - Verify network connectivity");
            log.error("   - Error: {}", e.getMessage());
            logEmailContent(event);
        } catch (Exception e) {
            log.error("❌ Failed to send booking confirmation email to {}: {}", event.getUserEmail(), e.getMessage());
            log.error("   Error type: {}", e.getClass().getName());
            logEmailContent(event);
        }
    }

    /**
     * Log email content (fallback when SMTP is not configured)
     */
    private void logEmailContent(BookingCreatedEvent event) {
        log.info("----------------------------------------");
        log.info("[LOG-ONLY MODE] Email content:");
        log.info("Dear Customer,");
        log.info("");
        log.info("Your booking has been confirmed!");
        log.info("");
        log.info("Booking Reference: {}", event.getBookingRef());
        log.info("Flight: {}", event.getFlightCode());
        log.info("Route: {} -> {}", event.getFromAirport(), event.getToAirport());
        if (event.getDepartureTime() != null) {
            log.info("Departure: {}", event.getDepartureTime().format(DATE_FORMATTER));
        }
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

        if (email == null || email.trim().isEmpty()) {
            log.error("❌ Cannot send cancellation email: recipient is null or empty");
            return;
        }

        if (fromEmail == null || fromEmail.contains("your_email")) {
            log.warn("⚠️ SMTP not configured - logging cancellation only");
            log.info("Your booking {} for flight {} has been cancelled.", bookingRef, flightCode);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("Booking Cancelled - " + bookingRef);
            message.setText(String.format("""
                    Dear Customer,

                    Your booking %s for flight %s has been cancelled.

                    If you did not request this cancellation, please contact us immediately.

                    Thank you for your understanding.

                    SkyWings Airline Team
                    """, bookingRef, flightCode));

            emailSender.send(message);
            log.info("✅ Cancellation email sent to {}", email);
        } catch (Exception e) {
            log.error("❌ Failed to send cancellation email: {}", e.getMessage());
        }
    }

    /**
     * Send flight status update email.
     */
    public void sendFlightStatusUpdate(String email, String flightCode, String status) {
        log.info("========================================");
        log.info("SENDING FLIGHT STATUS UPDATE EMAIL");
        log.info("========================================");
        log.info("To: {}", email);
        log.info("Flight {} status: {}", flightCode, status);

        if (email == null || email.trim().isEmpty()) {
            log.error("❌ Cannot send status update: recipient is null or empty");
            return;
        }

        if (fromEmail == null || fromEmail.contains("your_email")) {
            log.warn("⚠️ SMTP not configured - logging status update only");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("Flight Status Update - " + flightCode);
            message.setText(String.format("""
                    Dear Customer,

                    Your flight %s status has been updated to: %s

                    Please check your booking for more details.

                    SkyWings Airline Team
                    """, flightCode, status));

            emailSender.send(message);
            log.info("✅ Status update email sent to {}", email);
        } catch (Exception e) {
            log.error("❌ Failed to send status update email: {}", e.getMessage());
        }
    }

    /**
     * Send miles earned notification.
     */
    public void sendMilesEarnedNotification(String email, int miles, String flightCode, int newBalance) {
        log.info("========================================");
        log.info("SENDING MILES EARNED EMAIL");
        log.info("========================================");
        log.info("To: {}", email);
        log.info("Miles earned: {} for flight {}", miles, flightCode);

        if (email == null || email.trim().isEmpty()) {
            log.error("❌ Cannot send miles notification: recipient is null or empty");
            return;
        }

        if (fromEmail == null || fromEmail.contains("your_email")) {
            log.warn("⚠️ SMTP not configured - logging miles earned only");
            log.info("You earned {} miles. New balance: {}", miles, newBalance);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("You've earned " + miles + " miles!");
            message.setText(String.format("""
                    Dear Customer,

                    Congratulations! You've earned %d miles for your flight %s.

                    Your new miles balance: %d

                    Keep flying with us to earn more rewards!

                    SkyWings Airline Team
                    """, miles, flightCode, newBalance));

            emailSender.send(message);
            log.info("✅ Miles notification sent to {}", email);
        } catch (Exception e) {
            log.error("❌ Failed to send miles notification: {}", e.getMessage());
        }
    }
}
