package com.airline.notification.listener;

import com.airline.notification.event.BookingCreatedEvent;
import com.airline.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Listener for booking events from RabbitMQ.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BookingEventListener {

    private final EmailService emailService;

    @RabbitListener(queues = "email.queue")
    public void handleBookingCreated(BookingCreatedEvent event) {
        log.info("Received BookingCreatedEvent for booking: {}", event.getBookingRef());

        try {
            emailService.sendBookingConfirmation(event);
            log.info("Successfully processed booking confirmation for: {}", event.getBookingRef());
        } catch (Exception e) {
            log.error("Failed to process BookingCreatedEvent for booking: {}", event.getBookingRef(), e);
            // In production, you might want to send to a dead letter queue
            throw e;
        }
    }
}
