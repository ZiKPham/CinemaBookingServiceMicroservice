package com.cinema.booking_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic bookingEventsTopic() {
        return TopicBuilder.name("booking-events-v1")
                .partitions(3) // Chia thành 3 partitions để chia tải xử lý song song
                .replicas(2) // Nhân bản dữ liệu qua cả kafka1 và kafka2 (Fault-tolerant)
                .build();
    }
}
