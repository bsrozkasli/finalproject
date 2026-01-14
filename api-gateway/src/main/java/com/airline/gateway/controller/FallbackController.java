package com.airline.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Fallback controller for circuit breaker.
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/flight")
    public ResponseEntity<Map<String, String>> flightServiceFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "status", "SERVICE_UNAVAILABLE",
                        "message", "Flight service is currently unavailable. Please try again later."));
    }

    @GetMapping("/booking")
    public ResponseEntity<Map<String, String>> bookingServiceFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "status", "SERVICE_UNAVAILABLE",
                        "message", "Booking service is currently unavailable. Please try again later."));
    }

    @GetMapping("/notification")
    public ResponseEntity<Map<String, String>> notificationServiceFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "status", "SERVICE_UNAVAILABLE",
                        "message", "Notification service is currently unavailable. Please try again later."));
    }
}
