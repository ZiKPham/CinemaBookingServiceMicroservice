package com.cinema.cinema_service.domain.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ResHoldSeatDTO {
    private Long showtimeId;
    private List<Long> seatIds; // Danh sách ghế (dùng khi response cho hành động hold hàng loạt)
    private Long singleSeatId; // Dùng khi gọi API check 1 ghế lẻ (như getSeatHolder)
    private String userEmail; // Email người đang giữ ghế
    private Instant expiresAt; // Thời điểm hết hạn giữ ghế
    private String message; // Thông báo phản hồi (nếu cần)

    // Constructor 1: Dành cho API /hold (Giữ ghế hàng loạt)
    public ResHoldSeatDTO(Long showtimeId, List<Long> seatIds, Instant expiresAt, String message) {
        this.showtimeId = showtimeId;
        this.seatIds = seatIds;
        this.expiresAt = expiresAt;
        this.message = message;
    }

    // Constructor 2: Dành cho API /lock/holder (Kiểm tra trạng thái 1 ghế cụ thể)
    public ResHoldSeatDTO(Long showtimeId, Long singleSeatId, String userEmail, Instant expiresAt) {
        this.showtimeId = showtimeId;
        this.singleSeatId = singleSeatId;
        this.userEmail = userEmail;
        this.expiresAt = expiresAt;
    }
}