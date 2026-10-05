package com.cinema.user_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfiguration {

        private final CustomAccessDeniedHandler customAccessDeniedHandler;
        private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

        public SecurityConfiguration(CustomAccessDeniedHandler customAccessDeniedHandler,
                        CustomAuthenticationEntryPoint customAuthenticationEntryPoint) {
                this.customAccessDeniedHandler = customAccessDeniedHandler;
                this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
                return authConfig.getAuthenticationManager();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(c -> c.disable())
                                .cors(Customizer.withDefaults())
                                .httpBasic(httpBasic -> httpBasic.disable()) // <--- TẮT BẢNG ĐĂNG NHẬP HTTP BASIC MẶC
                                                                             // ĐỊNH
                                .formLogin(formLogin -> formLogin.disable()) // <--- TẮT LUÔN FORM LOGIN NẾU CÓ
                                .authorizeHttpRequests(authz -> authz
                                                // Cho phép auth và tài liệu Swagger không cần token
                                                .requestMatchers("/api/v1/auth/login", "/api/v1/auth/register",
                                                                "/api/v1/users/by-email")
                                                .permitAll()
                                                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**",
                                                                "/swagger-ui.html", "/user-service/v3/api-docs/**")
                                                .permitAll()

                                                // Phân quyền Admin
                                                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                                                // Các request còn lại vẫn yêu cầu xác thực
                                                .anyRequest().authenticated())

                                .exceptionHandling(exceptions -> exceptions
                                                .accessDeniedHandler(customAccessDeniedHandler))
                                .oauth2ResourceServer(oauth2 -> oauth2
                                                .jwt(jwt -> jwt.jwtAuthenticationConverter(
                                                                jwtAuthenticationConverter())) // Gắn converter chuẩn
                                                                                               // của bạn vào đây luôn
                                                                                               // cho chắc chắn
                                                .authenticationEntryPoint(customAuthenticationEntryPoint))
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

                return http.build();
        }

        @Bean
        public JwtAuthenticationConverter jwtAuthenticationConverter() {
                JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
                // Thay đổi thành tên claim thực tế chứa role trong token của bạn (ví dụ:
                // "role", "roles", hoặc "permission")
                grantedAuthoritiesConverter.setAuthoritiesClaimName("role");

                // Nếu trong token giá trị đã có sẵn chữ "ROLE_" rồi thì có thể để trống prefix,
                // ngược lại nếu chỉ là "ADMIN" thì giữ nguyên "ROLE_"
                grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

                JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
                jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
                return jwtAuthenticationConverter;
        }
}