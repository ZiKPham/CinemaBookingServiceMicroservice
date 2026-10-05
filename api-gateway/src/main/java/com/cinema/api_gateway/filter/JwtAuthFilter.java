package com.cinema.api_gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

        @Value("${jwt.base64-secret}")
        private String base64Secret;

        @Override
        public Mono<Void> filter(
                        ServerWebExchange exchange,
                        GatewayFilterChain chain) {

                ServerHttpRequest request = exchange.getRequest();
                String path = request.getURI().getPath();

                // =========================================================
                // 1. PUBLIC ENDPOINTS
                // =========================================================

                // Cho phép các endpoint public đi qua mà không cần JWT
                if (path.contains("/auth/")
                                || path.contains("/v3/api-docs")
                                || path.contains("/swagger-ui")
                                || path.contains("/webjars/")
                                || path.contains("/api/v1/payments/vn-pay-callback")
                                || path.contains("/api/v1/payments/momo-ipn")
                                || path.contains("/api/v1/payments/momo/mock-payment-page")
                                || path.equals("/")) {

                        return chain.filter(exchange);
                }

                // =========================================================
                // 2. KIỂM TRA AUTHORIZATION HEADER
                // =========================================================

                String authHeader = request
                                .getHeaders()
                                .getFirst(HttpHeaders.AUTHORIZATION);

                // Không có Authorization header
                if (authHeader == null || authHeader.trim().isEmpty()) {

                        return onError(
                                        exchange,
                                        "Missing Authorization Header",
                                        HttpStatus.UNAUTHORIZED);
                }

                // Authorization phải có dạng:
                // Bearer <token>
                if (!authHeader.toLowerCase().startsWith("bearer ")) {

                        return onError(
                                        exchange,
                                        "Invalid Authorization Header Format",
                                        HttpStatus.UNAUTHORIZED);
                }

                // Lấy JWT bỏ phần "Bearer "
                String token = authHeader.substring(7).trim();

                // Token rỗng
                if (token.isEmpty()) {

                        return onError(
                                        exchange,
                                        "JWT Token is Empty",
                                        HttpStatus.UNAUTHORIZED);
                }

                try {

                        // =====================================================
                        // 3. GIẢI MÃ BASE64 SECRET
                        // =====================================================

                        byte[] keyBytes = Base64
                                        .getDecoder()
                                        .decode(base64Secret);

                        SecretKey key = Keys.hmacShaKeyFor(keyBytes);

                        // =====================================================
                        // 4. VERIFY JWT
                        // =====================================================

                        Claims claims = Jwts
                                        .parser()
                                        .verifyWith(key)
                                        .build()
                                        .parseSignedClaims(token)
                                        .getPayload();

                        // =====================================================
                        // 5. LẤY USER ID / SUBJECT
                        // =====================================================

                        // SecurityUtil của user đang lưu:
                        //
                        // .subject(email)
                        //
                        // Vì vậy ở đây userId thực tế đang là email.
                        String userId = claims.getSubject();

                        if (userId == null || userId.isBlank()) {

                                return onError(
                                                exchange,
                                                "JWT Subject is Missing",
                                                HttpStatus.UNAUTHORIZED);
                        }

                        // =====================================================
                        // 6. TẠO REQUEST MỚI
                        // =====================================================
                        //
                        // Không dùng:
                        //
                        // request.mutate()
                        // .header(...)
                        //
                        // vì trong trường hợp của Gateway/WebFlux,
                        // headers có thể là ReadOnlyHttpHeaders.
                        //
                        // Dùng ServerHttpRequestDecorator để tạo headers mới.
                        //

                        ServerHttpRequest modifiedRequest = new ServerHttpRequestDecorator(request) {

                                @Override
                                public HttpHeaders getHeaders() {

                                        HttpHeaders headers = new HttpHeaders();

                                        // Copy toàn bộ header cũ
                                        headers.putAll(super.getHeaders());

                                        // Thêm user information
                                        headers.set("X-User-Id", userId);

                                        // Authorization đã tồn tại rồi nên
                                        // không cần set lại.
                                        //
                                        // Nếu muốn giữ nguyên Authorization,
                                        // putAll() phía trên đã giữ nó.

                                        return headers;
                                }
                        };

                        // =====================================================
                        // 7. GỬI REQUEST XUỐNG SERVICE
                        // =====================================================

                        ServerWebExchange modifiedExchange = exchange
                                        .mutate()
                                        .request(modifiedRequest)
                                        .build();

                        return chain.filter(modifiedExchange);

                } catch (Exception e) {

                        // Debug
                        e.printStackTrace();

                        return onError(
                                        exchange,
                                        "Invalid or Expired JWT Token",
                                        HttpStatus.UNAUTHORIZED);
                }
        }

        // =============================================================
        // ERROR RESPONSE
        // =============================================================

        private Mono<Void> onError(
                        ServerWebExchange exchange,
                        String errMessage,
                        HttpStatus httpStatus) {

                ServerHttpResponse response = exchange.getResponse();

                response.setStatusCode(httpStatus);

                response.getHeaders()
                                .setContentType(MediaType.APPLICATION_JSON);

                String errorJson = "{\"statusCode\":" + httpStatus.value()
                                + ",\"message\":\"" + errMessage
                                + "\",\"data\":null"
                                + ",\"error\":\"Unauthorized\"}";

                DataBuffer buffer = response
                                .bufferFactory()
                                .wrap(errorJson.getBytes(StandardCharsets.UTF_8));

                return response.writeWith(
                                Mono.just(buffer));
        }

        // =============================================================
        // FILTER ORDER
        // =============================================================

        @Override
        public int getOrder() {

                // Chạy sớm trong Gateway filter chain
                return -1;
        }
}