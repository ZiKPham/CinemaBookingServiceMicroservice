package com.cinema.notification_service.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingEventDTO {
    private String bookingId;
    private String userEmail;
    private String movieTitle;
    private String seatNumber;
    private double totalPrice;
}