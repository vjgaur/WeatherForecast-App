package com.example.weatherforecast.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WeatherResponse {
    private String zipCode;
    private Coordinates coordinates;
    private double currentTemperature;
    private double highTemperature;
    private double lowTemperature;
    private List<HourlyForecast> hourlyForecast;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
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