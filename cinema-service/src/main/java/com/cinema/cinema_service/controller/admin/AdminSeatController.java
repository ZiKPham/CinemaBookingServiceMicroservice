package com.cinema.cinema_service.controller.admin;

import com.cinema.cinema_service.domain.request.ReqCreateSeatDTO;
import com.cinema.cinema_service.domain.request.ReqUpdateSeatDTO;
import com.cinema.cinema_service.domain.request.SeatSearchCriteria;
import com.cinema.cinema_service.domain.response.ResSeatDTO;
import com.cinema.cinema_service.domain.response.ResultPaginationDTO;
import com.cinema.cinema_service.service.SeatService;
import com.cinema.cinema_service.util.error.IdInvalidException;
import jakarta.validation.Valid;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/seats")
public class AdminSeatController {

    private final SeatService seatService;

    public AdminSeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    @PostMapping
    public ResponseEntity<ResSeatDTO> createSeat(@Valid @RequestBody ReqCreateSeatDTO reqDTO)
            throws IdInvalidException {
        return ResponseEntity.status(HttpStatus.CREATED).body(seatService.handleCreateSeat(reqDTO));
    }

    @PutMapping
    public ResponseEntity<ResSeatDTO> updateSeat(@Valid @RequestBody ReqUpdateSeatDTO reqDTO)
            throws IdInvalidException {
        return ResponseEntity.ok(seatService.handleUpdateSeat(reqDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSeat(@PathVariable("id") long id) throws IdInvalidException {
        seatService.handleDeleteSeat(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<ResultPaginationDTO> getAllSeats(
            @ParameterObject SeatSearchCriteria criteria,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(seatService.fetchAllSeats(criteria, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResSeatDTO> getSeatById(@PathVariable("id") long id) throws IdInvalidException {
        return ResponseEntity.ok(seatService.fetchSeatById(id));
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<ResSeatDTO>> getSeatsByRoom(@PathVariable("roomId") long roomId) {
        return ResponseEntity.ok(seatService.getSeatsByRoomIdAndShowtime(roomId, null));
    }
}