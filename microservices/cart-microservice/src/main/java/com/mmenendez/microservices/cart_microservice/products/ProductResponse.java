package com.mmenendez.microservices.cart_microservice.products;

import java.util.Map;

public record ProductResponse(
    Integer id,
    String sku,
    Double price,
    Integer stock,
    Integer productId,
    String productName,
    Map<String, String> attributes
) {}
