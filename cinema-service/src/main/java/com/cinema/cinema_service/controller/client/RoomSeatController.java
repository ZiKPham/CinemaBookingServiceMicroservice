package com.cinema.cinema_service.controller.client;

import com.cinema.cinema_service.domain.response.ResSeatDTO;
import com.cinema.cinema_service.service.SeatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RoomSeatController {

    private final SeatService seatService;

    public RoomSeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    @GetMapping("/api/v1/rooms/{roomId}/seats")
    public ResponseEntity<List<ResSeatDTO>> getSeatsByRoomId(
            @PathVariable("roomId") long roomId,
            @RequestParam(value = "showtimeId", required = false) Long showtimeId) {
        List<ResSeatDTO> seats = seatService.getSeatsByRoomIdAndShowtime(roomId, showtimeId);
        return ResponseEntity.ok(seats);
    }
}