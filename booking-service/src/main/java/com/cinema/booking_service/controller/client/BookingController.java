package com.cinema.booking_service.controller.client;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Parameter;

import com.cinema.booking_service.domain.request.ReqBookingDTO;
import com.cinema.booking_service.domain.response.ResBookingDTO;
import com.cinema.booking_service.service.BookingService;
import com.cinema.booking_service.util.error.IdInvalidException;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<ResBookingDTO> createBooking(@Valid @RequestBody ReqBookingDTO reqBookingDTO,
            @Parameter(hidden = true) @RequestHeader("X-User-Email") String email)
            throws IdInvalidException {
        return ResponseEntity.ok(this.bookingService.handleCreateBooking(reqBookingDTO, email));
    }

    @PostMapping("/{id}/update-status")
    public ResponseEntity<Void> updateBookingStatus(
            @PathVariable("id") Long id,
            @RequestParam("status") String status) throws IdInvalidException {
        this.bookingService.updateBookingStatus(id, status);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<List<ResBookingDTO>> getMyBookings(
            @Parameter(hidden = true) @RequestHeader("X-User-Email") String userEmail) throws IdInvalidException {
        return ResponseEntity.ok(this.bookingService.getMyBookings(userEmail));
    }

    @GetMapping("/my-history")
    public ResponseEntity<List<ResBookingDTO>> getMyBookingHistory(
            @Parameter(hidden = true) @RequestHeader("X-User-Email") String userEmail) throws IdInvalidException {
        List<ResBookingDTO> history = bookingService.getBookingHistoryByUser(userEmail);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResBookingDTO> getBookingById(@PathVariable("id") Long id,
            @Parameter(hidden = true) @RequestHeader("X-User-Email") String userEmail)
            throws IdInvalidException {
        ResBookingDTO detail = bookingService.getBookingDetail(id, userEmail);
        return ResponseEntity.ok(detail);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ResBookingDTO> cancelBooking(@PathVariable("id") Long id,
            @Parameter(hidden = true) @RequestHeader("X-User-Email") String userEmail)
            throws IdInvalidException {
        ResBookingDTO cancelledBooking = bookingService.cancelBooking(id, userEmail);
        return ResponseEntity.ok(cancelledBooking);
    }
}
