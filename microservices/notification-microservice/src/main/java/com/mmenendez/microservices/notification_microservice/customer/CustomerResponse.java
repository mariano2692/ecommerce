package com.mmenendez.microservices.notification_microservice.customer;

public record CustomerResponse(
    String id,
    String firstName,
    String lastName,
    String email,
    String phone
) {
}
