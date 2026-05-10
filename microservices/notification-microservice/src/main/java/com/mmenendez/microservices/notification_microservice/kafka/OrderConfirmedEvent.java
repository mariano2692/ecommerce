package com.mmenendez.microservices.notification_microservice.kafka;

public record OrderConfirmedEvent(
    Long orderId,
    String customerId,
    Double amount,
    String paymentMethod
) {
    static final String TOPIC = "order-confirmed";
}
