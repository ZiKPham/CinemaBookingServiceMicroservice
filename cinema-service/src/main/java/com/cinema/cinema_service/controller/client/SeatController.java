package com.cinema.cinema_service.controller.client;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cinema.cinema_service.domain.request.ReqHoldSeatDTO;
import com.cinema.cinema_service.domain.response.ResHoldSeatDTO;
import com.cinema.cinema_service.service.SeatLockService;
import com.cinema.cinema_service.service.SeatService;
import com.cinema.cinema_service.util.error.IdInvalidException;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@RestController
@RequestMapping("/api/v1/seats")
public class SeatController {

    private final SeatLockService seatLockService;
    private final SeatService seatService;

    public SeatController(SeatLockService seatLockService, SeatService seatService) {
        this.seatLockService = seatLockService;
        this.seatService = seatService;
    }

    @PostMapping("/hold")
    public ResponseEntity<?> holdSeats(@Valid @RequestBody ReqHoldSeatDTO request,
            @Parameter(hidden = true) @RequestHeader(value = "X-User-Email") String userEmail)
            throws IdInvalidException {
        ResHoldSeatDTO res = seatService.handleHoldSeats(request, userEmail);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/check-lock")
    public ResponseEntity<Boolean> checkSeatLock(@RequestParam("showtimeId") Long showtimeId,
            @RequestParam("seatId") Long seatId) {
        boolean isLocked = seatLockService.isSeatLocked(showtimeId, seatId);
        return ResponseEntity.ok(isLocked);
    }

    @GetMapping("/lock/holder")
    public ResponseEntity<ResHoldSeatDTO> getSeatHolder(
            @RequestParam("showtimeId") Long showtimeId,
            @RequestParam("seatId") Long seatId) {

        ResHoldSeatDTO seatDetail = seatLockService.getSeatLockDetail(showtimeId, seatId);
        return ResponseEntity.ok(seatDetail);
    }

    @DeleteMapping("/lock")
    public ResponseEntity<Void> unlockSeat(
            @RequestParam("showtimeId") Long showtimeId,
            @RequestParam("seatId") Long seatId) {
        seatLockService.unlockSeat(showtimeId, seatId);
        return ResponseEntity.ok().build();
    }
}
