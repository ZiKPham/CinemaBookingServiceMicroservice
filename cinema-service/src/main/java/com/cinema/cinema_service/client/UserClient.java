package com.cinema.cinema_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.cinema.cinema_service.domain.response.ResUserDTO;

@FeignClient(name = "user-service") // Tên service của bạn trên Eureka
public interface UserClient {

    @GetMapping("/api/v1/users/by-email") // Đường dẫn API bên user-service để tìm user theo email
    ResUserDTO getUserByEmail(@RequestParam("email") String email);

    @GetMapping("/api/v1/admin/users/{id}")
    ResUserDTO getUserById(@PathVariable("id") Long id);
}
