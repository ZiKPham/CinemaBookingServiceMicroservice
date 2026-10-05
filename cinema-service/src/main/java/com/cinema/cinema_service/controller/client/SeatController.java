package com.cinema.cinema_service.controller.client;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cinema.cinema_service.client.UserClient;
import com.cinema.cinema_service.domain.request.ReqHoldSeatDTO;
import com.cinema.cinema_service.domain.response.ResHoldSeatDTO;
import com.cinema.cinema_service.domain.response.ResUserDTO;
import com.cinema.cinema_service.service.SeatLockService;
import com.cinema.cinema_service.util.error.IdInvalidException;

import jakarta.validation.Valid;

import java.time.Instant;

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
    private final UserClient userClient;

    public SeatController(SeatLockService seatLockService, UserClient userClient) {
        this.seatLockService = seatLockService;
        this.userClient = userClient;
    }

    @PostMapping("/hold")
    public ResponseEntity<?> holdSeats(@Valid @RequestBody ReqHoldSeatDTO request,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail)
            throws IdInvalidException {
        if (userEmail == null || userEmail.trim().isEmpty()) {
            throw new IdInvalidException("Xác thực người dùng không hợp lệ hoặc thiếu thông tin email");
        }

        // Lấy ID từ user-service một lần khi giữ ghế
        ResUserDTO userDto = userClient.getUserByEmail(userEmail);
        if (userDto == null) {
            throw new IdInvalidException("Không tìm thấy thông tin người dùng trên hệ thống");
        }

        for (Long seatId : request.getSeatIds()) {
            boolean success = seatLockService.lockSeat(request.getShowtimeId(), seatId, userEmail);
            if (!success) {
                throw new IdInvalidException("Ghế có ID " + seatId + " đang được giữ hoặc đã có người chọn!");
            }
        }

        Instant expiresAt = Instant.now().plusSeconds(300);
        ResHoldSeatDTO res = new ResHoldSeatDTO(request.getShowtimeId(), request.getSeatIds(), expiresAt,
                "Giữ ghế thành công trong 5 phút");

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
