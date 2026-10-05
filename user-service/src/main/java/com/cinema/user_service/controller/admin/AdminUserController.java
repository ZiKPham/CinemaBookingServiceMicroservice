package com.cinema.user_service.controller.admin;

import java.util.List;

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
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<ResUserDTO> createNewUser(@Valid @RequestBody ReqCreateUserDTO reqCreateUserDTO) {
        User user = this.userService.handleCreateUser(reqCreateUserDTO);
        ResUserDTO resUserDTO = this.userService.convertToResUserDTO(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(resUserDTO);
    }

    @GetMapping
    public ResponseEntity<List<ResUserDTO>> getAllUser() {
        return ResponseEntity.status(HttpStatus.OK).body(userService.fetchAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResUserDTO> getUserById(@PathVariable long id) throws IdInvalidException {
        return ResponseEntity.status(HttpStatus.OK).body(this.userService.fetchUserById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResUserDTO> updateUser(@PathVariable long id,
            @Valid @RequestBody ReqUpdateUserDTO reqUpdateUserDTO)
            throws IdInvalidException {
        return ResponseEntity.ok(this.userService.handleUpdateUser(id, reqUpdateUserDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") long id) throws IdInvalidException {
        this.userService.handleDeleteUser(id);
        return ResponseEntity.ok(null);
    }
}