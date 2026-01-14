package com.airline.scheduler.listener;

import com.airline.scheduler.event.BookingCreatedEvent;
import com.airline.scheduler.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Listens to booking events from RabbitMQ and triggers email notifications.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BookingEventListener {

    private final EmailService emailService;

    @RabbitListener(queues = "email.queue")
    public void handleBookingCreated(BookingCreatedEvent event) {
        log.info("========================================");
        log.info("RABBITMQ EVENT RECEIVED");
        log.info("========================================");
        log.info("Booking Ref: {}", event.getBookingRef());
        log.info("User Email: {}", event.getUserEmail());
        log.info("Flight Code: {}", event.getFlightCode());
        log.info("Passenger Count: {}", event.getPassengerCount());
        log.info("Event Class: {}", event.getClass().getName());
        log.info("========================================");

        if (event.getUserEmail() == null || event.getUserEmail().isEmpty()) {
            log.error("User email is null or empty for booking: {}", event.getBookingRef());
            return;
        }

        try {
            log.info("Attempting to send booking confirmation email to: {}", event.getUserEmail());
            emailService.sendBookingConfirmationEmail(event);
            log.info("✅ Booking confirmation email sent successfully for: {} to: {}",
                    event.getBookingRef(), event.getUserEmail());
        } catch (Exception e) {
            log.error("❌ Failed to send booking confirmation email for booking: {}",
                    event.getBookingRef(), e);
            log.error("Error type: {}", e.getClass().getName());
            log.error("Error message: {}", e.getMessage());
            if (e.getCause() != null) {
                log.error("Caused by: {} - {}", e.getCause().getClass().getName(), e.getCause().getMessage());
            }
            // Don't rethrow - we don't want to requeue the message indefinitely
        }
    }
}
