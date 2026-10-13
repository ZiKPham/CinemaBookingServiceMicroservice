package com.cinema.notification_service.consumer;

import com.cinema.notification_service.service.EmailService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.cinema.notification_service.domain.dto.BookingEventDTO;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NotificationConsumer {

    private final EmailService emailService;

    NotificationConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    @KafkaListener(topics = "booking-events-v1", groupId = "notification-group")
    public void consumeBookingEvent(BookingEventDTO event) {
        log.info("==================================================");
        log.info("📩 [KAFKA CONSUMER] Nhận thành công Event đặt vé!");
        log.info("Mã đặt vé: {}", event.getBookingId());
        log.info("Email khách hàng: {}", event.getUserEmail());
        log.info("Phim: {} | Ghế: {} | Giá: {} VNĐ",
                event.getMovieTitle(), event.getSeatNumber(), event.getTotalPrice());

        // Gọi service gửi email thực tế
        emailService.sendBookingConfirmationEmail(event.getUserEmail(), event.getMovieTitle(), event.getSeatNumber(),
                event.getTotalPrice(), event.getBookingId());
        log.info("==================================================");
    }

    @KafkaListener(topics = "booking-events-v1.DTL", groupId = "notification-dlq-group")
    public void consumeDLQ(BookingEventDTO event) {
        log.error("==================================================");
        log.error("⚠️ [DLQ CONSUMER] Phát hiện message lỗi không thể xử lý sau nhiều lần retry!");
        log.error("Mã đơn hàng bị lỗi: {}", event.getBookingId());
        log.error("Khách hàng: {}", event.getUserEmail());
        // Thêm logic lưu database, gửi cảnh báo cấp bách cho admin tại đây nếu cần
        log.error("==================================================");
    }
}
