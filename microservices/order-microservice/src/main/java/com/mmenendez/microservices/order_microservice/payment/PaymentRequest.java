package com.mmenendez.microservices.order_microservice.payment;

public record PaymentRequest(
    Long orderId,
    String customerId,
    Double amount,
    PaymentMethod paymentMethod
) {
}
