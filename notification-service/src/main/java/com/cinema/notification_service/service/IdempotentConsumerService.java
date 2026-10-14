package com.cinema.notification_service.service;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IdempotentConsumerService {

    private final StringRedisTemplate redisTemplate;
    private static final String PREFIX_PROCESSED_EVENT = "processed:event:";
    private static final Duration TTL = Duration.ofHours(24);

    /**
     * Kiểm tra và đánh dấu message đã được xử lý chưa.
     * 
     * @return true nếu message mới hoàn toàn (chưa xử lý), false nếu là message
     *         trùng.
     */
    public boolean isProcessed(String eventid) {
        String key = PREFIX_PROCESSED_EVENT + eventid;
        Boolean isNew = redisTemplate.opsForValue().setIfAbsent(key, "PROCESSED", TTL);

        // Nếu isNew == true tức là key chưa tồn tại trên Redis -> Cho phép xử lý
        // Nếu isNew == false tức là key đã tồn tại -> Message bị trùng, bỏ qua
        return Boolean.TRUE.equals(isNew);
    }
}
