package com.mmenendez.microservices.notification_microservice.notification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record NotificationRequest(
    @NotNull(message = "Order ID is required")
    Long orderId,
    @NotBlank(message = "Customer ID is required")
    String customerId,
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    Double amount,
    @NotBlank(message = "Payment method is required")
    String paymentMethod
) {}
