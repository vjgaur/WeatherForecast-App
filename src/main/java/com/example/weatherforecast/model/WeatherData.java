package com.example.weatherforecast.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Model class representing raw weather data from the external API.
 * Contains temperatures and hourly forecasts in the format returned by
 * Open-Meteo.
 * Used as an intermediate representation before conversion to WeatherResponse.
 * 
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherData {
    /**
     * The current temperature in degrees Celsius.
     */
    private double currentTemperature;
    /**
     * The maximum temperature for the day in degrees Celsius.
     */
    private double highTemperature;
    /**
     * The minimum temperature for the day in degrees Celsius.
     */
    private double lowTemperature;
    /**
     * List of temperatures for hourly forecasts in degrees Celsius.
     */
    private List<HourlyForecast> hourlyForecast;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    private boolean fromCache;

    /**
     * Model class representing an hourly weather forecast entry.
     * Contains time and temperature information for a specific hour.
     * 
     * @since 1.0
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HourlyForecast {
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime time;
        private double temperature;
    }
}