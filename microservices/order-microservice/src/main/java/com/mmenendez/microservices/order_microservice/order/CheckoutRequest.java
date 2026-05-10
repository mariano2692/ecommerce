package com.mmenendez.microservices.order_microservice.order;

import com.mmenendez.microservices.order_microservice.payment.PaymentMethod;

import jakarta.validation.constraints.NotNull;

public record CheckoutRequest(
    @NotNull(message = "Payment method cannot be null")
    PaymentMethod paymentMethod
) {
}
