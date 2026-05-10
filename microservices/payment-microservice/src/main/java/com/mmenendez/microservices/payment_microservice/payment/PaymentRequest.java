package com.mmenendez.microservices.payment_microservice.payment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PaymentRequest(
    @NotNull(message = "Order ID cannot be null")
    Long orderId,
    @NotNull(message = "Customer ID cannot be null")
    String customerId,
    @NotNull(message = "Amount cannot be null")
    @Positive(message = "Amount must be positive")
    Double amount,
    @NotNull(message = "Payment method cannot be null")
    PaymentMethod paymentMethod
) {
}
