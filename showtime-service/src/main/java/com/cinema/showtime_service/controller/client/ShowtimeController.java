package com.cinema.showtime_service.controller.client;

import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cinema.showtime_service.domain.request.ShowtimeSearchCriteria;
import com.cinema.showtime_service.domain.response.ResSeatDTO;
import com.cinema.showtime_service.domain.response.ResShowtimeDTO;
import com.cinema.showtime_service.domain.response.ResultPaginationDTO;
import com.cinema.showtime_service.service.ShowtimeService;
import com.cinema.showtime_service.util.error.IdInvalidException;

@RestController
@RequestMapping("/api/v1/showtimes")
public class ShowtimeController {

    private final ShowtimeService showtimeService;

    public ShowtimeController(ShowtimeService showtimeService) {
        this.showtimeService = showtimeService;
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

    @GetMapping("/{showtimeId}/seats")
    public ResponseEntity<List<ResSeatDTO>> getSeatsByShowtime(
            @PathVariable("showtimeId") long showtimeId) throws IdInvalidException {
        List<ResSeatDTO> seatMap = this.showtimeService.getSeatMapByShowtime(showtimeId);
        return ResponseEntity.ok(seatMap);
    }
}