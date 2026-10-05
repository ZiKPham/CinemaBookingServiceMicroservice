package com.cinema.showtime_service.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.cinema.showtime_service.domain.response.ResRoomDTO;
import com.cinema.showtime_service.domain.response.ResSeatDTO;
import com.cinema.showtime_service.domain.response.RestResponse;

@FeignClient(name = "cinema-service")
public interface CinemaClient {
    // Lấy danh sách ghế theo roomId
    @GetMapping("/api/v1/rooms/{roomId}/seats")
    RestResponse<List<ResSeatDTO>> getSeatsByRoomId(
            @PathVariable("roomId") long roomId,
            @RequestParam(value = "showtimeId", required = false) Long showtimeId);

    @GetMapping("/api/v1/rooms/{id}")
    ResRoomDTO getRoomById(@PathVariable("id") long id);

}
