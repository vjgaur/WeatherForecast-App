package com.example.weatherforecast.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model class representing geographic coordinates.
 * Contains latitude and longitude values for a location.
 * 
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Coordinates {
    /**
     * The latitude of the location in decimal degrees.
     * Positive values indicate north of the equator,
     * negative values indicate south of the equator.
     */
    private double latitude;
    /**
     * The longitude of the location in decimal degrees.
     * Positive values indicate east of the prime meridian,
     * negative values indicate west of the prime meridian.
     */
    private double longitude;
}