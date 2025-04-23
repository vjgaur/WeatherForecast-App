package com.example.weatherforecast.controller;

import com.example.weatherforecast.exception.ErrorResponse;
import com.example.weatherforecast.exception.WeatherServiceException;
import com.example.weatherforecast.model.WeatherResponse;
import com.example.weatherforecast.repository.WeatherCacheRepository;
import com.example.weatherforecast.service.WeatherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

/**
 * REST controller that handles weather-related API endpoints.
 * Provides functionality to fetch weather information based on zip/postal codes
 * and country codes, with caching support.
 * 
 * @author Vijayendra Gaur
 * @version 1.0
 * @since 2025-04-24
 */
@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private final WeatherService weatherService;
    private final WeatherCacheRepository cacheRepository;

    /**
     * Constructs a new WeatherController with the required dependencies.
     * 
     * @param weatherService  Service for retrieving weather forecasts
     * @param cacheRepository Repository for caching weather data
     */
    public WeatherController(WeatherService weatherService, WeatherCacheRepository cacheRepository) {
        this.weatherService = weatherService;
        this.cacheRepository = cacheRepository;
    }

    /**
     * Gets weather information for a specified zip/postal code and country code.
     * Checks cache first before making external API calls.
     * 
     * @param zipCode     The zip or postal code to get weather for
     * @param countryCode The ISO 3166-1 alpha-2 country code (defaults to "US" if
     *                    not provided)
     * @return ResponseEntity containing the weather forecast data
     * @throws WeatherServiceException if there's an error retrieving weather data
     */
    @GetMapping("/zipcode/{zipCode}")
    public ResponseEntity<WeatherResponse> getWeatherByZipCode(
            @PathVariable String zipCode,
            @RequestParam(required = false, defaultValue = "US") String countryCode) {

        // Check if data is in cache
        String cacheKey = zipCode + "_" + countryCode;
        if (cacheRepository.isInCache(cacheKey)) {
            WeatherResponse cachedResponse = cacheRepository.getFromCache(cacheKey);
            return ResponseEntity.ok(cachedResponse);
        }

        // If not in cache, get fresh data
        WeatherResponse response = weatherService.getWeatherForecast(zipCode, countryCode);
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.of(
                500,
                "Internal Server Error",
                ex.getMessage(),
                request.getRequestURI());
        return ResponseEntity.status(500).body(error);
    }
}