package com.cinema.notification_service.consumer;

import com.cinema.notification_service.service.EmailService;
import com.cinema.notification_service.service.IdempotentConsumerService;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.cinema.notification_service.domain.dto.BookingEventDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationConsumer {

    private final EmailService emailService;
    private final IdempotentConsumerService idempotentService;

    @KafkaListener(topics = "booking-events-v1", groupId = "notification-group")
    public void consumeBookingEvent(BookingEventDTO event) {
        String uniqueId = String.valueOf(event.getBookingId());

        boolean isNewEvent = idempotentService.isProcessed(uniqueId);
        if (!isNewEvent) {
            log.warn("⚠️ [IDEMPOTENT] Phát hiện message trùng lặp cho Mã đặt vé: {}. Bỏ qua xử lý!", uniqueId);
            return;
        }

        log.info("==================================================");
        log.info("📩 [KAFKA CONSUMER] Nhận thành công Event đặt vé!");
        log.info("Mã đặt vé: {}", event.getBookingId());
        log.info("Email khách hàng: {}", event.getUserEmail());
        log.info("Phim: {} | Ghế: {} | Giá: {} VNĐ",
                event.getMovieTitle(), event.getSeatNumber(), event.getTotalPrice());

        try {
            // 2. Gọi service gửi email thực tế
            emailService.sendBookingConfirmationEmail(event.getUserEmail(), event.getMovieTitle(),
                    event.getSeatNumber(),
                    event.getTotalPrice(), event.getBookingId());
            log.info("✅ Gửi email xác nhận thành công cho đơn hàng: {}", event.getBookingId());
        } catch (Exception e) {
            log.error("❌ Lỗi khi gửi email cho đơn hàng {}: {}", event.getBookingId(), e.getMessage());
            // Ném exception để Kafka thực hiện cơ chế Retry / đẩy sang DLQ nếu cần
            throw e;
        }
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
