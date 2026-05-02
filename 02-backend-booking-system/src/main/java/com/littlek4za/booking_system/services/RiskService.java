package com.littlek4za.booking_system.services;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class RiskService {

    private final Map<String, Integer> emailIpAttemptCounterForView = new ConcurrentHashMap<>();
    private final Map<String, Integer> ipAttemptCounterForView = new ConcurrentHashMap<>();
    private final Map<String, Integer> emailIpAttemptCounterForCreate = new ConcurrentHashMap<>();
    private final Map<String, Integer> ipAttemptCounterForCreate = new ConcurrentHashMap<>();
    private final Map<String, Integer> ipCreateSuccessCounter = new ConcurrentHashMap<>();

    private String buildKey(String email, String ip) {
        return email + "|" + ip;
    }

    // bookingview
    public void recordAttemptForView(String email, String ip) {
        String key = buildKey(email, ip);
        emailIpAttemptCounterForView.merge(key, 1, Integer::sum);
        ipAttemptCounterForView.merge(ip, 1, Integer::sum);
    }

    public boolean shouldLimitView(String email, String ip) {
        String key = buildKey(email, ip);
        int emailIpAttempts = emailIpAttemptCounterForView.getOrDefault(key, 0);
        int ipAttempts = ipAttemptCounterForView.getOrDefault(key, 0);
        return emailIpAttempts >= 3 || ipAttempts >= 10;
    }

    public void resetEmailIpForView(String email, String ip) {
        emailIpAttemptCounterForView.remove(buildKey(email, ip));
    }

    public void reduceIpPenaltyForView(String ip) {
        ipAttemptCounterForView.computeIfPresent(ip, (k, v) -> Math.max(0, v - 3));
    }

    // bookingcreate

    public void recordAttemptForCreate(String email, String ip) {
        String key = buildKey(email, ip);
        emailIpAttemptCounterForCreate.merge(key, 1, Integer::sum);
        ipAttemptCounterForCreate.merge(ip, 1, Integer::sum);
    }

    public void recordCreateSuccess(String ip) {
        ipCreateSuccessCounter.merge(ip, 1, Integer::sum);
    }

    public boolean shouldLimitCreate(String email, String ip) {
        String key = buildKey(email, ip);
        int emailIpAttempts = emailIpAttemptCounterForCreate.getOrDefault(key, 0);
        int ipAttempts = ipAttemptCounterForCreate.getOrDefault(ip, 0);
        int ipSuccess = ipCreateSuccessCounter.getOrDefault(ip, 0);
        return emailIpAttempts >= 3 || ipAttempts >= 10 || ipSuccess >= 1;
    }

    public void resetEmailIpForCreate(String email, String ip) {
        emailIpAttemptCounterForCreate.remove(buildKey(email, ip));
    }

    public void reduceIpPenaltyForCreate(String ip) {
        ipAttemptCounterForCreate.computeIfPresent(ip, (k, v) -> Math.max(0, v - 3));
    }

}
