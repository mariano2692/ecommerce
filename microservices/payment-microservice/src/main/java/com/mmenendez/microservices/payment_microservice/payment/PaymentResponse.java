package com.mmenendez.microservices.payment_microservice.payment;

import java.time.LocalDateTime;

public record PaymentResponse(
    Long id,
    Long orderId,
    String customerId,
    Double amount,
    PaymentMethod paymentMethod,
    PaymentStatus status,
    LocalDateTime createdAt
) {
}
