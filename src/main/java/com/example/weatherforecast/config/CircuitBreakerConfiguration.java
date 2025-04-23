package com.example.weatherforecast.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuration class for setting up Circuit Breaker patterns in the
 * application.
 * Configures circuit breakers for external API calls to improve resilience.
 * 
 * @since 1.0
 */
@Configuration
public class CircuitBreakerConfiguration {
    /**
     * Creates a CircuitBreaker instance for the Nominatim geocoding service.
     * Configured with appropriate timeout, retry, and failure rate thresholds.
     * 
     * @return A CircuitBreaker instance for Nominatim API calls
     */
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50) // Open circuit if 50% of calls fail
                .waitDurationInOpenState(Duration.ofSeconds(60)) // Wait 60s before half-open
                .slidingWindowSize(10) // Use last 10 calls to calculate failure rate
                .permittedNumberOfCallsInHalfOpenState(3) // Allow 3 calls in half-open state
                .minimumNumberOfCalls(5) // Need at least 5 calls to calculate failure rate
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .build();

        return CircuitBreakerRegistry.of(config);
    }

    /**
     * Creates a CircuitBreaker instance for the Nominatim geocoding service.
     * Configured with appropriate timeout, retry, and failure rate thresholds.
     * 
     * @return A CircuitBreaker instance for Nominatim API calls
     */
    @Bean
    public CircuitBreaker nominatimCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("nominatimApi");
    }

    /**
     * Creates a CircuitBreaker instance for the Open-Meteo weather service.
     * Configured with appropriate timeout, retry, and failure rate thresholds.
     * 
     * @return A CircuitBreaker instance for Open-Meteo API calls
     */
    @Bean
    public CircuitBreaker openMeteoCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("openMeteoApi");
    }
}