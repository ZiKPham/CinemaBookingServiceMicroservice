package com.cinema.api_gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String USER_EMAIL_HEADER = "X-User-Email";
    private static final String AUTH_PATH = "/api/v1/auth";
    private static final String API_DOCS_PATH = "/v3/api-docs";
    private static final String SWAGGER_UI_PATH = "/swagger-ui";
    private static final String WEBJARS_PATH = "/webjars";
    private final JwtParser jwtParser;

    public JwtAuthFilter(@Value("${jwt.base64-secret}") String base64Secret) {
        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(base64Secret);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("jwt.base64-secret must be valid Base64", exception);
        }
        SecretKey signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.jwtParser = Jwts.parser().verifyWith(signingKey).build();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        var request = exchange.getRequest();
        String path = request.getURI().getPath();
        if (request.getMethod() == HttpMethod.OPTIONS || isPublicPath(path)) {
            return chain.filter(exchange);
        }
        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || authorization.isBlank()) {
            return unauthorized(exchange, "Missing Authorization header");
        }
        if (!authorization.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            return unauthorized(exchange, "Authorization header must use Bearer scheme");
        }
        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            return unauthorized(exchange, "Bearer token is empty");
        }
        final String subject;
        try {
            Claims claims = jwtParser.parseSignedClaims(token).getPayload();
            subject = claims.getSubject();
        } catch (JwtException | IllegalArgumentException exception) {
            log.warn("Rejected request to {} because its JWT is invalid or expired", path);
            return unauthorized(exchange, "Invalid or expired JWT token");
        }
        if (subject == null || subject.isBlank()) {
            return unauthorized(exchange, "JWT subject is missing");
        }
        ServerHttpRequest authenticatedRequest = withUserEmail(request, subject);
        return chain.filter(exchange.mutate().request(authenticatedRequest).build());
    }

    private ServerHttpRequest withUserEmail(ServerHttpRequest request, String email) {
        HttpHeaders headers = new HttpHeaders();
        headers.putAll(request.getHeaders());
        // The user-service JWT subject is the authenticated user's email.
        // set() replaces any client-supplied value before forwarding the request.
        headers.set(USER_EMAIL_HEADER, email);

        HttpHeaders readOnlyHeaders = HttpHeaders.readOnlyHttpHeaders(headers);
        return new ServerHttpRequestDecorator(request) {
            @Override
            public HttpHeaders getHeaders() {
                return readOnlyHeaders;
            }
        };
    }

    private boolean isPublicPath(String path) {
        return isPathOrSubpath(path, AUTH_PATH)
                || isPathOrSubpath(path, API_DOCS_PATH)
                || isPathOrSubpath(path, SWAGGER_UI_PATH)
                || isPathOrSubpath(path, WEBJARS_PATH)
                || path.equals("/")
                || path.equals("/api/v1/payments/vn-pay-callback")
                || path.equals("/api/v1/payments/momo-ipn")
                || path.equals("/api/v1/payments/momo/mock-payment-page");
    }

    private boolean isPathOrSubpath(String path, String prefix) {
        return path.equals(prefix) || path.startsWith(prefix + "/");
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        var response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"statusCode\":401,\"message\":\"" + message
                + "\",\"data\":null,\"error\":\"Unauthorized\"}";
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
