package com.example.weatherforecast.client;

import com.example.weatherforecast.exception.WeatherServiceException;
import com.example.weatherforecast.model.Coordinates;
import com.example.weatherforecast.model.WeatherResponse;
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

class OpenMeteoClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private CircuitBreaker circuitBreaker;

    private ObjectMapper objectMapper;
    private OpenMeteoClient openMeteoClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();

        // Configure the circuitBreaker mock to simply execute the supplier
        when(circuitBreaker.executeSupplier(any())).thenAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(0);
            return supplier.get();
        });

        openMeteoClient = new OpenMeteoClient(restTemplate, objectMapper, circuitBreaker);
    }

    @Test
    void testGetWeatherForecast_Success() throws Exception {
        Coordinates coordinates = new Coordinates(40.7305, -73.9925);
        String zipCode = "10001";

        String mockResponse = """
                {
                    "current_weather": {
                        "temperature": 22.5
                    },
                    "daily": {
                        "temperature_2m_max": [25.0],
                        "temperature_2m_min": [18.0]
                    },
                    "hourly": {
                        "time": ["2025-04-21T00:00"],
                        "temperature_2m": [20.5]
                    }
                }
                """;

        when(restTemplate.getForEntity(any(URI.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        WeatherResponse result = openMeteoClient.getWeatherForecast(coordinates, zipCode);

        assertNotNull(result);
        assertEquals(zipCode, result.getZipCode());
        assertEquals(22.5, result.getCurrentTemperature(), 0.0001);
        assertEquals(25.0, result.getHighTemperature(), 0.0001);
        assertEquals(18.0, result.getLowTemperature(), 0.0001);
        assertFalse(result.isFromCache());
        assertFalse(result.getHourlyForecast().isEmpty());
    }

    @Test
    void testGetWeatherForecast_NetworkError() {
        Coordinates coordinates = new Coordinates(40.7305, -73.9925);
        String zipCode = "10001";

        when(restTemplate.getForEntity(any(URI.class), eq(String.class)))
                .thenThrow(new RestClientException("Network error"));

        Exception exception = assertThrows(WeatherServiceException.class, () -> {
            openMeteoClient.getWeatherForecast(coordinates, zipCode);
        });

        assertTrue(exception.getMessage().contains("Error communicating with weather service"));
    }

    @Test
    void testGetWeatherForecast_InvalidJsonResponse() {
        Coordinates coordinates = new Coordinates(40.7305, -73.9925);
        String zipCode = "10001";
        String invalidJson = "{invalid json}";

        when(restTemplate.getForEntity(any(URI.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(invalidJson, HttpStatus.OK));

        Exception exception = assertThrows(WeatherServiceException.class, () -> {
            openMeteoClient.getWeatherForecast(coordinates, zipCode);
        });

        assertTrue(exception.getMessage().contains("Error parsing weather service response"));
    }

    @Test
    void testGetWeatherForecast_NullResponse() {
        Coordinates coordinates = new Coordinates(40.7305, -73.9925);
        String zipCode = "10001";

        when(restTemplate.getForEntity(any(URI.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        Exception exception = assertThrows(WeatherServiceException.class, () -> {
            openMeteoClient.getWeatherForecast(coordinates, zipCode);
        });

        assertTrue(exception.getMessage().contains("No weather data received"));
    }

    @Test
    void testGetWeatherForecast_FallbackToHourlyTemperature() throws Exception {
        Coordinates coordinates = new Coordinates(40.7305, -73.9925);
        String zipCode = "10001";

        // Mock response without current_weather
        String mockResponse = """
                {
                    "daily": {
                        "temperature_2m_max": [25.0],
                        "temperature_2m_min": [18.0]
                    },
                    "hourly": {
                        "time": ["2025-04-21T00:00"],
                        "temperature_2m": [20.5]
                    }
                }
                """;

        when(restTemplate.getForEntity(any(URI.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        WeatherResponse result = openMeteoClient.getWeatherForecast(coordinates, zipCode);

        assertNotNull(result);
        assertEquals(20.5, result.getCurrentTemperature(), 0.0001); // Should fallback to first hourly value
    }

    @Test
    void testGetWeatherForecast_CircuitBreakerTest() {
        Coordinates coordinates = new Coordinates(40.7305, -73.9925);
        String zipCode = "10001";

        // Create a new mock for this test to avoid conflicts
        CircuitBreaker testCircuitBreaker = mock(CircuitBreaker.class);
        OpenMeteoClient testClient = new OpenMeteoClient(restTemplate, objectMapper, testCircuitBreaker);

        // Configure circuit breaker to throw an exception
        when(testCircuitBreaker.executeSupplier(any())).thenThrow(new RuntimeException("Circuit breaker open"));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            testClient.getWeatherForecast(coordinates, zipCode);
        });

        assertTrue(exception.getMessage().contains("Circuit breaker open"));
    }
}