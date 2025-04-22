package com.example.weatherforecast.service;

import com.example.weatherforecast.model.Coordinates;
import com.example.weatherforecast.exception.GeocodingException;

public interface GeocodingService {
    /**
     * Convert a ZIP code to coordinates (latitude/longitude)
     * 
     * @param zipCode     The ZIP code to convert
     * @param countryCode The country code (ISO 3166-1 alpha-2)
     * @return Coordinates representing the latitude and longitude
     * @throws GeocodingException if the ZIP code cannot be geocoded
     */
    Coordinates getCoordinatesForZipCode(String zipCode, String countryCode) throws GeocodingException;

}