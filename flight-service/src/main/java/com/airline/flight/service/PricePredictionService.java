package com.airline.flight.service;

import com.airline.flight.entity.Flight;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for predicting flight prices using ML model or fallback calculation.
 */
@Service
@Slf4j
public class PricePredictionService {

    @Value("${ml.service.url:http://localhost:8090}")
    private String mlServiceUrl;

    @Value("${ml.service.enabled:true}")
    private boolean mlServiceEnabled;

    private final RestTemplate restTemplate;

    private static final BigDecimal DURATION_FACTOR = new BigDecimal("0.50");
    private static final BigDecimal WEEKEND_MULTIPLIER = new BigDecimal("1.20");
    private static final BigDecimal HIGH_DEMAND_MULTIPLIER = new BigDecimal("1.10");
    private static final double HIGH_DEMAND_THRESHOLD = 0.80;

    // Currency conversion rate (INR to USD approximate)
    private static final BigDecimal INR_TO_USD = new BigDecimal("0.012");

    public PricePredictionService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Predict the price for a flight using ML model with fallback.
     */
    public BigDecimal predictPrice(Flight flight) {
        log.debug("Predicting price for flight: {}", flight.getCode());

        if (mlServiceEnabled) {
            try {
                BigDecimal mlPrice = predictFromMLService(flight);
                if (mlPrice != null) {
                    log.info("ML prediction for {}: ${}", flight.getCode(), mlPrice);
                    return mlPrice;
                }
            } catch (Exception e) {
                log.warn("ML service call failed for {}: {}", flight.getCode(), e.getMessage());
            }
        }

        // Fallback to rule-based calculation
        log.debug("Using fallback calculation for flight: {}", flight.getCode());
        return calculateFallbackPrice(flight);
    }

    /**
     * Call ML service for price prediction.
     */
    private BigDecimal predictFromMLService(Flight flight) {
        try {
            String url = mlServiceUrl + "/predict";

            // Calculate days left until departure
            long daysLeft = ChronoUnit.DAYS.between(
                    java.time.LocalDate.now(),
                    flight.getDepartureTime().toLocalDate());
            daysLeft = Math.max(1, daysLeft); // At least 1 day

            // Determine departure time category
            int hour = flight.getDepartureTime().getHour();
            String departureTime = getTimeCategory(hour);

            // Determine arrival time category
            int arrivalHour = flight.getArrivalTime().getHour();
            String arrivalTime = getTimeCategory(arrivalHour);

            // Build request body
            Map<String, Object> request = new HashMap<>();
            request.put("airline", getAirlineFromCode(flight.getCode()));
            request.put("source_city", mapAirportToCity(flight.getFromAirport()));
            request.put("destination_city", mapAirportToCity(flight.getToAirport()));
            request.put("departure_time", departureTime);
            request.put("arrival_time", arrivalTime);
            request.put("stops", "zero"); // Assuming direct flights
            request.put("class", "Economy"); // Default class
            request.put("duration", flight.getDurationMinutes() / 60.0);
            request.put("days_left", (int) daysLeft);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Object predictedPrice = response.getBody().get("predicted_price");
                if (predictedPrice != null) {
                    // Convert from INR to USD
                    BigDecimal priceInr = new BigDecimal(predictedPrice.toString());
                    BigDecimal priceUsd = priceInr.multiply(INR_TO_USD);
                    return priceUsd.setScale(2, RoundingMode.HALF_UP);
                }
            }

            return null;

        } catch (Exception e) {
            log.error("Error calling ML service: {}", e.getMessage());
            return null;
        }
    }

    private String getTimeCategory(int hour) {
        if (hour >= 4 && hour < 8)
            return "Early_Morning";
        if (hour >= 8 && hour < 12)
            return "Morning";
        if (hour >= 12 && hour < 17)
            return "Afternoon";
        if (hour >= 17 && hour < 21)
            return "Evening";
        if (hour >= 21 || hour < 4)
            return "Night";
        return "Morning";
    }

    private String getAirlineFromCode(String flightCode) {
        // Map flight code prefixes to airline names
        if (flightCode.startsWith("TK"))
            return "Air_India"; // Turkish Airlines -> Air India (closest match)
        if (flightCode.startsWith("AA"))
            return "Vistara";
        if (flightCode.startsWith("UA"))
            return "Indigo";
        if (flightCode.startsWith("BA"))
            return "Air_India";
        if (flightCode.startsWith("LH"))
            return "Vistara";
        return "Indigo"; // Default
    }

    private String mapAirportToCity(String airportCode) {
        // Map airport codes to city names from the dataset
        Map<String, String> airportToCity = Map.of(
                "IST", "Delhi",
                "JFK", "Mumbai",
                "LAX", "Bangalore",
                "LHR", "Kolkata",
                "CDG", "Hyderabad",
                "FRA", "Chennai");
        return airportToCity.getOrDefault(airportCode, "Delhi");
    }

    /**
     * Fallback price calculation when ML service is unavailable.
     */
    private BigDecimal calculateFallbackPrice(Flight flight) {
        BigDecimal predictedPrice = flight.getPrice();

        // Apply duration factor
        long durationMinutes = flight.getDurationMinutes();
        BigDecimal durationAdjustment = DURATION_FACTOR.multiply(BigDecimal.valueOf(durationMinutes / 60.0));
        predictedPrice = predictedPrice.add(durationAdjustment);

        // Apply weekend multiplier
        if (isWeekend(flight)) {
            predictedPrice = predictedPrice.multiply(WEEKEND_MULTIPLIER);
        }

        // Apply high demand multiplier
        if (isHighDemand(flight)) {
            predictedPrice = predictedPrice.multiply(HIGH_DEMAND_MULTIPLIER);
        }

        return predictedPrice.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate suggested price for a new flight.
     */
    public BigDecimal calculateSuggestedPrice(
            String fromAirport,
            String toAirport,
            long durationMinutes,
            boolean isWeekend) {

        BigDecimal basePrice = BigDecimal.valueOf(100);
        BigDecimal durationPrice = DURATION_FACTOR.multiply(BigDecimal.valueOf(durationMinutes));
        basePrice = basePrice.add(durationPrice);

        if (isWeekend) {
            basePrice = basePrice.multiply(WEEKEND_MULTIPLIER);
        }

        return basePrice.setScale(2, RoundingMode.HALF_UP);
    }

    private boolean isWeekend(Flight flight) {
        DayOfWeek dayOfWeek = flight.getDepartureTime().getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    private boolean isHighDemand(Flight flight) {
        if (flight.getCapacity() == 0) {
            return false;
        }
        double occupancyRate = (double) flight.getBookedSeats() / flight.getCapacity();
        return occupancyRate >= HIGH_DEMAND_THRESHOLD;
    }
}
