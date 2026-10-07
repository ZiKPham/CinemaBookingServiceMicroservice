package com.cinema.user_service.service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;

@Component
public class LoginRateLimiterService {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String username) {
        String normalizedKey = username == null ? "" : username.trim().toLowerCase();
        return cache.computeIfAbsent(normalizedKey, this::newBucket);
    }

    private Bucket newBucket(String username) {
        Bandwidth limit = Bandwidth.classic(5, Refill.greedy(5, Duration.ofSeconds(60)));
        return Bucket.builder().addLimit(limit).build();
    }
}
