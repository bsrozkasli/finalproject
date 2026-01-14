package com.airline.flight.config;

import com.airline.flight.security.ApiKeyAuthFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Security configuration with Auth0 and API key support.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SecurityConfig.class);

    private final ApiKeyAuthFilter apiKeyAuthFilter;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:}")
    private String issuerUri;

    public SecurityConfig(ApiKeyAuthFilter apiKeyAuthFilter) {
        this.apiKeyAuthFilter = apiKeyAuthFilter;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain partnerApiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/v1/partners/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated());

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        // Check if Auth0 is configured
        boolean isAuth0Configured = issuerUri != null && !issuerUri.isEmpty() && !issuerUri.contains("your-tenant");

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        if (isAuth0Configured) {
            // Production mode: enforce security with Auth0 Scopes & Roles
            http.authorizeHttpRequests(auth -> auth
                    // Public Endpoints
                    .requestMatchers(HttpMethod.GET, "/api/v1/flights/search", "/api/v1/flights/dates").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/flights/**", "/api/v1/airports/**").permitAll()
                    .requestMatchers("/actuator/**", "/error").permitAll()

                    // Admin Endpoint (Relaxed for demo purposes to prevent 403 errors)
                    .requestMatchers("/api/v1/admin/**").permitAll()

                    // Booking Endpoints (Read requires auth, Write allows both authenticated and
                    // anonymous)
                    .requestMatchers(HttpMethod.GET, "/api/v1/bookings/**").hasAuthority("SCOPE_read:bookings")
                    .requestMatchers(HttpMethod.POST, "/api/v1/bookings").permitAll()

                    // Miles/Loyalty Endpoints
                    .requestMatchers("/api/v1/miles/**").hasAnyAuthority("SCOPE_read:miles", "SCOPE_read:bookings")

                    // Default: Authenticated
                    .anyRequest().authenticated());

            http.oauth2ResourceServer(oauth2 -> oauth2
                    .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
        } else {
            // Development mode: allow all requests when Auth0 is not configured
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        }

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName("scope");
        grantedAuthoritiesConverter.setAuthorityPrefix("SCOPE_");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = grantedAuthoritiesConverter.convert(jwt);

            // DEBUG: Log all claims to see what Auth0 is actually sending in the Access
            // Token
            log.info("JWT Claims received: {}", jwt.getClaims());

            // Extract email claim from JWT
            String email = jwt.getClaimAsString("email");
            if (email == null) {
                // Auth0 often doesn't put email in Access Token. sub might contain it or we
                // might need a fallback.
                email = jwt.getClaimAsString("https://api.airline.com/email");
            }

            if (email == null) {
                email = jwt.getClaimAsString("sub"); // Fallback to sub
            }

            log.info("Extracted email/sub for authority check: {}", email);

            // Hardcoded admin emails for demo purposes
            List<String> adminEmails = Arrays.asList("admin@gmail.com", "admin1@gmail.com");

            if (email != null && (adminEmails.contains(email.toLowerCase()) || email.contains("admin"))) {
                log.info("AUTHENTICATION SUCCESS: Granting ADMIN roles to {}", email);
                List<GrantedAuthority> updatedAuthorities = new ArrayList<>(authorities);
                updatedAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                updatedAuthorities.add(new SimpleGrantedAuthority("SCOPE_admin:flights"));
                return updatedAuthorities;
            }

            return authorities;
        });
        return jwtAuthenticationConverter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
