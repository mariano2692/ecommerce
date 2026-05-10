package com.mmenendez.product_microservice.product;

import java.util.List;

public record ProductDetailResponse(
    Integer id,
    String name,
    String description,
    String imageUrl,
    Integer categoryId,
    String categoryName,
    String categoryDescription,
    List<ProductVariantResponse> variants
) {}
