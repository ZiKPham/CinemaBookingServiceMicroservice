package com.cinema.pay_service.service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;

@Component
public class PaymentRateLimiterService {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    public void checkRateLimit(String key) {
        String normalizedKey = (key == null || key.trim().isEmpty()) ? "anonymous" : key.trim().toLowerCase();
        Bucket bucket = cache.computeIfAbsent(normalizedKey, this::newBucket);

        if (!bucket.tryConsume(1)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Bạn đang thực hiện các giao dịch thanh toán quá nhanh. Vui lòng thử lại sau vài phút.");
        }
    }

    private Bucket newBucket(String userId) {
        Bandwidth limit = Bandwidth.classic(3, Refill.greedy(3, Duration.ofSeconds(60)));
        return Bucket.builder().addLimit(limit).build();
    }
}
