package com.example.weatherforecast.service.impl;

import com.example.weatherforecast.client.OpenMeteoClient;
import com.example.weatherforecast.exception.GeocodingException;
import com.example.weatherforecast.exception.WeatherServiceException;
import com.example.weatherforecast.model.Coordinates;
import com.example.weatherforecast.model.WeatherResponse;
import com.example.weatherforecast.service.GeocodingService;
import com.example.weatherforecast.service.WeatherService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Implementation of the WeatherService that uses geocoding and weather APIs
 * to retrieve weather forecasts for locations specified by zip/postal codes.
 * 
 * @see WeatherService
 */
@Service
public class WeatherServiceImpl implements WeatherService {

    private final GeocodingService geocodingService;
    private final OpenMeteoClient openMeteoClient;

    /**
     * Constructs a new WeatherServiceImpl with required dependencies.
     * 
     * @param geocodingService Service for converting zip codes to geographic
     *                         coordinates
     * @param openMeteoClient  Client for accessing the Open-Meteo weather API
     */
    public WeatherServiceImpl(GeocodingService geocodingService, OpenMeteoClient openMeteoClient) {
        this.geocodingService = geocodingService;
        this.openMeteoClient = openMeteoClient;
    }

    /**
     * {@inheritDoc}
     * This implementation first converts the zip/postal code to coordinates,
     * then fetches weather data for those coordinates.
     */
    @Override
    @Cacheable(value = "weatherCache", key = "#zipCode + '_' + #countryCode", unless = "#result == null")
    public WeatherResponse getWeatherForecast(String zipCode, String countryCode) throws WeatherServiceException {
        try {
            // Get coordinates for the ZIP code
            Coordinates coordinates = geocodingService.getCoordinatesForZipCode(zipCode, countryCode);

            // Get weather forecast for the coordinates
            return openMeteoClient.getWeatherForecast(coordinates, zipCode);
        } catch (GeocodingException e) {
            throw new WeatherServiceException("Error getting coordinates: " + e.getMessage(), e);
        }
    }
}