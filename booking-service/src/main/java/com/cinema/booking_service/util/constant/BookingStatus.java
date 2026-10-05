package com.cinema.booking_service.util.constant;

public enum BookingStatus {
    PENDING, // Chờ thanh toán / Đang giữ ghế
    PAID, // Đã thanh toán thành công
    CANCELLED, // Đã hủy
    EXPIRED // Hết hạn giữ ghế (quá thời gian thanh toán)
}