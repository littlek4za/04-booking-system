package com.littlek4za.booking_system.services;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class RiskService {

    private final Map<String,Integer> attempCounter = new ConcurrentHashMap<>();

    public void recordAttempt(String email) {
        attempCounter.merge(email, 1, Integer::sum);
    }

    public boolean shouldRequireCaptcha(String email){
        return attempCounter.getOrDefault(email,0) >= 3;
    }

    public void reset(String email) {
        attempCounter.remove(email);
    }

}
