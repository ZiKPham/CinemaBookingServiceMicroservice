package com.cinema.cinema_service.service;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.cinema.cinema_service.domain.response.ResHoldSeatDTO;

@Service
public class SeatLockService {

    private final StringRedisTemplate redisTemplate;

    public SeatLockService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Thời gian giữ ghế mặc định: 5 phút (300 giây)
    private static final long LOCK_TTL_SECONDS = 300;

    /**
     * Tạo khóa Redis cho ghế của suất chiếu
     * Key format: seat:lock:{showtimeId}:{seatId}
     * 
     * @return true nếu giữ ghế thành công, false nếu ghế đã bị người khác giữ
     */
    public boolean lockSeat(Long showtimeId, Long seatId, String userEmail) {
        String lockKey = generateLockKey(showtimeId, seatId);
        String userStrId = String.valueOf(userEmail);

        Boolean isLocked = redisTemplate.opsForValue().setIfAbsent(lockKey, userStrId, LOCK_TTL_SECONDS,
                TimeUnit.SECONDS);

        return Boolean.TRUE.equals(isLocked);
    }

    public boolean isSeatLocked(Long showtimeId, Long seatId) {
        String lockKey = generateLockKey(showtimeId, seatId);
        return Boolean.TRUE.equals(redisTemplate.hasKey(lockKey));
    }

    public String getSeatHolder(Long showtimeId, Long seatId) {
        String lockKey = generateLockKey(showtimeId, seatId);
        return redisTemplate.opsForValue().get(lockKey);
    }

    public void unlockSeat(Long showtimeId, Long seatId) {
        String lockKey = generateLockKey(showtimeId, seatId);
        redisTemplate.delete(lockKey);
    }

    public ResHoldSeatDTO getSeatLockDetail(Long showtimeId, Long seatId) {
        String lockKey = generateLockKey(showtimeId, seatId);
        String userEmail = redisTemplate.opsForValue().get(lockKey);

        // Tính thời gian hết hạn nếu ghế đang bị khóa (có thể lấy TTL thực tế từ Redis
        // hoặc tính 5 phút)
        Instant expiresAt = null;
        if (userEmail != null) {
            Long expireSeconds = redisTemplate.getExpire(lockKey, TimeUnit.SECONDS);
            expiresAt = Instant.now()
                    .plusSeconds(expireSeconds != null && expireSeconds > 0 ? expireSeconds : LOCK_TTL_SECONDS);
        }

        return new ResHoldSeatDTO(showtimeId, seatId, userEmail, expiresAt);
    }

    private String generateLockKey(Long showtimeId, Long seatId) {
        return "seat:lock:" + showtimeId + ":" + seatId;
    }
}
