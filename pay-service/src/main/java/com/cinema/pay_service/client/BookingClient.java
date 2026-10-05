package com.cinema.pay_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.cinema.pay_service.domain.response.ResBookingDTO;
import com.cinema.pay_service.domain.response.RestResponse;

@FeignClient(name = "booking-service")
public interface BookingClient {

    @GetMapping("/api/v1/bookings/{id}")
    RestResponse<ResBookingDTO> getBookingById(
            @PathVariable("id") Long bookingId,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail);

    @PostMapping("/api/v1/bookings/{id}/update-status")
    void updateBookingStatus(@PathVariable("id") Long bookingId, @RequestParam("status") String status);
}