package com.airline.flight.security;

import com.airline.flight.entity.PartnerAirline;
import com.airline.flight.repository.PartnerAirlineRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

/**
 * Filter for API key authentication for partner endpoints.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-KEY";

    private final PartnerAirlineRepository partnerAirlineRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String apiKey = request.getHeader(API_KEY_HEADER);

        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("Missing API key for partner endpoint: {}", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter()
                    .write("{\"error\": \"Missing API key\", \"message\": \"X-API-KEY header is required\"}");
            return;
        }

        Optional<PartnerAirline> partnerOpt = partnerAirlineRepository.findByApiKeyAndIsActiveTrue(apiKey);

        if (partnerOpt.isEmpty()) {
            log.warn("Invalid or inactive API key: {}", apiKey.substring(0, Math.min(10, apiKey.length())) + "...");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"error\": \"Invalid API key\", \"message\": \"The provided API key is invalid or inactive\"}");
            return;
        }

        PartnerAirline partner = partnerOpt.get();
        log.info("Partner authenticated: {} ({})", partner.getName(), partner.getCode());

        // Create authentication token
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                partner.getCode(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_PARTNER")));

        // Store partner details for later use
        request.setAttribute("partner", partner);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Only filter partner API endpoints
        return !request.getRequestURI().startsWith("/api/v1/partners/");
    }
}
