package com.cinema.booking_service.controller.admin;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cinema.booking_service.domain.request.BookingSearchCriteria;
import com.cinema.booking_service.domain.response.ResBookingDTO;
import com.cinema.booking_service.domain.response.ResultPaginationDTO;
import com.cinema.booking_service.service.BookingService;
import com.cinema.booking_service.util.error.IdInvalidException;

@RestController
@RequestMapping("/api/v1/admin/bookings")
public class AdminBookingController {

    private final BookingService bookingService;

    public AdminBookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public ResponseEntity<ResultPaginationDTO> getAllBookings(
            @ParameterObject BookingSearchCriteria criteria, @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(this.bookingService.fetchAllBookings(criteria, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResBookingDTO> getBookingDetailForAdmin(@PathVariable("id") Long id)
            throws IdInvalidException {
        // Xem chi tiết bất kỳ vé nào không phân biệt chủ sở hữu
        ResBookingDTO detail = this.bookingService.getBookingDetailAdmin(id);
        return ResponseEntity.ok(detail);
    }
}