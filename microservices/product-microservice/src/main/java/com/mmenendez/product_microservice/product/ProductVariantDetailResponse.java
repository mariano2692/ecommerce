package com.mmenendez.product_microservice.product;

import java.util.Map;

public record ProductVariantDetailResponse(
    Integer id,
    String sku,
    Double price,
    Integer stock,
    Integer productId,
    String productName,
    Map<String, String> attributes
) {}
