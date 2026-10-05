package com.cinema.cinema_service.domain.response;

import java.time.Instant;

import com.cinema.cinema_service.domain.Seat;
import com.cinema.cinema_service.util.constant.SeatType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResSeatDTO {
    private long id;
    private String seatNumber;
    private SeatType seatType;
    private boolean isBooked;
    private long roomId;
    private String roomName;
    private Instant createdAt;
    private Instant updatedAt;

    public ResSeatDTO(Seat seat, boolean isBooked) {
        this.id = seat.getId();
        this.seatNumber = seat.getSeatNumber();
        this.seatType = seat.getSeatType();
        this.isBooked = isBooked;
        if (seat.getRoom() != null) {
            this.roomId = seat.getRoom().getId();
            this.roomName = seat.getRoom().getName();
        }
        this.createdAt = seat.getCreatedAt();
        this.updatedAt = seat.getUpdatedAt();
    }
}