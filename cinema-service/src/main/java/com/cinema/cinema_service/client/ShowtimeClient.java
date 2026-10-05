package com.cinema.cinema_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.cinema.cinema_service.domain.response.ResShowtimeDTO;
import com.cinema.cinema_service.domain.response.RestResponse;

@FeignClient(name = "showtime-service")
public interface ShowtimeClient {
    @GetMapping("/api/v1/showtimes/{id}")
    RestResponse<ResShowtimeDTO> getShowtimeById(@PathVariable("id") long id);

}
