package com.example.weatherforecast.repository;

import com.example.weatherforecast.model.WeatherResponse;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Component
public class WeatherCacheRepository {

    private final CacheManager cacheManager;

    public WeatherCacheRepository(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public boolean isInCache(String cacheKey) {
        Cache cache = cacheManager.getCache("weatherCache");
        if (cache != null) {
            Cache.ValueWrapper wrapper = cache.get(cacheKey);
            return wrapper != null && wrapper.get() != null;
        }
        return false;
    }

    public WeatherResponse getFromCache(String cacheKey) {
        Cache cache = cacheManager.getCache("weatherCache");
        if (cache != null) {
            Cache.ValueWrapper wrapper = cache.get(cacheKey);
            if (wrapper != null && wrapper.get() != null) {
                WeatherResponse response = (WeatherResponse) wrapper.get();
                response.setFromCache(true);
                return response;
            }
        }
        return null;
    }
}