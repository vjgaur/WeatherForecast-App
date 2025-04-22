package com.example.weatherforecast.service;

import com.example.weatherforecast.model.Coordinates;
import com.example.weatherforecast.client.NominatimClient;
import com.example.weatherforecast.exception.GeocodingException;
import com.example.weatherforecast.service.impl.GeocodingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class GeocodingServiceTest {

    @Mock
    private NominatimClient nominatimClient;

    @InjectMocks
    private GeocodingServiceImpl geocodingService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetCoordinatesForZipCode_Success() {
        // Prepare test data
        String zipCode = "10001";
        String countryCode = "US"; // Add country code parameter
        Coordinates expectedCoordinates = new Coordinates(40.7305, -73.9925);

        // Configure mock
        when(nominatimClient.getCoordinatesForZipCode(eq(zipCode), eq(countryCode))).thenReturn(expectedCoordinates);

        // Execute the service method
        Coordinates result = geocodingService.getCoordinatesForZipCode(zipCode, countryCode);

        // Verify the result
        assertNotNull(result);
        assertEquals(expectedCoordinates.getLatitude(), result.getLatitude(), 0.0001);
        assertEquals(expectedCoordinates.getLongitude(), result.getLongitude(), 0.0001);
    }

    @Test
    public void testGetCoordinatesForZipCode_Error() {
        // Prepare test data
        String zipCode = "00000"; // Invalid ZIP code
        String countryCode = "US"; // Add country code parameter

        // Configure mock
        when(nominatimClient.getCoordinatesForZipCode(eq(zipCode), eq(countryCode)))
                .thenThrow(new GeocodingException("No coordinates found"));

        // Execute and verify
        Exception exception = assertThrows(GeocodingException.class, () -> {
            geocodingService.getCoordinatesForZipCode(zipCode, countryCode);
        });

        // Verify the exception message
        assertTrue(exception.getMessage().contains("No coordinates found"));
    }

    @Test
    public void testGetCoordinatesForZipCode_EmptyZipCode() {
        String zipCode = ""; // Empty ZIP code
        String countryCode = "US";

        // Execute and verify
        Exception exception = assertThrows(GeocodingException.class, () -> {
            geocodingService.getCoordinatesForZipCode(zipCode, countryCode);
        });

        // If your service validates empty zip codes
        assertTrue(exception.getMessage().contains("empty") ||
                exception.getMessage().contains("invalid"));
    }

    @Test
    public void testGetCoordinatesForZipCode_NullZipCode() {
        String zipCode = null; // Null ZIP code
        String countryCode = "US";

        // Execute and verify
        Exception exception = assertThrows(GeocodingException.class, () -> {
            geocodingService.getCoordinatesForZipCode(zipCode, countryCode);
        });

        // If your service validates null zip codes
        assertTrue(exception.getMessage().contains("null") ||
                exception.getMessage().contains("empty"));
    }

    @Test
    public void testGetCoordinatesForZipCode_DifferentCountries() {
        // Test for Canada
        String caZipCode = "M5V 3L9";
        String caCountryCode = "CA";
        Coordinates caCoordinates = new Coordinates(43.6426, -79.3871);

        when(nominatimClient.getCoordinatesForZipCode(eq(caZipCode), eq(caCountryCode)))
                .thenReturn(caCoordinates);

        Coordinates caResult = geocodingService.getCoordinatesForZipCode(caZipCode, caCountryCode);
        assertNotNull(caResult);
        assertEquals(caCoordinates.getLatitude(), caResult.getLatitude(), 0.0001);

        // Test for UK
        String ukZipCode = "SW1A 1AA";
        String ukCountryCode = "GB";
        Coordinates ukCoordinates = new Coordinates(51.5014, -0.1419);

        when(nominatimClient.getCoordinatesForZipCode(eq(ukZipCode), eq(ukCountryCode)))
                .thenReturn(ukCoordinates);

        Coordinates ukResult = geocodingService.getCoordinatesForZipCode(ukZipCode, ukCountryCode);
        assertNotNull(ukResult);
        assertEquals(ukCoordinates.getLatitude(), ukResult.getLatitude(), 0.0001);
    }

    @Test
    public void testGetCoordinatesForZipCode_InvalidFormatForCountry() {
        String zipCode = "ABCDE"; // Invalid format
        String countryCode = "US";

        when(nominatimClient.getCoordinatesForZipCode(eq(zipCode), eq(countryCode)))
                .thenThrow(new GeocodingException("Invalid postal code format"));

        Exception exception = assertThrows(GeocodingException.class, () -> {
            geocodingService.getCoordinatesForZipCode(zipCode, countryCode);
        });

        assertTrue(exception.getMessage().contains("Invalid postal code format"));
    }
}