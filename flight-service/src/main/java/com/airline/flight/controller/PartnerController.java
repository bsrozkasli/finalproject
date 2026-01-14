package com.airline.flight.controller;

import com.airline.flight.dto.AddMilesRequest;
import com.airline.flight.dto.MilesAccountResponse;
import com.airline.flight.service.MilesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/partners")
@RequiredArgsConstructor
@Slf4j
public class PartnerController {

        private final MilesService milesService;

        /**
         * Add miles to a user's account from a partner airline.
         * This endpoint should be secured/authenticated in production.
         */
        @PostMapping("/miles")
        public ResponseEntity<MilesAccountResponse> addMiles(@Valid @RequestBody AddMilesRequest request) {
                log.info("Partner Request: Adding {} miles to user {}", request.getAmount(), request.getUserId());

                String description = request.getDescription() != null ? request.getDescription()
                                : "Partner Airline Activity";

                MilesAccountResponse response = milesService.earnMiles(
                                request.getUserId(),
                                request.getAmount(),
                                description,
                                request.getReferenceId());

                return ResponseEntity.ok(response);
        }
}
