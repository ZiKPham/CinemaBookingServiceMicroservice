package com.cinema.booking_service.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.cinema.booking_service.domain.Booking;
import com.cinema.booking_service.domain.dto.PaymentEventDTO;
import com.cinema.booking_service.repository.BookingRepository;
import com.cinema.booking_service.service.BookingService;
import com.cinema.booking_service.util.constant.BookingStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingPaymentConsumer {

    private final BookingService bookingService;
    private final BookingRepository bookingRepository;

    @KafkaListener(topics = "payment-events-v1", groupId = "booking-payment-group")
    public void consumePaymentEvent(PaymentEventDTO event) {
        log.info("📩 [BOOKING-SERVICE] Nhận event thanh toán cho Booking ID: {} | Trạng thái: {}",
                event.getBookingId(), event.getStatus());

        try {
            Booking booking = bookingRepository.findById(event.getBookingId()).orElse(null);

            if (booking == null) {
                log.warn("⚠️ Không tìm thấy đơn hàng với ID: {} để cập nhật trạng thái!", event.getBookingId());
                return;
            }

            if (BookingStatus.PAID.name().equals(booking.getStatus())
                    || BookingStatus.CANCELLED.name().equals(booking.getStatus())) {
                log.info("ℹ️ Đơn hàng ID: {} đã ở trạng thái [{}] trước đó, bỏ qua event trùng lặp.",
                        booking.getId(), booking.getStatus());
                return;
            }

            if ("SUCCESS".equals(event.getStatus().name())) {
                // Cập nhật trạng thái đơn hàng thành PAID
                bookingService.updateBookingStatus(event.getBookingId(), "PAID");
                log.info("✅ Đã cập nhật đơn hàng ID: {} sang trạng thái PAID thành công!", event.getBookingId());
            } else if ("FAILED".equals(event.getStatus().name())) {
                // Cập nhật trạng thái đơn hàng thành CANCELLED (hoặc FAILED)
                bookingService.updateBookingStatus(event.getBookingId(), "CANCELLED");
                log.info("❌ Đã hủy đơn hàng ID: {} do thanh toán thất bại!", event.getBookingId());
            }
        } catch (Exception e) {
            log.error("❌ Lỗi khi xử lý cập nhật trạng thái đơn hàng từ Kafka event: {}", e.getMessage());
        }

    }
}
