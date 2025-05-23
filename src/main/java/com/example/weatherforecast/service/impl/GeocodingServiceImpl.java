package com.example.weatherforecast.service.impl;

import com.example.weatherforecast.client.NominatimClient;
import com.example.weatherforecast.exception.GeocodingException;
import com.example.weatherforecast.model.Coordinates;
import com.example.weatherforecast.service.GeocodingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of the GeocodingService interface that uses the Nominatim API.
 * Converts zip/postal codes to geographic coordinates using OpenStreetMap data.
 * 
 * @see GeocodingService
 * @since 1.0
 */
@Service
public class GeocodingServiceImpl implements GeocodingService {

    private final NominatimClient nominatimClient;

    /**
     * Constructs a new GeocodingServiceImpl with the required Nominatim client.
     * 
     * @param nominatimClient Client for accessing the Nominatim geocoding API
     */
    public GeocodingServiceImpl(NominatimClient nominatimClient) {
        this.nominatimClient = nominatimClient;
    }

    /**
     * {@inheritDoc}
     * This implementation uses the Nominatim API to convert zip/postal codes to
     * coordinates.
     * Validates inputs before making the API call.
     */
    @Override
    public Coordinates getCoordinatesForZipCode(String zipCode, String countryCode) throws GeocodingException {
        if (zipCode == null || zipCode.trim().isEmpty()) {
            throw new GeocodingException("Postal code cannot be empty");
        }
        // Sanitize input
        zipCode = zipCode.trim();
        countryCode = (countryCode == null || countryCode.trim().isEmpty()) ? "US" : countryCode.trim().toUpperCase();

        return nominatimClient.getCoordinatesForZipCode(zipCode, countryCode);
    }
}