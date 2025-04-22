package com.example.weatherforecast.service;

import com.example.weatherforecast.client.OpenMeteoClient;
import com.example.weatherforecast.exception.GeocodingException;
import com.example.weatherforecast.exception.WeatherServiceException;
import com.example.weatherforecast.model.Coordinates;
import com.example.weatherforecast.model.WeatherResponse;
import com.example.weatherforecast.service.impl.WeatherServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class WeatherServiceTest {

  @Mock
  private GeocodingService geocodingService;

  @Mock
  private OpenMeteoClient openMeteoClient;

  @InjectMocks
  private WeatherServiceImpl weatherService;

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void testGetWeatherForecast_Success() {
    // Prepare test data
    String zipCode = "10001";
    String countryCode = "US";
    Coordinates coordinates = new Coordinates(40.7305, -73.9925);
    List<WeatherResponse.HourlyForecast> hourlyForecasts = new ArrayList<>();
    hourlyForecasts.add(new WeatherResponse.HourlyForecast(LocalDateTime.now(), 20.5));

    WeatherResponse expectedResponse = WeatherResponse.builder()
        .zipCode(zipCode)
        .coordinates(coordinates)
        .currentTemperature(22.5)
        .highTemperature(25.0)
        .lowTemperature(18.0)
        .hourlyForecast(hourlyForecasts)
        .timestamp(LocalDateTime.now())
        .fromCache(false)
        .build();

    // Configure mocks
    when(geocodingService.getCoordinatesForZipCode(zipCode, countryCode)).thenReturn(coordinates);
    when(openMeteoClient.getWeatherForecast(any(Coordinates.class), anyString())).thenReturn(expectedResponse);

    // Execute the service method
    WeatherResponse result = weatherService.getWeatherForecast(zipCode, countryCode);

    // Verify the result
    assertNotNull(result);
    assertEquals(zipCode, result.getZipCode());
    assertEquals(22.5, result.getCurrentTemperature(), 0.0001);
    assertEquals(25.0, result.getHighTemperature(), 0.0001);
    assertEquals(18.0, result.getLowTemperature(), 0.0001);
    assertFalse(result.isFromCache());
  }

  @Test
  public void testGetWeatherForecast_GeocodingError() {
    // Prepare test data
    String zipCode = "00000"; // Invalid ZIP code
    String countryCode = "US";
    // Configure mock
    when(geocodingService.getCoordinatesForZipCode(zipCode, countryCode))
        .thenThrow(new GeocodingException("No coordinates found"));

    // Execute the service method and verify it throws the expected exception
    Exception exception = assertThrows(WeatherServiceException.class, () -> {
      weatherService.getWeatherForecast(zipCode, countryCode);
    });

    // Verify the exception message
    assertTrue(exception.getMessage().contains("Error getting coordinates"));
  }

  @Test
  public void testGetWeatherForecast_WeatherApiError() {
    // Prepare test data
    String zipCode = "10001";
    String countryCode = "US";
    Coordinates coordinates = new Coordinates(40.7305, -73.9925);

    // Configure mocks
    when(geocodingService.getCoordinatesForZipCode(zipCode, countryCode)).thenReturn(coordinates);
    when(openMeteoClient.getWeatherForecast(any(Coordinates.class), anyString()))
        .thenThrow(new WeatherServiceException("Error fetching weather data"));

    // Execute the service method and verify it throws the expected exception
    Exception exception = assertThrows(WeatherServiceException.class, () -> {
      weatherService.getWeatherForecast(zipCode, countryCode);
    });

    // Verify the exception message
    assertTrue(exception.getMessage().contains("Error fetching weather data"));
  }
}