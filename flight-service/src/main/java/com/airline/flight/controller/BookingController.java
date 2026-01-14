package com.airline.flight.controller;

import com.airline.flight.dto.BookingRequest;
import com.airline.flight.dto.BookingResponse;
import com.airline.flight.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for booking operations.
 */
@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final BookingService bookingService;

    /**
     * Create a new booking.
     */
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody BookingRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        String userId = getUserIdFromJwt(jwt);
        String userEmail = getUserEmailFromJwt(jwt);

        // Use email from request if provided, otherwise use from JWT
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            userEmail = request.getEmail();
        }

        log.info("Creating booking for user: {} with email: {} on flight: {}", userId, userEmail,
                request.getFlightId());

        BookingResponse booking = bookingService.createBooking(request, userId, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }

    /**
     * Get booking by reference.
     */
    @GetMapping("/{ref}")
    public ResponseEntity<BookingResponse> getBookingByRef(@PathVariable String ref) {
        log.info("Getting booking by reference: {}", ref);
        BookingResponse booking = bookingService.getBookingByRef(ref);
        return ResponseEntity.ok(booking);
    }

    /**
     * Get all bookings for the authenticated user.
     */
    @GetMapping("/my-bookings")
    public ResponseEntity<List<BookingResponse>> getMyBookings(@AuthenticationPrincipal Jwt jwt) {
        String userId = getUserIdFromJwt(jwt);
        log.info("Getting bookings for user: {}", userId);

        List<BookingResponse> bookings = bookingService.getBookingsByUserId(userId);
        return ResponseEntity.ok(bookings);
    }

    /**
     * Cancel a booking.
     */
    @DeleteMapping("/{ref}")
    public ResponseEntity<BookingResponse> cancelBooking(
            @PathVariable String ref,
            @AuthenticationPrincipal Jwt jwt) {

        String userId = getUserIdFromJwt(jwt);
        log.info("Cancelling booking: {} for user: {}", ref, userId);

        BookingResponse booking = bookingService.cancelBooking(ref, userId);
        return ResponseEntity.ok(booking);
    }

    private String getUserIdFromJwt(Jwt jwt) {
        if (jwt == null) {
            // For development without Auth0
            return "dev-user-001";
        }
        // Auth0 uses 'sub' claim for user ID
        return jwt.getClaimAsString("sub");
    }

    private String getUserEmailFromJwt(Jwt jwt) {
        if (jwt == null) {
            // For development without Auth0 - use a dev email
            return "basarozkasli@gmail.com";
        }
        // Auth0 typically uses 'email' claim if scope requested
        String email = jwt.getClaimAsString("email");

        if (email == null || email.isEmpty()) {
            // Fallback for development/testing
            email = "basarozkasli@gmail.com";
        }
        return email;
    }
}
