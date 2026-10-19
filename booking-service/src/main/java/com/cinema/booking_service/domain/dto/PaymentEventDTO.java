package com.cinema.booking_service.domain.dto;

import java.time.Instant;

import com.cinema.booking_service.util.constant.PaymentMethod;
import com.cinema.booking_service.util.constant.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentEventDTO {
    private Long paymentId;
    private Long bookingId;
    private String userEmail;
    private Double amount;
    private PaymentMethod paymentMethod;
    private String transactionCode;
    private PaymentStatus status;
    private Instant createdAt;
}
