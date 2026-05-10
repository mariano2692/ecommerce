package com.mmenendez.microservices.order_microservice.product;

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
