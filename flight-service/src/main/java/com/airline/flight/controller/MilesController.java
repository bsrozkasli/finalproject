package com.airline.flight.controller;

import com.airline.flight.dto.MilesAccountResponse;
import com.airline.flight.service.MilesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for miles operations.
 */
@RestController
@RequestMapping("/api/v1/miles")
@RequiredArgsConstructor
@Slf4j
public class MilesController {

    private final MilesService milesService;

    /**
     * Get the miles account for the authenticated user.
     * Automatically creates an account if it doesn't exist.
     */
    @GetMapping("/account")
    public ResponseEntity<MilesAccountResponse> getMyAccount(@AuthenticationPrincipal Jwt jwt) {
        String userId = getUserIdFromJwt(jwt);
        log.info("Getting miles account for user: {}", userId);

        MilesAccountResponse account = milesService.getOrCreateAccountResponse(userId);
        return ResponseEntity.ok(account);
    }

    /**
     * Burn miles for a redemption.
     */
    @PostMapping("/burn")
    public ResponseEntity<MilesAccountResponse> burnMiles(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam int amount,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String referenceId) {

        String userId = getUserIdFromJwt(jwt);
        log.info("Burning {} miles for user: {}", amount, userId);

        String desc = description != null ? description : "Miles redemption";
        MilesAccountResponse account = milesService.burnMiles(userId, amount, desc, referenceId);
        return ResponseEntity.ok(account);
    }

    private String getUserIdFromJwt(Jwt jwt) {
        if (jwt == null) {
            return "dev-user-001";
        }
        String userId = jwt.getClaimAsString("sub");
        if (userId == null) {
            userId = jwt.getClaimAsString("oid");
        }
        return userId;
    }
}
