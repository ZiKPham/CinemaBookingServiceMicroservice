package com.cinema.showtime_service.controller.admin;

import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cinema.showtime_service.domain.request.ReqCreateShowtimeDTO;
import com.cinema.showtime_service.domain.request.ReqUpdateShowtimeDTO;
import com.cinema.showtime_service.domain.request.ShowtimeSearchCriteria;
import com.cinema.showtime_service.domain.response.ResShowtimeDTO;
import com.cinema.showtime_service.domain.response.ResultPaginationDTO;
import com.cinema.showtime_service.service.ShowtimeService;
import com.cinema.showtime_service.util.error.IdInvalidException;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/showtimes")
public class AdminShowtimeController {

    private final ShowtimeService showtimeService;

    public AdminShowtimeController(ShowtimeService showtimeService) {
        this.showtimeService = showtimeService;
    }

    @PostMapping
    public ResponseEntity<ResShowtimeDTO> createShowtime(@Valid @RequestBody ReqCreateShowtimeDTO req)
            throws IdInvalidException {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.showtimeService.handleCreateShowtime(req));
    }

    @GetMapping
    public ResponseEntity<ResultPaginationDTO> getAllShowtimes(
            @ParameterObject ShowtimeSearchCriteria criteria, @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(this.showtimeService.fetchAllShowtimes(criteria, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResShowtimeDTO> getShowtimeById(@PathVariable("id") long id) throws IdInvalidException {
        return ResponseEntity.ok(this.showtimeService.fetchShowtimeById(id));
    }

    @PutMapping("{id}")
    public ResponseEntity<ResShowtimeDTO> updateShowtime(@PathVariable("id") long id,
            @Valid @RequestBody ReqUpdateShowtimeDTO reqDTO)
            throws IdInvalidException {
        ResShowtimeDTO res = this.showtimeService.handleUpdateShowtime(id, reqDTO);
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShowtime(@PathVariable("id") long id) throws IdInvalidException {
        this.showtimeService.handleDeleteShowtime(id);
        return ResponseEntity.ok().build();
    }

}