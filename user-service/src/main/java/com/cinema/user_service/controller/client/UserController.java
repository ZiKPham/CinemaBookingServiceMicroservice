package com.cinema.user_service.controller.client;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cinema.user_service.domain.User;
import com.cinema.user_service.domain.request.ReqCreateUserDTO;
import com.cinema.user_service.domain.request.ReqUpdateUserDTO;
import com.cinema.user_service.domain.response.ResUserDTO;
import com.cinema.user_service.service.UserService;
import com.cinema.user_service.util.error.IdInvalidException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResUserDTO> updateUser(@PathVariable long id,
            @Valid @RequestBody ReqUpdateUserDTO reqUpdateUserDTO)
            throws IdInvalidException {
        return ResponseEntity.ok(this.userService.handleUpdateUser(id, reqUpdateUserDTO));
    }

    @GetMapping("/by-email")
    public ResponseEntity<ResUserDTO> getUserByEmail(@RequestParam("email") String email) throws IdInvalidException {
        ResUserDTO resUserDTO = this.userService.getUserByEmail(email);
        return ResponseEntity.ok(resUserDTO);
    }
}