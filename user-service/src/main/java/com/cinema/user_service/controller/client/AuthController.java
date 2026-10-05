package com.cinema.user_service.controller.client;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cinema.user_service.domain.User;
import com.cinema.user_service.domain.request.LoginDTO;
import com.cinema.user_service.domain.request.RegisterDTO;
import com.cinema.user_service.domain.response.ResLoginDTO;
import com.cinema.user_service.domain.response.ResRegisterDTO;
import com.cinema.user_service.domain.response.ResUserDTO;
import com.cinema.user_service.service.UserService;
import com.cinema.user_service.util.SecurityUtil;
import com.cinema.user_service.util.error.IdInvalidException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtil securityUtil;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserService userService, PasswordEncoder passwordEncoder,
            SecurityUtil securityUtil, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.securityUtil = securityUtil;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public ResponseEntity<ResRegisterDTO> register(@Valid @RequestBody RegisterDTO registerDTO)
            throws IdInvalidException {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.userService.handleRegister(registerDTO));
    }

    @PostMapping("/login")
    public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody LoginDTO loginDTO) throws IdInvalidException {
        // 1. Nạp username và password vào token xác thực
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginDTO.getUsername(), loginDTO.getPassword());

        // 2. Xác thực người dùng (Spring sẽ tự động gọi UserDetailsCustom để check DB)
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        // 3. Lưu thông tin vào SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 4. Lấy thông tin user để tạo JWT token
        User user = this.userService.getUserByUsername(loginDTO.getUsername());
        String access_token = this.securityUtil.createToken(user.getEmail(), user.getRole());

        return ResponseEntity.ok(new ResLoginDTO(access_token));
    }
}
