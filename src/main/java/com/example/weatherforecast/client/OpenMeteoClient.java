package com.example.weatherforecast.client;

import com.example.weatherforecast.exception.WeatherServiceException;
import com.example.weatherforecast.model.Coordinates;
import com.example.weatherforecast.model.WeatherResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class OpenMeteoClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private static final String OPEN_METEO_API_URL = "https://api.open-meteo.com/v1/forecast";

    @Autowired
    public OpenMeteoClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Get weather forecast for specific coordinates
     * 
     * @param coordinates Latitude and longitude
     * @param zipCode     ZIP code for the response
     * @return Weather forecast data
     * @throws WeatherServiceException if weather data fetch fails
     */
    public WeatherResponse getWeatherForecast(Coordinates coordinates, String zipCode) throws WeatherServiceException {
        try {
            URI uri = UriComponentsBuilder.fromUriString(OPEN_METEO_API_URL)
                    .queryParam("latitude", coordinates.getLatitude())
                    .queryParam("longitude", coordinates.getLongitude())
                    .queryParam("hourly", "temperature_2m")
                    .queryParam("daily", "temperature_2m_max,temperature_2m_min")
                    .queryParam("current_weather", "true")
                    .queryParam("timezone", "auto")
                    .build()
                    .toUri();

            ResponseEntity<String> responseEntity = restTemplate.getForEntity(uri, String.class);
            String response = responseEntity.getBody();

            if (response == null) {
                throw new WeatherServiceException("No weather data received from weather service");
            }

            JsonNode rootNode = objectMapper.readTree(response);

            // Extract current temperature
            double currentTemp;
            if (rootNode.has("current_weather") && rootNode.get("current_weather").has("temperature")) {
                currentTemp = rootNode.get("current_weather").get("temperature").asDouble();
            } else {
                // Fallback to first hourly value if current_weather not available
                currentTemp = rootNode.path("hourly").path("temperature_2m").get(0).asDouble();
            }

            // Extract high and low temperature
            double highTemp = rootNode.path("daily").path("temperature_2m_max").get(0).asDouble();
            double lowTemp = rootNode.path("daily").path("temperature_2m_min").get(0).asDouble();

            // Create hourly forecast for next 24 hours
            List<WeatherResponse.HourlyForecast> hourlyForecast = new ArrayList<>();
            JsonNode hourlyTemp = rootNode.path("hourly").path("temperature_2m");
            JsonNode hourlyTime = rootNode.path("hourly").path("time");
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

            int forecastHours = Math.min(24, hourlyTemp.size());
            for (int i = 0; i < forecastHours; i++) {
                String timeStr = hourlyTime.get(i).asText();
                System.out.println("Raw time from API: " + timeStr);

                LocalDateTime forecastTime;

                // Handle different time formats that might be returned by the API
                if (timeStr.contains("T")) {
                    forecastTime = LocalDateTime.parse(timeStr, formatter);
                } else {
                    // If only date is provided, add time
                    forecastTime = LocalDateTime.parse(timeStr + "T00:00:00", formatter).plusHours(i);
                }

                double temperature = hourlyTemp.get(i).asDouble();
                hourlyForecast.add(new WeatherResponse.HourlyForecast(forecastTime, temperature));
            }

            // Build response
            return WeatherResponse.builder()
                    .zipCode(zipCode)
                    .coordinates(coordinates)
                    .currentTemperature(currentTemp)
                    .highTemperature(highTemp)
                    .lowTemperature(lowTemp)
                    .hourlyForecast(hourlyForecast)
                    .timestamp(LocalDateTime.now())
                    .fromCache(false)
                    .build();

        } catch (RestClientException e) {
            throw new WeatherServiceException("Error communicating with weather service: " + e.getMessage(), e);
        } catch (JsonProcessingException e) {
            throw new WeatherServiceException("Error parsing weather service response: " + e.getMessage(), e);
        }
    }
}