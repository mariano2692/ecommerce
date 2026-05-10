package com.mmenendez.microservices.payment_microservice.notification;

public record OrderConfirmedEvent(
    Long orderId,
    String customerId,
    Double amount,
    String paymentMethod
) {
}
