package com.example.weatherforecast.repository;

import com.example.weatherforecast.model.WeatherResponse;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

/**
 * Repository for caching and retrieving weather forecast data.
 * Provides methods to check, retrieve, and store weather data in the cache.
 * 
 * @since 1.0
 */
@Component
public class WeatherCacheRepository {

    private final CacheManager cacheManager;

    /**
     * Constructs a new WeatherCacheRepository with the required cache manager.
     * 
     * @param cacheManager The cache manager used for storing and retrieving data
     */
    public WeatherCacheRepository(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * Checks if data for the specified cache key exists in the cache.
     * 
     * @param cacheKey The key to check in the cache
     * @return true if data exists in the cache, false otherwise
     */
    public boolean isInCache(String cacheKey) {
        Cache cache = cacheManager.getCache("weatherCache");
        if (cache != null) {
            Cache.ValueWrapper wrapper = cache.get(cacheKey);
            return wrapper != null && wrapper.get() != null;
        }
        return false;
    }

    /**
     * Retrieves weather data from the cache for the specified key.
     * Sets the fromCache flag to true before returning the data.
     * 
     * @param cacheKey The key to retrieve data for
     * @return The cached WeatherResponse, or null if not found or on error
     */
    public WeatherResponse getFromCache(String cacheKey) {
        Cache cache = cacheManager.getCache("weatherCache");
        if (cache != null) {
            Cache.ValueWrapper wrapper = cache.get(cacheKey);
            if (wrapper != null) {
                Object value = wrapper.get();
                if (value instanceof WeatherResponse) {
                    WeatherResponse response = (WeatherResponse) value;
                    response.setFromCache(true);
                    return response;
                }
            }
        }
        return null;
    }
}