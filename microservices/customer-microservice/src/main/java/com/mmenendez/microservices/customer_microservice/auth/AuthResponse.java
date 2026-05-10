package com.mmenendez.microservices.customer_microservice.auth;

public record AuthResponse(
    String token,
    String customerId
) {
}
