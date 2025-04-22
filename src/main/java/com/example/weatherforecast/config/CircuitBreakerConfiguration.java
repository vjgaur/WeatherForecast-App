package com.example.weatherforecast.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class CircuitBreakerConfiguration {
    
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
    
    @Bean
    public CircuitBreaker nominatimCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("nominatimApi");
    }
    
    @Bean
    public CircuitBreaker openMeteoCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("openMeteoApi");
    }
}