package com.airline.scheduler.controller;

import com.airline.scheduler.job.FlightStatusJob;
import com.airline.scheduler.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for scheduler service operations and manual job triggers.
 */
@RestController
@RequestMapping("/api/v1/scheduler")
@RequiredArgsConstructor
@Slf4j
public class SchedulerController {

    private final FlightStatusJob flightStatusJob;
    private final EmailService emailService;
    private final org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping requestMappingHandlerMapping;

    @jakarta.annotation.PostConstruct
    public void init() {
        log.info("SchedulerController initialized. dumping mappings:");
        requestMappingHandlerMapping.getHandlerMethods().forEach((key, value) -> {
            log.info("Mapped URL: {}", key);
        });
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "scheduler-service"));
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "scheduler-service",
                "description", "Nightly batch job service",
                "jobs", Map.of(
                        "flightStatusJob", "0 0 0 * * ? (Daily at midnight)")));
    }

    /**
     * Manually trigger the flight status job.
     * Useful for testing.
     */
    @PostMapping("/jobs/flight-status/trigger")
    public ResponseEntity<Map<String, String>> triggerFlightStatusJob() {
        log.info("Manual trigger requested for FlightStatusJob");

        try {
            flightStatusJob.runManually();
            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "message", "FlightStatusJob triggered successfully"));
        } catch (Exception e) {
            log.error("Failed to trigger FlightStatusJob", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "FAILED",
                    "message", e.getMessage()));
        }
    }

    /**
     * Test email sending endpoint.
     * Useful for testing email configuration without frontend.
     */
    @GetMapping("/test/email")
    public ResponseEntity<Map<String, String>> testEmail(@RequestParam String to) {
        log.info("Test email requested to: {}", to);

        try {
            emailService.sendWelcomeEmail(to, "TEST123456");
            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "message", "Test email sent successfully to " + to,
                    "note", "Check your inbox and spam folder"));
        } catch (Exception e) {
            log.error("Failed to send test email to: {}", to, e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "FAILED",
                    "message", "Failed to send email: " + e.getMessage(),
                    "error", e.getClass().getSimpleName()));
        }
    }
}
