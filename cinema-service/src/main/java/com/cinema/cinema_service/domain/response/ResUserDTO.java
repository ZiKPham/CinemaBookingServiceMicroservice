package com.cinema.cinema_service.domain.response;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResUserDTO {
    private long id;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private Instant createdAt;
    private Instant updatedAt;
}