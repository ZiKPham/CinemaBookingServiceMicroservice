package com.cinema.booking_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.cinema.booking_service.domain.response.ResUserDTO;

@FeignClient(name = "user-service")
public interface UserClient {
    @GetMapping("/api/v1/admin/users/{email}")
    ResUserDTO getUserByEmail(@PathVariable("email") String email);
}
