package com.cinema.booking_service.domain.response;

import java.time.Instant;
import java.util.List;

import com.cinema.booking_service.util.constant.BookingStatus;
import com.cinema.booking_service.util.constant.PaymentMethod;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResBookingDTO {

    private long id;
    private double totalPrice;
    private BookingStatus status;
    private PaymentMethod paymentMethod;
    private Instant createdAt;

    private UserSummary user;
    private ShowtimeSummary showtime;
    private List<TicketSummary> tickets;

    @Getter
    @Setter
    public static class UserSummary {
        private long id;
        private String fullName;
        private String email;
    }

    @Getter
    @Setter
    public static class ShowtimeSummary {
        private long id;
        private String movieTitle;
        private String roomName;
        private Instant startTime;
    }

    @Getter
    @Setter
    public static class TicketSummary {
        private long id;
        private Long seatId;
        private String seatNumber;
        private double price;
        private String ticketCode;
        private String qrCode;
    }
}
