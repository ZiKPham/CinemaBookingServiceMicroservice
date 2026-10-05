package com.cinema.showtime_service.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "booking-service")
public interface BookingClient {
    // Lấy danh sách seatId đã được đặt của suất chiếu
    @GetMapping("/api/v1/bookings/booked-seats")
    List<Long> getBookedSeatIds(@RequestParam("showtimeId") long showtimeId);
}