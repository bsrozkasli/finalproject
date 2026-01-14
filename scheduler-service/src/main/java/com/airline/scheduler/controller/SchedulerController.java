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
    private final org.springframework.core.env.Environment environment;

    @jakarta.annotation.PostConstruct
    public void init() {
        log.info("SchedulerController initialized successfully");
        log.info("Available endpoints:");
        log.info("  GET  /api/v1/scheduler/health");
        log.info("  GET  /api/v1/scheduler/status");
        log.info("  POST /api/v1/scheduler/jobs/flight-status/trigger");
        log.info("  GET  /api/v1/scheduler/test/email?to=<email>");
        log.info("  GET  /api/v1/scheduler/test/email/detailed?to=<email>");
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
    public ResponseEntity<Map<String, Object>> testEmail(@RequestParam String to) {
        log.info("========================================");
        log.info("TEST EMAIL REQUEST");
        log.info("========================================");
        log.info("Recipient: {}", to);

        try {
            log.info("Calling emailService.sendWelcomeEmail...");
            emailService.sendWelcomeEmail(to, "TEST123456");
            
            log.info("✅ Email service call completed without exception");
            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "message", "Test email sent successfully to " + to,
                    "note", "Check your inbox and spam folder",
                    "timestamp", java.time.LocalDateTime.now().toString()));
        } catch (Exception e) {
            log.error("❌ Exception caught in testEmail endpoint:", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "FAILED",
                    "message", "Failed to send email: " + e.getMessage(),
                    "error", e.getClass().getSimpleName(),
                    "details", e.toString()));
        }
    }

    /**
     * Test email with detailed configuration info.
     */
    @GetMapping("/test/email/detailed")
    public ResponseEntity<Map<String, Object>> testEmailDetailed(@RequestParam(required = false) String to) {
        log.info("========================================");
        log.info("DETAILED EMAIL TEST");
        log.info("========================================");
        
        Map<String, Object> response = new java.util.HashMap<>();
        
        try {
            // Check email configuration
            String mailHost = environment.getProperty("spring.mail.host", "NOT SET");
            String mailPort = environment.getProperty("spring.mail.port", "NOT SET");
            String mailUsername = environment.getProperty("spring.mail.username", "NOT SET");
            String mailPassword = environment.getProperty("spring.mail.password", "NOT SET");
            boolean passwordSet = mailPassword != null && !mailPassword.isEmpty() && 
                                 !mailPassword.contains("your_app_password");
            
            response.put("configuration", Map.of(
                    "host", mailHost,
                    "port", mailPort,
                    "username", mailUsername,
                    "passwordConfigured", passwordSet,
                    "passwordLength", passwordSet ? mailPassword.length() : 0
            ));
            
            if (to != null && !to.isEmpty()) {
                log.info("Sending test email to: {}", to);
                emailService.sendWelcomeEmail(to, "TEST123456");
                response.put("emailSent", true);
                response.put("recipient", to);
                response.put("status", "SUCCESS");
            } else {
                response.put("emailSent", false);
                response.put("status", "CONFIGURATION_ONLY");
                response.put("note", "Add ?to=your-email@example.com to send test email");
            }
            
        } catch (Exception e) {
            log.error("Error in detailed email test:", e);
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
            response.put("errorType", e.getClass().getName());
        }
        
        return ResponseEntity.ok(response);
    }
}
