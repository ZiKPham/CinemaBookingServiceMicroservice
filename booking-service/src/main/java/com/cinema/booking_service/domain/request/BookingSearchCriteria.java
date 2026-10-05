package com.cinema.booking_service.domain.request;

import java.time.Instant;
import com.cinema.booking_service.util.constant.BookingStatus;
import com.cinema.booking_service.util.constant.PaymentMethod;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingSearchCriteria {
    private BookingStatus status; // Trạng thái vé (PENDING, PAID, CANCELLED...)
    private PaymentMethod paymentMethod; // Phương thức thanh toán (CASH, MOMO, VNPAY...)
    private Long userId; // Lọc theo ID người dùng
    private Long showtimeId; // Lọc vé theo suất chiếu cụ thể
    private Instant fromDate; // Đặt vé từ ngày...
    private Instant toDate; // Đặt vé đến ngày...
}