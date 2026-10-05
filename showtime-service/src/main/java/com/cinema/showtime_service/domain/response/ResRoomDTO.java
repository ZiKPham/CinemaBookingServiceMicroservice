package com.cinema.showtime_service.domain.response;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResRoomDTO {
    private long id;
    private String name;
    private int totalSeats;
    private CinemaRoom cinema;
    private Instant createdAt;
    private Instant updatedAt;

    @Getter
    @Setter
    public static class CinemaRoom {
        private long id;
        private String name;
    }
}