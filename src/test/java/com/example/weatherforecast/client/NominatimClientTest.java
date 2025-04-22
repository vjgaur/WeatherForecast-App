package com.example.weatherforecast.client;

import com.example.weatherforecast.exception.GeocodingException;
import com.example.weatherforecast.model.Coordinates;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class NominatimClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private CircuitBreaker circuitBreaker;

    private ObjectMapper objectMapper;
    private NominatimClient nominatimClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();

        // Configure the circuitBreaker mock to simply execute the supplier
        when(circuitBreaker.executeSupplier(any())).thenAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(0);
            return supplier.get();
        });

        nominatimClient = new NominatimClient(restTemplate, objectMapper, circuitBreaker);
    }

    @Test
    void testGetCoordinatesForZipCode_Success() throws Exception {
        String zipCode = "10001";
        String countryCode = "US";
        String mockResponse = "[{\"lat\":\"40.7305\",\"lon\":\"-73.9925\"}]";

        when(restTemplate.getForEntity(any(URI.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        Coordinates result = nominatimClient.getCoordinatesForZipCode(zipCode, countryCode);

        assertNotNull(result);
        assertEquals(40.7305, result.getLatitude(), 0.0001);
        assertEquals(-73.9925, result.getLongitude(), 0.0001);
    }

    @Test
    void testGetCoordinatesForZipCode_EmptyResponse() {
        String zipCode = "00000";
        String countryCode = "US";

        when(restTemplate.getForEntity(any(URI.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>("[]", HttpStatus.OK));

        Exception exception = assertThrows(GeocodingException.class, () -> {
            nominatimClient.getCoordinatesForZipCode(zipCode, countryCode);
        });

        assertTrue(exception.getMessage().contains("No location found"));
    }

    @Test
    void testGetCoordinatesForZipCode_NetworkError() {
        String zipCode = "10001";
        String countryCode = "US";

        when(restTemplate.getForEntity(any(URI.class), eq(String.class)))
                .thenThrow(new RestClientException("Network error"));

        Exception exception = assertThrows(GeocodingException.class, () -> {
            nominatimClient.getCoordinatesForZipCode(zipCode, countryCode);
        });

        assertTrue(exception.getMessage().contains("Error communicating"));
    }

    @Test
    void testGetCoordinatesForZipCode_InvalidJsonResponse() {
        String zipCode = "10001";
        String countryCode = "US";
        String invalidJson = "[{invalid json}]";

        when(restTemplate.getForEntity(any(URI.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(invalidJson, HttpStatus.OK));

        Exception exception = assertThrows(GeocodingException.class, () -> {
            nominatimClient.getCoordinatesForZipCode(zipCode, countryCode);
        });

        assertTrue(exception.getMessage().contains("Error parsing"));
    }

    @Test
    void testGetCoordinatesForZipCode_InvalidFormatForCountry() {
        String zipCode = "ABCDE"; // Invalid US zip code format
        String countryCode = "US";

        Exception exception = assertThrows(GeocodingException.class, () -> {
            nominatimClient.getCoordinatesForZipCode(zipCode, countryCode);
        });

        assertTrue(exception.getMessage().contains("Invalid postal code format"));
    }

    @Test
    void testGetCoordinatesForZipCode_CircuitBreakerTest() {
        String zipCode = "10001";
        String countryCode = "US";

        // Create a new mock for this test to avoid conflicts
        CircuitBreaker testCircuitBreaker = mock(CircuitBreaker.class);
        NominatimClient testClient = new NominatimClient(restTemplate, objectMapper, testCircuitBreaker);

        // Configure circuit breaker to throw an exception
        when(testCircuitBreaker.executeSupplier(any())).thenThrow(new RuntimeException("Circuit breaker open"));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            testClient.getCoordinatesForZipCode(zipCode, countryCode);
        });

        assertTrue(exception.getMessage().contains("Circuit breaker open"));
    }
}