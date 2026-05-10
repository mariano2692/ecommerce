package com.mmenendez.product_microservice.product;

import java.util.Map;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ProductVariantRequest(
    Integer id,
    @NotBlank(message = "SKU is required")
    String sku,
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than zero")
    Double price,
    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock cannot be negative")
    Integer stock,
    Map<String, String> attributes
) {}
