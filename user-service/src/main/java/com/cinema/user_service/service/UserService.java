package com.cinema.user_service.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cinema.user_service.domain.User;
import com.cinema.user_service.domain.request.RegisterDTO;
import com.cinema.user_service.domain.request.ReqCreateUserDTO;
import com.cinema.user_service.domain.request.ReqUpdateUserDTO;
import com.cinema.user_service.domain.response.ResRegisterDTO;
import com.cinema.user_service.domain.response.ResUserDTO;
import com.cinema.user_service.repository.UserRepository;
import com.cinema.user_service.util.error.IdInvalidException;

@Service
public class UserService {
    final private PasswordEncoder passwordEncoder;
    final private UserRepository userRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User handleCreateUser(ReqCreateUserDTO reqUserDTO) {
        User user = new User();
        user.setEmail(reqUserDTO.getEmail());

        String hashPassword = this.passwordEncoder.encode(reqUserDTO.getPassword());
        user.setPassword(hashPassword);

        user.setFullName(reqUserDTO.getFullName());
        user.setPhone(reqUserDTO.getPhone());

        return this.userRepository.save(user);
    }

    public List<ResUserDTO> fetchAllUsers() {
        List<User> users = this.userRepository.findAll();
        return users.stream()
                .map(this::convertToResUserDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "user-detail", key = "#p0")
    public ResUserDTO fetchUserById(Long id) throws IdInvalidException {
        Optional<User> userOptional = this.userRepository.findById(id);
        if (!userOptional.isPresent()) {
            throw new IdInvalidException("User với id = " + id + " không tồn tại");
        }
        return this.convertToResUserDTO(userOptional.get());
    }

    @Cacheable(value = "user-email", key = "#p0")
    public ResUserDTO getUserByEmail(String email) throws IdInvalidException {
        User user = this.userRepository.findByEmail(email);
        if (user == null) {
            throw new IdInvalidException("Email người dùng không tồn tại: " + email);
        }
        return convertToResUserDTO(user);
    }

    @CacheEvict(value = { "user-detail", "user-email" }, allEntries = true)
    public ResUserDTO handleUpdateUser(Long id, ReqUpdateUserDTO reqUpdateUserDTO) throws IdInvalidException {
        Optional<User> uOptional = this.userRepository.findById(id);
        if (!uOptional.isPresent()) {
            throw new IdInvalidException("User với id = " + id + " không tồn tại");
        }

        User currentUser = uOptional.get();
        if (reqUpdateUserDTO.getFullName() != null && !reqUpdateUserDTO.getFullName().trim().isEmpty()) {
            currentUser.setFullName(reqUpdateUserDTO.getFullName());
        }

        // Chỉ update phone nếu client CÓ truyền vào
        if (reqUpdateUserDTO.getPhone() != null && !reqUpdateUserDTO.getPhone().trim().isEmpty()) {
            currentUser.setPhone(reqUpdateUserDTO.getPhone());
        }

        User updateUser = this.userRepository.save(currentUser);
        return this.convertToResUserDTO(updateUser);
    }

    @CacheEvict(value = { "user-detail", "user-email" }, allEntries = true)
    public void handleDeleteUser(long id) throws IdInvalidException {
        Optional<User> uOptional = this.userRepository.findById(id);
        if (!uOptional.isPresent()) {
            throw new IdInvalidException("User với id = " + id + " không tồn tại");
        }
        this.userRepository.deleteById(id);
    }

    public User getUserByUsername(String username) {
        return this.userRepository.findByEmail(username);
    }

    public ResRegisterDTO handleRegister(RegisterDTO dto) throws IdInvalidException {
        if (this.userRepository.existsByEmail(dto.getEmail())) {
            throw new IdInvalidException("User với email " + dto.getEmail() + " không tồn tại");
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

    public ResUserDTO convertToResUserDTO(User user) {
        ResUserDTO res = new ResUserDTO();
        res.setId(user.getId());
        res.setEmail(user.getEmail());
        res.setFullName(user.getFullName());
        res.setPhone(user.getPhone());
        res.setCreatedAt(user.getCreatedAt());
        res.setUpdatedAt(user.getUpdatedAt());
        return res;
    }
}
