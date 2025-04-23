package com.example.weatherforecast.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object representing weather forecast information.
 * Contains current temperature, high/low temperatures, and hourly forecast
 * data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WeatherResponse {
    /**
     * The zip/postal code this forecast is for.
     */
    private String zipCode;
    /**
     * The geographic coordinates of the location.
     */
    private Coordinates coordinates;
    /**
     * The current temperature in degrees Celsius.
     */
    private double currentTemperature;
    /**
     * The forecasted high temperature for the day in degrees Celsius.
     */
    private double highTemperature;
    /**
     * The forecasted low temperature for the day in degrees Celsius.
     */
    private double lowTemperature;
    /**
     * List of hourly forecast entries for the next 24 hours.
     */
    private List<HourlyForecast> hourlyForecast;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    /**
     * Timestamp when this forecast was generated.
     */
    private LocalDateTime timestamp;
    /**
     * Flag indicating whether this forecast was retrieved from cache.
     */
    private boolean fromCache;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HourlyForecast {
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime time;
        private double temperature;
    }
}