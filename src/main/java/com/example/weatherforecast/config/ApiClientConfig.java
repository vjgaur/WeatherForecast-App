package com.example.weatherforecast.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration class for API client components.
 * Sets up RestTemplate and other required beans for making API calls.
 * 
 * @since 1.0
 */
@Configuration
public class ApiClientConfig {
    /**
     * Creates an ObjectMapper bean for JSON serialization and deserialization.
     * Configured with appropriate modules and serialization settings.
     * 
     * @return A configured ObjectMapper instance
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // 5 seconds
        factory.setReadTimeout(5000); // 5 seconds
        return factory;
    }

    /**
     * Creates a RestTemplate bean for making HTTP requests to external APIs.
     * Configured with appropriate connection and read timeouts.
     * 
     * @return A configured RestTemplate instance
     */
    @Bean
    public RestTemplate restTemplate(ClientHttpRequestFactory clientHttpRequestFactory) {
        return new RestTemplate(clientHttpRequestFactory);
    }
}