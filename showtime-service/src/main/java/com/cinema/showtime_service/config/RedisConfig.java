package com.cinema.showtime_service.config;

import java.time.Duration;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

@Configuration
@EnableCaching
public class RedisConfig {

        @Bean
        public RedisCacheManager cacheManager(
                        RedisConnectionFactory connectionFactory,
                        ObjectMapper objectMapper) {

                // Tạo ObjectMapper riêng cho Redis
                ObjectMapper redisObjectMapper = objectMapper.copy();

                // Cho phép Jackson lưu thông tin class/type vào JSON
                BasicPolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                                .allowIfSubType("com.cinema.showtime_service.")
                                .allowIfSubType("java.util.")
                                .allowIfSubType("java.time.")
                                .allowIfSubType("java.lang.")
                                .build();

                redisObjectMapper.activateDefaultTyping(
                                ptv,
                                ObjectMapper.DefaultTyping.NON_FINAL,
                                JsonTypeInfo.As.PROPERTY);

                GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(
                                redisObjectMapper);

                RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(30))
                                .disableCachingNullValues()

                                .computePrefixWith(
                                                cacheName -> "showtime-service:" + cacheName + "::")

                                .serializeKeysWith(
                                                RedisSerializationContext.SerializationPair
                                                                .fromSerializer(new StringRedisSerializer()))

                                .serializeValuesWith(
                                                RedisSerializationContext.SerializationPair
                                                                .fromSerializer(serializer));

                return RedisCacheManager.builder(connectionFactory)
                                .cacheDefaults(defaultConfig)
                                .build();
        }
}