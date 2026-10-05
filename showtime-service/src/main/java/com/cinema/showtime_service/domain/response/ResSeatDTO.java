package com.cinema.showtime_service.domain.response;

import com.cinema.showtime_service.util.constant.SeatType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResSeatDTO {
    private long id;
    private String seatNumber;
    private SeatType seatType;
    private boolean isBooked;

}