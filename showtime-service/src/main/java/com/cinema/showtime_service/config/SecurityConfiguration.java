package com.cinema.showtime_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Tắt CSRF vì microservice thường dùng REST API thuần (stateless)
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // Cấu hình phân quyền cho các endpoint
                .authorizeHttpRequests(authz -> authz
                        // Cho phép tự do truy cập xem phim, lịch chiếu (tùy theo đường dẫn API của bạn)
                        .requestMatchers("/api/v1/movies/**", "/api/v1/showtimes/**", "/api/v1/rooms/**")
                        .permitAll()
                        // Các request khác (như đặt vé, giữ ghế cần qua Gateway truyền header xuống)
                        // tạm thời cho phép hoặc cấu hình tùy ý
                        .anyRequest().permitAll() // Hoặc .authenticated() nếu muốn bắt buộc có token/header từ gateway
                )

                // Cấu hình Stateless (không lưu session trên server vì dùng token/gateway)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}