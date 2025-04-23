package com.example.weatherforecast.client;

import com.example.weatherforecast.exception.GeocodingException;
import com.example.weatherforecast.model.Coordinates;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Client for interacting with the Nominatim geocoding service.
 * Provides functionality to convert zip/postal codes to geographic coordinates.
 * Implements circuit breaker pattern for handling API failures gracefully.
 * 
 * @since 1.0
 */
@Component
public class NominatimClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final CircuitBreaker circuitBreaker;
    private static final String NOMINATIM_API_URL = "https://nominatim.openstreetmap.org/search";

    // Map of country codes to postal code patterns
    private static final Map<String, Pattern> POSTAL_CODE_PATTERNS = new HashMap<>();

    static {
        // Initialize with common postal code formats
        POSTAL_CODE_PATTERNS.put("US", Pattern.compile("^\\d{5}(-\\d{4})?$")); // US: 12345 or 12345-6789
        POSTAL_CODE_PATTERNS.put("CA", Pattern.compile("^[A-Za-z]\\d[A-Za-z]\\s?\\d[A-Za-z]\\d$")); // CA: A1A 1A1
        POSTAL_CODE_PATTERNS.put("GB", Pattern.compile("^[A-Za-z]{1,2}\\d[A-Za-z\\d]?\\s?\\d[A-Za-z]{2}$")); // UK: AB1
                                                                                                             // 2CD
        POSTAL_CODE_PATTERNS.put("AU", Pattern.compile("^\\d{4}$")); // AU: 1234
    }

    /**
     * Constructs a new NominatimClient with required dependencies.
     * 
     * @param restTemplate            RestTemplate for making HTTP requests
     * @param objectMapper            ObjectMapper for JSON
     *                                serialization/deserialization
     * @param nominatimCircuitBreaker Circuit breaker for handling API failures
     */
    public NominatimClient(RestTemplate restTemplate, ObjectMapper objectMapper,
            CircuitBreaker nominatimCircuitBreaker) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.circuitBreaker = nominatimCircuitBreaker;

    }

    /**
     * Get coordinates for a ZIP code using Nominatim API
     * 
     * @param zipCode     ZIP code to geocode
     * @param countryCode Country code (ISO 3166-1 alpha-2)
     * @return Coordinates (latitude and longitude)
     * @throws GeocodingException if geocoding fails
     */
    public Coordinates getCoordinatesForZipCode(String zipCode, String countryCode) throws GeocodingException {
        // Validate inputs
        if (zipCode == null || zipCode.trim().isEmpty()) {
            throw new GeocodingException("Postal code cannot be empty");
        }
        // Wrap the API call with circuit breaker
        return circuitBreaker.executeSupplier(() -> {
            try {
                return fetchCoordinates(zipCode, countryCode);
            } catch (GeocodingException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Builds the query URL for the Nominatim API request.
     * 
     * @param zipCode     The zip or postal code to geocode
     * @param countryCode The ISO 3166-1 alpha-2 country code
     * @return URL string for the API request
     */
    private Coordinates fetchCoordinates(String zipCode, String countryCode) throws GeocodingException {
        zipCode = zipCode.trim();
        countryCode = (countryCode == null || countryCode.trim().isEmpty()) ? "US" : countryCode.trim().toUpperCase();

        // Validate postal code format if pattern exists for the country
        Pattern pattern = POSTAL_CODE_PATTERNS.get(countryCode);
        if (pattern != null && !pattern.matcher(zipCode).matches()) {
            throw new GeocodingException("Invalid postal code format for " + getCountryName(countryCode) +
                    ". Please check and try again.");
        }

        try {
            // Add delay to respect Nominatim usage policy (1 request per second)
            Thread.sleep(1000);
            // Encode the ZIP code to handle special characters
            String encodedZipCode = URLEncoder.encode(zipCode, StandardCharsets.UTF_8);

            URI uri = UriComponentsBuilder.fromUriString(NOMINATIM_API_URL)
                    .queryParam("postalcode", encodedZipCode)
                    .queryParam("country", countryCode)
                    .queryParam("format", "json")
                    .queryParam("limit", 1)
                    .queryParam("email", "application@example.com") // As per Nominatim usage policy
                    .build()
                    .toUri();

            ResponseEntity<String> responseEntity = restTemplate.getForEntity(uri, String.class);
            String response = responseEntity.getBody();

            if (response == null || response.equals("[]")) {
                throw new GeocodingException("No location found for postal code '" + zipCode + "' in " +
                        getCountryName(countryCode) + ". Please verify both postal code and country selection.");
            }

            JsonNode rootNode = objectMapper.readTree(response);

            if (rootNode.size() == 0) {
                throw new GeocodingException("No location found for postal code '" + zipCode + "' in " +
                        getCountryName(countryCode) + ". Please verify both postal code and country selection.");
            }

            JsonNode firstResult = rootNode.get(0);
            double lat = firstResult.path("lat").asDouble();
            double lon = firstResult.path("lon").asDouble();

            return new Coordinates(lat, lon);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GeocodingException("Request interrupted", e);
        } catch (RestClientException e) {
            if (e.getMessage().contains("429")) {
                throw new GeocodingException("Rate limit exceeded. Please try again later.", e);
            }
            throw new GeocodingException("Error communicating with geocoding service: " + e.getMessage(), e);
        } catch (JsonProcessingException e) {
            throw new GeocodingException("Error parsing geocoding service response: " + e.getMessage(), e);
        } catch (Exception e) {
            if (!(e instanceof GeocodingException)) {
                throw new GeocodingException("Unexpected error during geocoding: " + e.getMessage(), e);
            }
            throw e;
        }
    }

    /**
     * Get a readable country name from country code
     */
    private String getCountryName(String countryCode) {
        Map<String, String> countryNames = new HashMap<>();
        countryNames.put("US", "United States");
        countryNames.put("CA", "Canada");
        countryNames.put("GB", "United Kingdom");
        countryNames.put("AU", "Australia");
        countryNames.put("DE", "Germany");
        countryNames.put("FR", "France");
        countryNames.put("JP", "Japan");
        countryNames.put("IN", "India");
        countryNames.put("IT", "Italy");
        countryNames.put("ES", "Spain");
        countryNames.put("NL", "Netherlands");
        countryNames.put("BR", "Brazil");
        countryNames.put("RU", "Russia");
        countryNames.put("CN", "China");

        return countryNames.getOrDefault(countryCode, countryCode);
    }
}