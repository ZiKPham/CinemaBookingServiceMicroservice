package com.cinema.booking_service.domain.response;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResUserDTO {
    private long id;
    private String email;
    private String fullName;
    private String phone;
    private Instant createdAt;
    private Instant updatedAt;
}
