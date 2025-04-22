package com.example.weatherforecast.service.impl;

import com.example.weatherforecast.client.NominatimClient;
import com.example.weatherforecast.exception.GeocodingException;
import com.example.weatherforecast.model.Coordinates;
import com.example.weatherforecast.service.GeocodingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GeocodingServiceImpl implements GeocodingService {

    private final NominatimClient nominatimClient;

    
    public GeocodingServiceImpl(NominatimClient nominatimClient) {
        this.nominatimClient = nominatimClient;
    }

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