package com.mmenendez.product_microservice.product;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record ProductRequest(
    Integer id,
    @NotBlank(message = "Product name is required")
    String name,
    String description,
    String imageUrl,
    @NotNull(message = "Category ID is required")
    Integer categoryId,
    @NotEmpty(message = "Product must have at least one variant")
    @Valid
    List<ProductVariantRequest> variants
) {}
