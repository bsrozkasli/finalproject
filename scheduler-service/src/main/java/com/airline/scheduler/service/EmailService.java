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
    
    @org.springframework.beans.factory.annotation.Autowired
    private org.springframework.core.env.Environment environment;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    /**
     * Send a booking confirmation email.
     * Logs errors but does not throw exceptions to prevent blocking the booking
     * process.
     */
    public void sendBookingConfirmationEmail(BookingCreatedEvent event) {
        log.info("Sending booking confirmation email to: {}", event.getUserEmail());

        if (event == null) {
            log.error("❌ Cannot send email: event is null!");
            return;
        }

        if (event.getUserEmail() == null || event.getUserEmail().trim().isEmpty()) {
            log.error("❌ Cannot send email: recipient email is null or empty!");
            return;
        }

        if (fromEmail == null || fromEmail.trim().isEmpty()) {
            log.error("❌ Cannot send email: sender email (spring.mail.username) is not configured!");
            return;
        }

        // Verify email configuration
        String mailPassword = environment.getProperty("spring.mail.password", "");
        if (mailPassword == null || mailPassword.isEmpty() || mailPassword.contains("your_app_password")) {
            log.error("❌ Cannot send email: SMTP password is not configured!");
            log.error("   Set SMTP_PASSWORD environment variable or update application.yml");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(event.getUserEmail());
            message.setSubject("✈️ Booking Confirmed - " + (event.getBookingRef() != null ? event.getBookingRef() : "N/A"));

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

            log.info("Attempting to send email via SMTP...");
            log.info("  From: {}", fromEmail);
            log.info("  To: {}", event.getUserEmail());
            log.info("  Subject: {}", message.getSubject());
            
            emailSender.send(message);
            log.info("✅ Booking confirmation email sent successfully to {}", event.getUserEmail());
        } catch (org.springframework.mail.MailAuthenticationException e) {
            log.error("❌ SMTP Authentication failed! Check your email configuration:");
            log.error("   - Gmail App Password is required (not regular password)");
            log.error("   - Set SMTP_PASSWORD environment variable or update application.yml");
            log.error("   - Get App Password from: https://myaccount.google.com/apppasswords");
            log.error("   - Error: {}", e.getMessage());
        } catch (org.springframework.mail.MailSendException e) {
            log.error("❌ Failed to send email due to SMTP error:");
            log.error("   - Check SMTP server connection (smtp.gmail.com:587)");
            log.error("   - Verify network connectivity");
            log.error("   - Error: {}", e.getMessage());
            if (e.getCause() != null) {
                log.error("   - Cause: {}", e.getCause().getMessage());
            }
        } catch (Exception e) {
            log.error("❌ Failed to send booking confirmation email to {}: {}", event.getUserEmail(), e.getMessage());
            log.error("   Error type: {}", e.getClass().getName());
            if (e.getCause() != null) {
                log.error("   Cause: {} - {}", e.getCause().getClass().getName(), e.getCause().getMessage());
            }
            log.debug("Full exception stack trace: ", e);
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
        
        if (to == null || to.trim().isEmpty()) {
            log.error("❌ Cannot send welcome email: recipient email is null or empty!");
            return;
        }

        if (fromEmail == null || fromEmail.trim().isEmpty()) {
            log.error("❌ Cannot send welcome email: sender email (spring.mail.username) is not configured!");
            return;
        }

        // Verify email configuration
        String mailPassword = environment.getProperty("spring.mail.password", "");
        if (mailPassword == null || mailPassword.isEmpty() || mailPassword.contains("your_app_password")) {
            log.error("❌ Cannot send welcome email: SMTP password is not configured!");
            log.error("   Set SMTP_PASSWORD environment variable or update application.yml");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Welcome to Miles&Smiles!");
            message.setText("Dear Member,\n\n" +
                    "Welcome to Miles&Smiles program. Your member number is: " + (memberNumber != null ? memberNumber : "N/A") + "\n\n" +
                    "Start flying and earning miles today!\n\n" +
                    "Best regards,\nAirline Team");

            emailSender.send(message);
            log.info("✅ Welcome email sent successfully to {}", to);
        } catch (org.springframework.mail.MailAuthenticationException e) {
            log.error("❌ SMTP Authentication failed for welcome email! Check SMTP_PASSWORD configuration.");
            log.error("   Error: {}", e.getMessage());
        } catch (Exception e) {
            log.error("❌ Failed to send welcome email to {}: {}", to, e.getMessage());
            log.error("   Error type: {}", e.getClass().getName());
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
        
        if (to == null || to.trim().isEmpty()) {
            log.error("❌ Cannot send miles update email: recipient email is null or empty!");
            return;
        }

        if (fromEmail == null || fromEmail.trim().isEmpty()) {
            log.error("❌ Cannot send miles update email: sender email (spring.mail.username) is not configured!");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("You Earned Miles!");
            message.setText("Dear Member,\n\n" +
                    "Congratulations! You earned " + milesEarned + " miles from your recent flight " + (flightCode != null ? flightCode : "N/A")
                    + ".\n" +
                    "Your new total balance is: " + newBalance + " miles.\n\n" +
                    "Keep flying with us!\n\n" +
                    "Best regards,\nAirline Team");

            emailSender.send(message);
            log.info("✅ Miles update email sent successfully to {}", to);
        } catch (org.springframework.mail.MailAuthenticationException e) {
            log.error("❌ SMTP Authentication failed for miles update email! Check SMTP_PASSWORD configuration.");
            log.error("   Error: {}", e.getMessage());
        } catch (Exception e) {
            log.error("❌ Failed to send miles update email to {}: {}", to, e.getMessage());
            log.error("   Error type: {}", e.getClass().getName());
            // Don't throw exception - email failure should not block miles processing
        }
    }
}
