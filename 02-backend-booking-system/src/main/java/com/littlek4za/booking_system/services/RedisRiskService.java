package com.littlek4za.booking_system.services;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.littlek4za.booking_system.models.CacheKeys;

@Service
public class RedisRiskService {

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisCacheService redisCacheService;

    public RedisRiskService(RedisTemplate<String, String> redisTemplate, RedisCacheService redisCacheService) {
        this.redisTemplate = redisTemplate;
        this.redisCacheService = redisCacheService;
    }

    private static final long LOGIN_BAN_DURATION_MINUTES = 15;
    private static final long LOGIN_CACHE_EXPIRE_MINUTES = 5;

    private long getCount(String key) {
        String value = redisTemplate.opsForValue().get(key);
        return value == null ? 0L : Long.parseLong(value);
    }

    // Booking View
    public void recordAttemptForBookingView(String email, String ip) {

        String emailIpAttemptCounterForBookingViewKey = CacheKeys.emailIpAttemptCounterForBookingView(email, ip);
        String ipAttemptCounterForBookingViewKey = CacheKeys.ipAttemptCounterForBookingView(ip);

        Long emailIpAttemptCount = redisTemplate.opsForValue().increment(emailIpAttemptCounterForBookingViewKey);
        Long ipAttemptCount = redisTemplate.opsForValue().increment(ipAttemptCounterForBookingViewKey);

        if (emailIpAttemptCount != null && emailIpAttemptCount == 1) {
            redisTemplate.expire(emailIpAttemptCounterForBookingViewKey, Duration.ofMinutes(5));
        }

        if (ipAttemptCount != null && ipAttemptCount == 1) {
            redisTemplate.expire(ipAttemptCounterForBookingViewKey, Duration.ofMinutes(5));
        }
    }

    public boolean shouldLimiBookingtView(String email, String ip) {

        String emailIpAttemptCounterForBookingViewKey = CacheKeys.emailIpAttemptCounterForBookingView(email, ip);
        String ipAttemptCounterForBookingViewKey = CacheKeys.ipAttemptCounterForBookingView(ip);

        Long emailIpAttemptCount = getCount(emailIpAttemptCounterForBookingViewKey);
        Long ipAttemptCount = getCount(ipAttemptCounterForBookingViewKey);

        return emailIpAttemptCount >= 3 || ipAttemptCount >= 10;
    }

    public void resetEmailIpForBookingView(String email, String ip) {

        String emailIpAttemptCounterForBookingViewKey = CacheKeys.emailIpAttemptCounterForBookingView(email, ip);

        redisTemplate.delete(emailIpAttemptCounterForBookingViewKey);
    }

    public void reduceIpPenaltyForBookingView(String ip) {

        String ipAttemptCounterForBookingViewKey = CacheKeys.ipAttemptCounterForBookingView(ip);

        Long ipAttemptCount = redisTemplate.opsForValue().decrement(ipAttemptCounterForBookingViewKey, 3);

        if (ipAttemptCount != null && ipAttemptCount < 0) {
            redisTemplate.opsForValue().set(ipAttemptCounterForBookingViewKey, "0");
        }
    }

    // Booking Create

    public void recordAttemptForBookingCreate(String email, String ip) {

        String emailIpAttemptCounterForBookingCreateKey = CacheKeys.emailIpAttemptCounterForBookingCreate(email, ip);
        String ipAttemptCounterForBookingCreateKey = CacheKeys.ipAttemptCounterForBookingCreate(ip);

        Long emailIpAttemptCount = redisTemplate.opsForValue().increment(emailIpAttemptCounterForBookingCreateKey);
        Long ipAttemptCount = redisTemplate.opsForValue().increment(ipAttemptCounterForBookingCreateKey);

        if (emailIpAttemptCount != null && emailIpAttemptCount == 1) {
            redisTemplate.expire(emailIpAttemptCounterForBookingCreateKey, Duration.ofMinutes(5));
        }

        if (ipAttemptCount != null && ipAttemptCount == 1) {
            redisTemplate.expire(ipAttemptCounterForBookingCreateKey, Duration.ofMinutes(5));
        }

    }

    public void recordBookingCreateSuccess(String ip) {

        String ipCreateSuccessCounterKey = CacheKeys.ipCreateSuccessCounter(ip);

        Long ipCreateSuccessCount = redisTemplate.opsForValue().increment(ipCreateSuccessCounterKey);

        if (ipCreateSuccessCount != null && ipCreateSuccessCount == 1) {
            redisTemplate.expire(ipCreateSuccessCounterKey, Duration.ofMinutes(15));
        }
    }

    public boolean shouldLimitBookingCreate(String email, String ip) {

        String emailIpAttemptCounterForBookingCreateKey = CacheKeys.emailIpAttemptCounterForBookingCreate(email, ip);
        String ipAttemptCounterForBookingCreateKey = CacheKeys.ipAttemptCounterForBookingCreate(ip);
        String ipCreateSuccessCounterKey = CacheKeys.ipCreateSuccessCounter(ip);

        Long emailIpAttemptCount = getCount(emailIpAttemptCounterForBookingCreateKey);
        Long ipAttemptCount = getCount(ipAttemptCounterForBookingCreateKey);
        Long ipCreateSuccessCount = getCount(ipCreateSuccessCounterKey);

        return emailIpAttemptCount >= 3 || ipAttemptCount >= 10 || ipCreateSuccessCount >= 20;
    }

    public void resetEmailIpForBookingCreate(String email, String ip) {

        String emailIpAttemptCounterForBookingCreateKey = CacheKeys.emailIpAttemptCounterForBookingCreate(email, ip);

        redisTemplate.delete(emailIpAttemptCounterForBookingCreateKey);

    }

    public void reduceIpPenaltyForBookingCreate(String ip) {
        String ipAttemptCounterForBookingCreateKey = CacheKeys.ipAttemptCounterForBookingCreate(ip);

        Long ipAttemptCount = redisTemplate.opsForValue().decrement(ipAttemptCounterForBookingCreateKey, 3);

        if (ipAttemptCount != null && ipAttemptCount < 0) {
            redisTemplate.opsForValue().set(ipAttemptCounterForBookingCreateKey, "0");
        }
    }

    // Auth Login

    public boolean isIpBannedForLogin(String ip) {

        Boolean banned = redisTemplate.hasKey(CacheKeys.ipBanForLogin(ip));

        return Boolean.TRUE.equals(banned);
    }

    public void recordAttemptForLogin(String username, String ip) {

        // username + ip
        String usernameIpAttemptCounterForLoginKey = CacheKeys.usernameIpAttemptCounterForLogin(ip, username);

        Long usernameIpAttemptCount = redisTemplate.opsForValue().increment(usernameIpAttemptCounterForLoginKey);
        if (usernameIpAttemptCount == 1) {
            redisTemplate.expire(usernameIpAttemptCounterForLoginKey, Duration.ofMinutes(LOGIN_CACHE_EXPIRE_MINUTES));
        }

        // username
        String usernameAttemptCounterForLoginKey = CacheKeys.usernameAttemptCounterForLogin(username);

        Long usernameAttemptCount = redisTemplate.opsForValue().increment(usernameAttemptCounterForLoginKey);
        if (usernameAttemptCount == 1) {
            redisTemplate.expire(usernameAttemptCounterForLoginKey, Duration.ofMinutes(LOGIN_CACHE_EXPIRE_MINUTES));
        }

        // ip
        String ipAttemptCounterForLoginKey = CacheKeys.ipAttemptCounterForLogin(ip);

        Long ipAttemptCount = redisTemplate.opsForValue().increment(ipAttemptCounterForLoginKey);
        if (ipAttemptCount == 1) {
            redisTemplate.expire(ipAttemptCounterForLoginKey, Duration.ofMinutes(LOGIN_CACHE_EXPIRE_MINUTES));
        }

        if (ipAttemptCount != null && ipAttemptCount >= 20) {
            redisTemplate.opsForValue().set(CacheKeys.ipBanForLogin(ip), "1",
                    Duration.ofMinutes(LOGIN_BAN_DURATION_MINUTES));
        }
    }

    public boolean shouldLimitLogin(String username, String ip) {

        if (isIpBannedForLogin(ip)) {
            return true;
        }

        String usernameIpAttemptCounterForLoginKey = CacheKeys.usernameIpAttemptCounterForLogin(ip, username);
        String usernameAttemptCounterForLoginKey = CacheKeys.usernameAttemptCounterForLogin(username);

        Long usernameIpAttempts = getCount(usernameIpAttemptCounterForLoginKey);
        Long usernameAttempts = getCount(usernameAttemptCounterForLoginKey);

        return usernameIpAttempts >= 5 || usernameAttempts >= 10;
    }

    public void recordSuccessfulLogin(String username, String ip) {

        String usernameIpAttemptCounterForLoginKey = CacheKeys.usernameIpAttemptCounterForLogin(ip, username);
        String usernameAttemptCounterForLoginKey = CacheKeys.usernameAttemptCounterForLogin(username);

        redisCacheService.delete(usernameIpAttemptCounterForLoginKey);
        redisCacheService.delete(usernameAttemptCounterForLoginKey);
    }

}
