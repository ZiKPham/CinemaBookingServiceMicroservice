package com.cinema.cinema_service.domain.response;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResShowtimeDTO {
    private long id;
    private Instant startTime;
    private Instant endTime;
    private double price;
    private Long movieId;
    private Long roomId;
    private Instant createdAt;
    private Instant updatedAt;
}