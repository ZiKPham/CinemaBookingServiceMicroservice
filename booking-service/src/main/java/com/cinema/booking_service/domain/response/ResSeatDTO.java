package com.cinema.booking_service.domain.response;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResSeatDTO {
    private Long id;
    private String seatNumber;
    private String seatType;
    private Long roomId;
    private String roomName;
    private Instant createdAt;
    private Instant updatedAt;
    private Boolean booked;
}
