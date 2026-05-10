package com.mmenendez.product_microservice.category;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequest(
    Integer id,
    @NotBlank(message = "Category name is required")
    String name,
    String description
) {}
