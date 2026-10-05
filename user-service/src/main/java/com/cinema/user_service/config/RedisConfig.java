package com.cinema.user_service.config;

import java.time.Duration;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import com.cinema.user_service.domain.response.ResUserDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
@EnableCaching
public class RedisConfig {

        @Bean
        public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
                // 1. Khởi tạo ObjectMapper và cấu hình hỗ trợ Java 8 Date/Time (LocalDate,...)
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());

                // 2. Sử dụng Jackson2JsonRedisSerializer tường minh cho kiểu ResMovieDTO
                // (Giúp tránh hoàn toàn các lỗi về type info phức tạp của
                // GenericJackson2JsonRedisSerializer)
                Jackson2JsonRedisSerializer<ResUserDTO> serializer = new Jackson2JsonRedisSerializer<>(objectMapper,
                                ResUserDTO.class);

                // 3. Cấu hình Redis Cache
                RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(30))
                                .disableCachingNullValues()
                                // Thêm prefix riêng cho service này để tránh xung đột key trên Redis
                                .computePrefixWith(cacheName -> "user-service:" + cacheName + "::")
                                .serializeValuesWith(
                                                RedisSerializationContext.SerializationPair.fromSerializer(serializer));

                return RedisCacheManager.builder(connectionFactory)
                                .cacheDefaults(defaultConfig)
                                .build();
        }
}