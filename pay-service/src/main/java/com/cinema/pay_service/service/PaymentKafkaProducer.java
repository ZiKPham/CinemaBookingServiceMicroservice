package com.cinema.pay_service.service;

import com.cinema.pay_service.domain.dto.PaymentEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "payment-events-v1";

    public void sendPaymentEvent(PaymentEventDTO event) {
        log.info("🚀 [PAYMENT PRODUCER] Bắn event thanh toán cho bookingId: {} - Status: {}",
                event.getBookingId(), event.getStatus());
        kafkaTemplate.send(TOPIC, String.valueOf(event.getBookingId()), event);
    }
}