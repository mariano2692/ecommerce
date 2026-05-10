package com.mmenendez.product_microservice.product;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ProductQuantityRequest(
    @NotNull(message = "Variant ID is required")
    Integer variantId,
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    Integer quantity
) {}
