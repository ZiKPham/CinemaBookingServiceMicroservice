package com.cinema.booking_service.controller.client;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cinema.booking_service.domain.dto.BookingEventDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/bookings/test-kafka")
@RequiredArgsConstructor
public class TestKafkaController {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @GetMapping("/send-duplicate")
    public String sendDuplicateEvents() {
        // Tạo 1 event cố định với bookingId = 9999
        BookingEventDTO event = new BookingEventDTO();
        event.setBookingId("51");
        event.setUserEmail("pp09380461@gmail.com");
        event.setMovieTitle("Phim Test Idempotent");
        event.setSeatNumber("A1");
        event.setTotalPrice(100000.0);

        // Bắn lần 1
        kafkaTemplate.send("booking-events-v1", event);

        // Bắn lần 2 (Y hệt lần 1, giả lập lỗi mạng gửi trùng message)
        kafkaTemplate.send("booking-events-v1", event);

        return "Đã bắn 2 message trùng lặp có bookingId = 9999 lên Kafka!";
    }
}
