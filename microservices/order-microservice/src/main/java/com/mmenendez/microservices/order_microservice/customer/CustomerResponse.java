package com.mmenendez.microservices.order_microservice.customer;

public record CustomerResponse(
    String id,
    String firstName,
    String lastName,
    String email
) {
}
