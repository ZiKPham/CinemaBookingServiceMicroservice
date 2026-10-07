package com.cinema.booking_service.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.cinema.booking_service.domain.response.RestResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Duration;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final int MAX_REQUESTS = 5;
    private static final long TIME_WINDOW_SECONDS = 60;

    public RateLimitInterceptor(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        String clientKey = request.getHeader("X-User-Email");

        if (clientKey == null || clientKey.trim().isEmpty()) {
            clientKey = request.getRemoteAddr();
        }

        String redisKey = "rate-limit:" + request.getRequestURI() + ":" + clientKey;

        Long currentRequests = redisTemplate.opsForValue().increment(redisKey);

        if (currentRequests != null && currentRequests == 1) {
            redisTemplate.expire(
                    redisKey,
                    Duration.ofSeconds(TIME_WINDOW_SECONDS));
        }

        if (currentRequests != null && currentRequests > MAX_REQUESTS) {

            RestResponse<Object> res = new RestResponse<>();
            res.setStatusCode(HttpStatus.TOO_MANY_REQUESTS.value());
            res.setError("Too Many Requests");
            res.setMessage(
                    "Bạn thao tác quá nhanh, vui lòng thử lại sau ít phút!");

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json;charset=UTF-8");

            response.getWriter().write(
                    objectMapper.writeValueAsString(res));

            return false;
        }

        return true;
    }
}
