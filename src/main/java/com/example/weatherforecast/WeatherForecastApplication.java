package com.example.weatherforecast;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Main entry point for the Weather Forecast application.
 * Bootstraps the Spring Boot application with all required configurations.
 * 
 * @since 1.0
 */
@SpringBootApplication
@EnableCaching
public class WeatherForecastApplication {
	/**
	 * Main method that starts the Spring Boot application.
	 * 
	 * @param args Command line arguments passed to the application
	 */
	public static void main(String[] args) {
		SpringApplication.run(WeatherForecastApplication.class, args);
	}

}