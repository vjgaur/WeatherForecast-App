package com.example.weatherforecast.exception;

/**
 * Exception thrown when there is an error retrieving weather data.
 * This could be due to API errors, network issues, or invalid input.
 */
public class WeatherServiceException extends RuntimeException {
    /**
     * Constructs a new exception with the specified message.
     * 
     * @param message The error message
     */
    public WeatherServiceException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified message and cause.
     * 
     * @param message The error message
     * @param cause   The cause of the exception
     */
    public WeatherServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}