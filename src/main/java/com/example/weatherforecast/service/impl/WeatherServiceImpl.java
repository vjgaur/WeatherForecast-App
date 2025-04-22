package com.example.weatherforecast.service.impl;

import com.example.weatherforecast.client.OpenMeteoClient;
import com.example.weatherforecast.exception.GeocodingException;
import com.example.weatherforecast.exception.WeatherServiceException;
import com.example.weatherforecast.model.Coordinates;
import com.example.weatherforecast.model.WeatherResponse;
import com.example.weatherforecast.service.GeocodingService;
import com.example.weatherforecast.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class WeatherServiceImpl implements WeatherService {

    private final GeocodingService geocodingService;
    private final OpenMeteoClient openMeteoClient;

    @Autowired
    public WeatherServiceImpl(GeocodingService geocodingService, OpenMeteoClient openMeteoClient) {
        this.geocodingService = geocodingService;
        this.openMeteoClient = openMeteoClient;
    }

    @Override
    @Cacheable(value = "weatherCache", key = "#zipCode + '_' + #countryCode", condition = "#result != null")
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