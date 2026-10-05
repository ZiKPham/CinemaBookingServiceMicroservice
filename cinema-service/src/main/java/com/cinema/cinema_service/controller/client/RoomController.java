package com.cinema.cinema_service.controller.client;

import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cinema.cinema_service.domain.request.RoomSearchCriteria;
import com.cinema.cinema_service.domain.response.ResRoomDTO;
import com.cinema.cinema_service.domain.response.ResultPaginationDTO;
import com.cinema.cinema_service.service.RoomService;
import com.cinema.cinema_service.util.error.IdInvalidException;

@RestController
@RequestMapping("/api/v1/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
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
}