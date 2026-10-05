package com.cinema.cinema_service.controller.admin;

import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cinema.cinema_service.domain.request.ReqCreateRoomDTO;
import com.cinema.cinema_service.domain.request.ReqUpdateRoomDTO;
import com.cinema.cinema_service.domain.request.RoomSearchCriteria;
import com.cinema.cinema_service.domain.response.ResRoomDTO;
import com.cinema.cinema_service.domain.response.ResultPaginationDTO;
import com.cinema.cinema_service.service.RoomService;
import com.cinema.cinema_service.util.error.IdInvalidException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/rooms")
public class AdminRoomController {

    private final RoomService roomService;

    public AdminRoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping
    public ResponseEntity<ResRoomDTO> createRoom(@Valid @RequestBody ReqCreateRoomDTO reqDTO)
            throws IdInvalidException {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.roomService.handleCreateRoom(reqDTO));
    }

    @GetMapping
    public ResponseEntity<ResultPaginationDTO> getAllRooms(@ParameterObject RoomSearchCriteria criteria,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(this.roomService.fetchAllRooms(criteria, pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ResRoomDTO>> getRoomsByCinemaAndName(
            @RequestParam("cinemaName") String cinemaName,
            @RequestParam(value = ("roomName"), required = false) String roomName) {
        return ResponseEntity.ok(this.roomService.fetchRoomsByCinemaAndRoomName(cinemaName, roomName));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResRoomDTO> getRoomById(@PathVariable("id") long id) throws IdInvalidException {
        ResRoomDTO resRoomDTO = this.roomService.fetchRoomById(id);
        return ResponseEntity.ok(resRoomDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResRoomDTO> updateRoom(@PathVariable("id") long id, @RequestBody ReqUpdateRoomDTO reqDTO)
            throws IdInvalidException {
        return ResponseEntity.ok(this.roomService.handleUpdateRoom(id, reqDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable("id") long id) throws IdInvalidException {
        this.roomService.handleDeleteRoom(id);
        return ResponseEntity.ok(null);
    }
}