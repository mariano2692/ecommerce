package com.mmenendez.microservices.customer_microservice.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CustomerRequest(
    String id,
    @NotBlank(message = "First name is required")
    String firstName,
    @NotBlank(message = "Last name is required")
    String lastName,
    @NotBlank(message = "Email is required")
    @Email(message = "Email is not valid")
    String email,
    String phone,
    String address,
    String city
) {}
