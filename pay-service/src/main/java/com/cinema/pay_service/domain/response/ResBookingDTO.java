package com.cinema.pay_service.domain.response;

import java.time.Instant;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResBookingDTO {
    private long id;
    private double totalPrice;
    private String status;
    private String paymentMethod;
    private String userEmail;
    private Long showtimeId;
    private List<TicketDTO> tickets;
    private Instant createdAt;
    private Instant updatedAt;

    @Getter
    @Setter
    public static class TicketDTO {
        private long id;
        private String seatNumber;
        private double price;
    }
}