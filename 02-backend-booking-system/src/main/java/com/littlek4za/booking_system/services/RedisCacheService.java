package com.littlek4za.booking_system.services;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RedisCacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisCacheService(RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public <T> T get(String key, Class<T> clazz) {
        try {
            String json = redisTemplate.opsForValue().get(key);

            if (json == null) {
                return null;
            }

            return objectMapper.readValue(json, clazz);

        } catch (Exception e) {

            log.warn("Redis GET failed for key={}", key, e);
            return null;
        }
    }

    public <T> T getList(String key, TypeReference<T> typeRef) {

        try {
            String json = redisTemplate.opsForValue().get(key);

            if (json == null)
                return null;

            return objectMapper.readValue(json, typeRef);

        } catch (Exception e) {
            log.warn("Redis GET LIST failed for key={}", key, e);
            return null;
        }
    }

    public void set(String key, Object value, Duration ttl) {

        try {

            String json = objectMapper.writeValueAsString(value);

            redisTemplate.opsForValue().set(key, json, ttl);

        } catch (Exception e) {

            log.warn("Redis SET failed for key={}", key, e);
        }
    }

    public void setAndGroup(String cacheKey, Object cacheValue, Duration ttl, String setKey, String setValue) {

        try {

            // 1: store cache
            String json = objectMapper.writeValueAsString(cacheValue);
            redisTemplate.opsForValue().set(cacheKey, json, ttl);
            // 2: store index(SADD)
            if (setKey != null && setValue != null) {
                redisTemplate.opsForSet().add(setKey, setValue);
            }

        } catch (Exception e) {

            log.warn("Redis SET + SADD failed for cacheKey={}", cacheKey, e);
        }
    }

    public void delete(String key) {

        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("Redis DELETE failed for key={}", key, e);
        }
    }

    public void deleteByGroup(String setKey, Function<String, String> cacheKeyBuilder) {
        Set<String> setValue = redisTemplate.opsForSet().members(setKey);

        if (setValue == null || setValue.isEmpty()) {
            return;
        }

        for (String value : setValue) {
            redisTemplate.delete(cacheKeyBuilder.apply(value));
        }

        redisTemplate.delete(setKey);
    }

    public void removeSetValueFromGroup(String setKey, String setValue) {
        redisTemplate.opsForSet().remove(setKey, setValue);

        Long size = redisTemplate.opsForSet().size(setKey);

        if (size == null || size == 0) {
            redisTemplate.delete(setKey);
        }
    }

    public void deleteByPatternScan(String pattern) {
        ScanOptions options = ScanOptions.scanOptions()
                .match(pattern)
                .count(100)
                .build();

        try (Cursor<String> cursor = redisTemplate.scan(options)) {
            List<String> keysToDelete = new ArrayList<>();
            while (cursor.hasNext()) {
                keysToDelete.add(cursor.next());
            }

            if (!keysToDelete.isEmpty()) {
                redisTemplate.delete((keysToDelete));
            }
        } catch (Exception e) {
            log.warn("Redis DELETE failed for pattern={}", pattern, e);
        }
    }

}
