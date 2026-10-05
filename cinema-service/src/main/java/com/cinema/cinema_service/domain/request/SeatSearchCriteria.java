package com.cinema.cinema_service.domain.request;

import com.cinema.cinema_service.util.constant.SeatType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SeatSearchCriteria {
    private String seatNumber;
    private SeatType seatType;
    private Long roomId;
    private Long cinemaId;
}