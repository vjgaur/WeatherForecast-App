package com.example.weatherforecast.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GeocodingException.class)
    public ResponseEntity<ErrorResponse> handleGeocodingException(GeocodingException ex, WebRequest request) {
        String message = ex.getMessage();
        if (message.startsWith("Invalid postal code format")) {
        } else if (message.contains("No location found")) {
        } else {
            message = "Unable to find location for the provided postal code. Please check your input and try again.";
        }

        ErrorResponse error = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Location Error",
                message,
                request.getDescription(false).replace("uri=", ""));
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(WeatherServiceException.class)
    public ResponseEntity<ErrorResponse> handleWeatherServiceException(WeatherServiceException ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Weather Service Error",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", ""));
        return new ResponseEntity<>(error, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", ""));
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(CallNotPermittedException.class)
public ResponseEntity<ErrorResponse> handleCircuitBreakerException(CallNotPermittedException ex, WebRequest request) {
    ErrorResponse error = ErrorResponse.of(
            HttpStatus.SERVICE_UNAVAILABLE.value(),
            "Service Temporarily Unavailable",
            "The weather service is currently experiencing issues. Please try again later.",
            request.getDescription(false).replace("uri=", "")
    );
    return new ResponseEntity<>(error, HttpStatus.SERVICE_UNAVAILABLE);
}
}