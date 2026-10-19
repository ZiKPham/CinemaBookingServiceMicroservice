package com.cinema.pay_service.domain.dto;

import com.cinema.pay_service.util.constant.PaymentMethod;
import com.cinema.pay_service.util.constant.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
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