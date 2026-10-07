package com.cinema.movie_service.service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;

@Service
public class MovieRateLimiterService {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    public void checkRateLimit(String key) {
        String normalizedKey = (key == null || key.trim().isEmpty()) ? "anonymous" : key.trim().toLowerCase();
        Bucket bucket = cache.computeIfAbsent(normalizedKey, this::newBucket);

        if (!bucket.tryConsume(1)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Bạn đang gửi quá nhiều yêu cầu tra cứu phim. Vui lòng thử lại sau giây lát.");
        }
    }

    private Bucket newBucket(String key) {
        // Cấu hình: Tối đa 15 request trong vòng 10 giây
        Bandwidth limit = Bandwidth.classic(10, Refill.greedy(10, Duration.ofSeconds(10)));
        return Bucket.builder().addLimit(limit).build();
    }
}