package com.example.weatherforecast.controller;

import com.example.weatherforecast.model.Coordinates;
import com.example.weatherforecast.model.WeatherResponse;
import com.example.weatherforecast.repository.WeatherCacheRepository;
import com.example.weatherforecast.service.WeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class WeatherControllerTest {

    private MockMvc mockMvc;

    @Mock
    private WeatherService weatherService;

    @Mock
    private WeatherCacheRepository cacheRepository;

    @InjectMocks
    private WeatherController weatherController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(weatherController).build();
    }

    @Test
    public void testGetWeatherByZipCode_Success() throws Exception {
        // Prepare test data
        String zipCode = "10001";
        String countryCode = "US";
        Coordinates coordinates = new Coordinates(40.7305, -73.9925);
        List<WeatherResponse.HourlyForecast> hourlyForecasts = new ArrayList<>();
        hourlyForecasts.add(new WeatherResponse.HourlyForecast(LocalDateTime.now(), 20.5));

        WeatherResponse mockResponse = WeatherResponse.builder()
                .zipCode(zipCode)
                .coordinates(coordinates)
                .currentTemperature(22.5)
                .highTemperature(25.0)
                .lowTemperature(18.0)
                .hourlyForecast(hourlyForecasts)
                .timestamp(LocalDateTime.now())
                .fromCache(false)
                .build();

        // Configure mocks
        when(cacheRepository.isInCache(zipCode + "_" + countryCode)).thenReturn(false);
        when(weatherService.getWeatherForecast(zipCode, countryCode)).thenReturn(mockResponse);

        // Perform test
        mockMvc.perform(get("/api/weather/zipcode/" + zipCode)
                .param("countryCode", countryCode)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.zipCode").value(zipCode))
                .andExpect(jsonPath("$.currentTemperature").value(22.5))
                .andExpect(jsonPath("$.highTemperature").value(25.0))
                .andExpect(jsonPath("$.lowTemperature").value(18.0))
                .andExpect(jsonPath("$.fromCache").value(false));
    }

    @Test
    public void testGetWeatherByZipCode_FromCache() throws Exception {
        // Prepare test data
        String zipCode = "10001";
        String countryCode = "US";
        String cacheKey = zipCode + "_" + countryCode;

        Coordinates coordinates = new Coordinates(40.7305, -73.9925);
        List<WeatherResponse.HourlyForecast> hourlyForecasts = new ArrayList<>();
        hourlyForecasts.add(new WeatherResponse.HourlyForecast(LocalDateTime.now(), 20.5));

        WeatherResponse mockResponse = WeatherResponse.builder()
                .zipCode(zipCode)
                .coordinates(coordinates)
                .currentTemperature(22.5)
                .highTemperature(25.0)
                .lowTemperature(18.0)
                .hourlyForecast(hourlyForecasts)
                .timestamp(LocalDateTime.now())
                .fromCache(true)
                .build();

        // Configure mocks - these are likely not being set up correctly
        when(cacheRepository.isInCache(anyString())).thenReturn(true);
        when(cacheRepository.getFromCache(anyString())).thenReturn(mockResponse);

        // Execute the request and print the response content
        MvcResult result = mockMvc.perform(get("/api/weather/zipcode/" + zipCode)
                .param("countryCode", countryCode)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        System.out.println("Response content: " + content);

        // If we got a valid response, check specific fields
        if (content != null && !content.isEmpty()) {
            mockMvc.perform(get("/api/weather/zipcode/" + zipCode)
                    .param("countryCode", countryCode)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.zipCode").value(zipCode))
                    .andExpect(jsonPath("$.fromCache").value(true));
        } else {
            // If we got an empty response, fail with a more helpful message
            fail("Response body is empty - controller is likely not returning the mock response correctly");
        }
    }
}