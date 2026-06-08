package com.littlek4za.booking_system.services;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class RiskService {

    private static class AttemptInfo {
        private int count;
        private long lastUpdated;

        public AttemptInfo(int count, long lastUpdated) {
            this.count = count;
            this.lastUpdated = lastUpdated;
        }

        public int getCount() {
            return count;
        }

        public long getLastUpdated() {
            return lastUpdated;
        }
    }

    // Booking View
    private final Map<String, Integer> emailIpAttemptCounterForView = new ConcurrentHashMap<>();
    private final Map<String, Integer> ipAttemptCounterForView = new ConcurrentHashMap<>();

    // Booking Create
    private final Map<String, Integer> emailIpAttemptCounterForCreate = new ConcurrentHashMap<>();
    private final Map<String, Integer> ipAttemptCounterForCreate = new ConcurrentHashMap<>();
    private final Map<String, Integer> ipCreateSuccessCounter = new ConcurrentHashMap<>();

    // Auth Login
    private final Map<String, AttemptInfo> usernameIpAttemptCounterForLogin = new ConcurrentHashMap<>();
    private final Map<String, AttemptInfo> ipAttemptCounterForLogin = new ConcurrentHashMap<>();
    private final Map<String, AttemptInfo> usernameAttemptCounterForLogin = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> bannedIpsForLogin = new ConcurrentHashMap<>();

    private static final long BAN_DURATION_MS = 15 * 50 * 1000;
    private static final long WINDOW_MS = 5 * 60 * 1000;

    private String buildKey(String input, String ip) {
        return input + "|" + ip;
    }

    // Booking View
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

    // Booking Create

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

    // Auth Login

    public boolean isIpBannedForLogin(String ip) {
        Long banExpiry = bannedIpsForLogin.get(ip);
        if (banExpiry == null) {
            return false;
        }

        if (System.currentTimeMillis() > banExpiry) {
            bannedIpsForLogin.remove(ip); 
            return false;
        }

        return true; 
    }

    public void recordAttemptForLogin(String username, String ip) {
        long now = System.currentTimeMillis();
        String key = buildKey(username.toLowerCase(), ip);

        usernameIpAttemptCounterForLogin
                .compute(key, (k, attemptInfo) -> {
                    if (attemptInfo == null || now - attemptInfo.getLastUpdated() > WINDOW_MS) {
                        return new AttemptInfo(1, now);
                    }
                    return new AttemptInfo(attemptInfo.getCount() + 1, now);
                });
        AttemptInfo updatedIpAttemptCounter = ipAttemptCounterForLogin
                .compute(ip, (k, attemptInfo) -> {
                    if (attemptInfo == null || now - attemptInfo.getLastUpdated() > WINDOW_MS) {
                        return new AttemptInfo(1, now);
                    }
                    return new AttemptInfo(attemptInfo.getCount() + 1, now);
                });
        usernameAttemptCounterForLogin
                .compute(username.toLowerCase(), (k, attemptInfo) -> {
                    if (attemptInfo == null || now - attemptInfo.getLastUpdated() > WINDOW_MS) {
                        return new AttemptInfo(1, now);
                    }
                    return new AttemptInfo(attemptInfo.getCount() + 1, now);
                });
        
        if (updatedIpAttemptCounter != null && updatedIpAttemptCounter.getCount() >= 20) {
            bannedIpsForLogin.put(ip, now + BAN_DURATION_MS);
        }
    }

    public boolean shouldLimitLogin(String username, String ip) {
        long now = System.currentTimeMillis();
        String key = buildKey(username.toLowerCase(), ip);

        AttemptInfo usernameIpInfo = usernameIpAttemptCounterForLogin.get(key);

        AttemptInfo usernameInfo = usernameAttemptCounterForLogin.get(username.toLowerCase());

        int usernameIpAttempts = (usernameIpInfo == null || now - usernameIpInfo.getLastUpdated() > WINDOW_MS) ? 0
                : usernameIpInfo.getCount();

        int usernameAttempts = (usernameInfo == null || usernameInfo.getLastUpdated() > WINDOW_MS) ? 0
                : usernameInfo.getCount();

        return usernameIpAttempts >= 5 || usernameAttempts >= 10;
    }

    public void recordSuccessfulLogin(String username, String ip) {
        String key = buildKey(username.toLowerCase(), ip);

        usernameIpAttemptCounterForLogin.remove(key);

        usernameAttemptCounterForLogin.remove(username.toLowerCase());
    }

    // to clean up expired entries and stop memory leaks
    @Scheduled(fixedDelay = 60000) // Runs every 1 minute
    public void cleanExpiredEntries() {
        long now = System.currentTimeMillis();
        usernameIpAttemptCounterForLogin.values().removeIf(v -> now - v.getLastUpdated() > WINDOW_MS);
        usernameAttemptCounterForLogin.values().removeIf(v -> now - v.getLastUpdated() > WINDOW_MS);
    }

}
