package com.cinema.user_service.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.cinema.user_service.domain.User;
import com.cinema.user_service.domain.request.LoginDTO;
import com.cinema.user_service.domain.request.RegisterDTO;
import com.cinema.user_service.domain.response.ResLoginDTO;
import com.cinema.user_service.domain.response.ResRegisterDTO;
import com.cinema.user_service.repository.UserRepository;
import com.cinema.user_service.util.SecurityUtil;
import com.cinema.user_service.util.error.IdInvalidException;

import io.github.bucket4j.Bucket;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final LoginRateLimiterService loginRateLimiterService;
    private final AuthenticationManager authenticationManager;
    private final SecurityUtil securityUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
            UserService userService, LoginRateLimiterService loginRateLimiterService,
            AuthenticationManager authenticationManager, SecurityUtil securityUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
        this.loginRateLimiterService = loginRateLimiterService;
        this.authenticationManager = authenticationManager;
        this.securityUtil = securityUtil;
    }

    public ResRegisterDTO handleRegister(RegisterDTO dto) throws IdInvalidException {
        if (this.userRepository.existsByEmail(dto.getEmail())) {
            throw new IdInvalidException("User với email " + dto.getEmail() + " đã tồn tại");
        }

        User newUser = new User();
        newUser.setEmail(dto.getEmail());
        newUser.setPassword(this.passwordEncoder.encode(dto.getPassword()));
        newUser.setPhone(dto.getPhone());
        newUser.setFullName(dto.getFullName());

        User saved = this.userRepository.save(newUser);

        ResRegisterDTO res = new ResRegisterDTO();
        res.setId(saved.getId());
        res.setEmail(saved.getEmail());
        res.setFullName(saved.getFullName());
        res.setPhone(saved.getPhone());

        return res;
    }

    public ResLoginDTO handleLogin(LoginDTO loginDTO) {
        String normalizedUsername = loginDTO.getUsername() == null ? "" : loginDTO.getUsername().trim().toLowerCase();
        Bucket bucket = this.loginRateLimiterService.resolveBucket(normalizedUsername);

        if (!bucket.tryConsume(1)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Bạn đã nhập sai quá nhiều lần hoặc thử đăng nhập quá nhanh. Vui lòng thử lại sau 1 phút.");
        }

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginDTO.getUsername(), loginDTO.getPassword());
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Tạo JWT token trả về khi đăng nhập thành công
        User user = this.userService.getUserByUsername(loginDTO.getUsername());
        String accessToken = this.securityUtil.createToken(user.getEmail(), user.getRole());

        return new ResLoginDTO(accessToken);
    }
}