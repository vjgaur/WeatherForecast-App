package com.example.weatherforecast.service;

import com.example.weatherforecast.model.WeatherResponse;
import com.example.weatherforecast.exception.WeatherServiceException;

public interface WeatherService {
    /**
     * Get weather forecast for a given ZIP code
     * 
     * @param zipCode     The ZIP code to get the forecast for
     * @param countryCode The country code (ISO 3166-1 alpha-2)
     * @return WeatherResponse containing the forecast data
     * @throws WeatherServiceException if the forecast cannot be retrieved
     */
    WeatherResponse getWeatherForecast(String zipCode, String countryCode) throws WeatherServiceException;
}