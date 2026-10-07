package com.cinema.user_service.controller.client;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cinema.user_service.domain.request.LoginDTO;
import com.cinema.user_service.domain.request.RegisterDTO;
import com.cinema.user_service.domain.response.ResLoginDTO;
import com.cinema.user_service.domain.response.ResRegisterDTO;
import com.cinema.user_service.service.AuthService;
import com.cinema.user_service.util.error.IdInvalidException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ResRegisterDTO> register(@Valid @RequestBody RegisterDTO registerDTO)
            throws IdInvalidException {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.authService.handleRegister(registerDTO));
    }

    @PostMapping("/login")
    public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
        return ResponseEntity.ok(this.authService.handleLogin(loginDTO));
    }
}