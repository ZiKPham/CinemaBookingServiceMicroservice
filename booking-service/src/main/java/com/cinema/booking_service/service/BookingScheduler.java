package com.cinema.booking_service.service;

import com.cinema.booking_service.domain.Booking;
import com.cinema.booking_service.repository.BookingRepository;
import com.cinema.booking_service.util.constant.BookingStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant; // Thay đổi từ LocalDateTime sang Instant
import java.time.temporal.ChronoUnit; // Thêm import này để trừ thời gian
import java.util.List;

@Component
public class BookingScheduler {

    private final BookingRepository bookingRepository;

    public BookingScheduler(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    // Chạy định kỳ mỗi 60 giây (1 phút) một lần
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void autoCancelExpiredBookings() {
        // Sử dụng Instant và trừ đi 15 phút
        Instant expireTime = Instant.now().minus(15, ChronoUnit.MINUTES);

        // Lấy danh sách đơn hàng PENDING quá hạn
        List<Booking> expiredBookings = bookingRepository.findByStatusAndCreatedAtBefore(BookingStatus.PENDING,
                expireTime);

        if (!expiredBookings.isEmpty()) {
            for (Booking booking : expiredBookings) {
                booking.setStatus(BookingStatus.CANCELLED);
            }
            bookingRepository.saveAll(expiredBookings);
            System.out.println("=== Đã tự động hủy " + expiredBookings.size() + " đơn hàng quá hạn thanh toán ===");
        }
    }
}