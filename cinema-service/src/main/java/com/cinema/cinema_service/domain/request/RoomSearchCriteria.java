package com.cinema.cinema_service.domain.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomSearchCriteria {
    private String name;
    private Integer totalSeats;
    private Long cinemaId;
}
