package com.cinema.notification_service.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendBookingConfirmationEmail(String toEmail, String movieTitle, String seatNumber, double totalPrice,
            String bookingId) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("🎟️ Xác nhận đặt vé thành công - Mã đơn: #" + bookingId);
            message.setText(
                    "Cảm ơn bạn đã đặt vé tại hệ thống Cinema!\n\n" +
                            "Thông tin vé của bạn:\n" +
                            "- Mã đơn hàng: " + bookingId + "\n" +
                            "- Tên phim: " + movieTitle + "\n" +
                            "- Ghế đã chọn: " + seatNumber + "\n" +
                            "- Tổng tiền: " + totalPrice + " VNĐ\n\n" +
                            "Chúc bạn xem phim vui vẻ!");

            mailSender.send(message);
            log.info("📧 [REAL EMAIL] Đã gửi email xác nhận thành công tới: {}", toEmail);
        } catch (Exception e) {
            log.error("❌ [REAL EMAIL] Gửi email thất bại tới {}: {}", toEmail, e.getMessage());
            // Ném exception ra để Kafka Consumer có cơ chế bắt lỗi/retry nếu cần
            throw new RuntimeException("Không thể gửi email", e);
        }
    }
}
