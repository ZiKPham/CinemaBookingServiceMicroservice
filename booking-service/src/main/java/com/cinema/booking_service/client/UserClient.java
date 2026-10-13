package com.cinema.booking_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.cinema.booking_service.domain.response.ResUserDTO;
import com.cinema.booking_service.domain.response.RestResponse;

@FeignClient(name = "user-service")
public interface UserClient {
    @GetMapping("/api/v1/users/by-email")
    RestResponse<ResUserDTO> getUserByEmail(@RequestParam("email") String email);
}
