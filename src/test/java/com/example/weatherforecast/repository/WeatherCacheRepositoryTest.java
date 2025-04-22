package com.example.weatherforecast.repository;

import com.example.weatherforecast.model.Coordinates;
import com.example.weatherforecast.model.WeatherResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WeatherCacheRepositoryTest {

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    private WeatherCacheRepository weatherCacheRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        weatherCacheRepository = new WeatherCacheRepository(cacheManager);
    }

    @Test
    void testIsInCache_CacheExists() {
        String cacheKey = "10001_US";
        Cache.ValueWrapper valueWrapper = mock(Cache.ValueWrapper.class);

        when(cacheManager.getCache("weatherCache")).thenReturn(cache);
        when(cache.get(cacheKey)).thenReturn(valueWrapper);
        when(valueWrapper.get()).thenReturn(createMockWeatherResponse());

        boolean result = weatherCacheRepository.isInCache(cacheKey);

        assertTrue(result);
    }

    @Test
    void testIsInCache_CacheDoesNotExist() {
        String cacheKey = "10001_US";

        when(cacheManager.getCache("weatherCache")).thenReturn(cache);
        when(cache.get(cacheKey)).thenReturn(null);

        boolean result = weatherCacheRepository.isInCache(cacheKey);

        assertFalse(result);
    }

    @Test
    void testIsInCache_NoCacheManager() {
        String cacheKey = "10001_US";

        when(cacheManager.getCache("weatherCache")).thenReturn(null);

        boolean result = weatherCacheRepository.isInCache(cacheKey);

        assertFalse(result);
    }

    @Test
    void testGetFromCache_Success() {
        String cacheKey = "10001_US";
        WeatherResponse mockResponse = createMockWeatherResponse();
        Cache.ValueWrapper valueWrapper = mock(Cache.ValueWrapper.class);

        when(cacheManager.getCache("weatherCache")).thenReturn(cache);
        when(cache.get(cacheKey)).thenReturn(valueWrapper);
        when(valueWrapper.get()).thenReturn(mockResponse);

        WeatherResponse result = weatherCacheRepository.getFromCache(cacheKey);

        assertNotNull(result);
        assertTrue(result.isFromCache());
        assertEquals(mockResponse.getZipCode(), result.getZipCode());
    }

    @Test
    void testGetFromCache_NoValueWrapper() {
        String cacheKey = "10001_US";

        when(cacheManager.getCache("weatherCache")).thenReturn(cache);
        when(cache.get(cacheKey)).thenReturn(null);

        WeatherResponse result = weatherCacheRepository.getFromCache(cacheKey);

        assertNull(result);
    }

    @Test
    void testGetFromCache_NoCacheManager() {
        String cacheKey = "10001_US";

        when(cacheManager.getCache("weatherCache")).thenReturn(null);

        WeatherResponse result = weatherCacheRepository.getFromCache(cacheKey);

        assertNull(result);
    }

    @Test
    void testGetFromCache_NullValue() {
        String cacheKey = "10001_US";
        Cache.ValueWrapper valueWrapper = mock(Cache.ValueWrapper.class);

        when(cacheManager.getCache("weatherCache")).thenReturn(cache);
        when(cache.get(cacheKey)).thenReturn(valueWrapper);
        when(valueWrapper.get()).thenReturn(null);

        WeatherResponse result = weatherCacheRepository.getFromCache(cacheKey);

        assertNull(result);
    }

    private WeatherResponse createMockWeatherResponse() {
        return WeatherResponse.builder()
                .zipCode("10001")
                .coordinates(new Coordinates(40.7305, -73.9925))
                .currentTemperature(22.5)
                .highTemperature(25.0)
                .lowTemperature(18.0)
                .hourlyForecast(new ArrayList<>())
                .timestamp(LocalDateTime.now())
                .fromCache(false)
                .build();
    }
}