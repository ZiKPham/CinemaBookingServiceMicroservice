package com.cinema.booking_service.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResSeatLockDetailDTO {
    private Long showtimeId;
    private Long seatId;
    private String userEmail;
    private Instant expiresAt;
}