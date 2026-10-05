package com.cinema.showtime_service.client;

import com.cinema.showtime_service.domain.response.ResMovieDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "movie-service") // Tên của movie-service đăng ký trên Eureka / Consul hoặc cấu hình trong
                                     // properties
public interface MovieClient {

    @GetMapping("/api/v1/movies/{id}")
    ResMovieDTO getMovieById(@PathVariable("id") long id);
}