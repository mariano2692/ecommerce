package com.mmenendez.product_microservice.product;

public record ProductResponse(
    Integer id,
    String name,
    String description,
    String imageUrl,
    Integer categoryId,
    String categoryName,
    String categoryDescription,
    Double priceFrom,
    Integer totalStock
) {}
