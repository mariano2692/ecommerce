package com.mmenendez.product_microservice.product;

import java.util.Map;

public record ProductVariantResponse(
    Integer id,
    String sku,
    Double price,
    Integer stock,
    Map<String, String> attributes
) {}
