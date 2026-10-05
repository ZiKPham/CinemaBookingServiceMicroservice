package com.cinema.booking_service.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignClientConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                        .getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();

                    // Tự động lấy header X-User-Email từ request gốc (do API Gateway hoặc Filter
                    // truyền xuống)
                    String userEmail = request.getHeader("X-User-Email");
                    if (userEmail != null && !userEmail.isBlank()) {
                        // Đính kèm vào request của Feign gọi sang service khác
                        template.header("X-User-Email", userEmail);
                    }

                    // Nếu bạn muốn truyền tiếp cả Authorization Token, có thể làm tương tự:
                    String authHeader = request.getHeader("Authorization");
                    if (authHeader != null && !authHeader.isBlank()) {
                        template.header("Authorization", authHeader);
                    }
                }
            }
        };
    }
}