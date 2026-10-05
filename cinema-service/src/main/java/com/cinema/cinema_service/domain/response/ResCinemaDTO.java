package com.cinema.cinema_service.domain.response;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResCinemaDTO {
    private long id;
    private String name;
    private String address;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
}