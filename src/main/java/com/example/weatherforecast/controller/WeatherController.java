package com.example.weatherforecast.controller;

import com.example.weatherforecast.exception.ErrorResponse;
import com.example.weatherforecast.model.WeatherResponse;
import com.example.weatherforecast.repository.WeatherCacheRepository;
import com.example.weatherforecast.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private final WeatherService weatherService;
    private final WeatherCacheRepository cacheRepository;

    @Autowired
    public WeatherController(WeatherService weatherService, WeatherCacheRepository cacheRepository) {
        this.weatherService = weatherService;
        this.cacheRepository = cacheRepository;
    }

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