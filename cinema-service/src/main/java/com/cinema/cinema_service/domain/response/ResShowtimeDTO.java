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
    private MovieShowtime movie;
    private RoomShowtime room;
    private Instant createdAt;
    private Instant updatedAt;

    @Getter
    @Setter
    public static class MovieShowtime {
        private Long id;
        private String name;
    }

    @Getter
    @Setter
    public static class RoomShowtime {
        private Long id;
        private String name;
        private String cinemaName;
    }
}
