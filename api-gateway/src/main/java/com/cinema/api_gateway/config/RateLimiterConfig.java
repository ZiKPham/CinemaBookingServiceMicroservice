package com.cinema.api_gateway.config;

import java.util.Base64;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

@Configuration
public class RateLimiterConfig {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public KeyResolver userJwtKeyResolver() {
        return exchange -> {
            String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                // TODO: Giải mã JWT token (bằng JJWT hoặc Nimbus) để lấy ra UserId hoặc
                // Username thực tế.
                // Ví dụ tạm thời: Trả về chính chuỗi token hoặc parse payload lấy sub/userId
                // làm key.
                String userId = extractUserIdFromToken(token);
                return Mono.just(userId);
            }

            // Nếu không có token, fallback về IP hoặc định danh khách vãng lai
            String clientIp = exchange.getRequest().getRemoteAddress() != null
                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                    : "anonymous";

            return Mono.just("ip_" + clientIp);
        };
    }

    private String extractUserIdFromToken(String token) {
        try {
            // JWT gồm 3 phần: header.payload.signature phân tách bằng dấu chấm (.)
            String[] parts = token.split("\\.");
            if (parts.length >= 2) {
                // Giải mã phần payload (phần thứ 2)
                String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
                JsonNode node = objectMapper.readTree(payloadJson);

                // Ưu tiên lấy "userId" hoặc "sub" (subject) tùy theo cách bạn sinh token ở
                // auth-service
                if (node.has("userId")) {
                    return node.get("userId").asText();
                } else if (node.has("sub")) {
                    return node.get("sub").asText();
                }
            }
        } catch (Exception e) {
            // Nếu token lỗi hoặc không parse được, fallback về hash của token hoặc chuỗi
            // mặc định
            System.err.println("Không thể parse JWT payload cho rate limit: " + e.getMessage());
        }

        // Fallback an toàn nếu không tìm thấy field
        return "user_unknown";
    }
}
