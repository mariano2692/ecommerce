package com.mmenendez.microservices.order_microservice.orderItem;

public record OrderItemResponse(
    Long id,
    Integer variantId,
    String variantSku,
    String productName,
    Integer quantity,
    Double unitPrice
) {}
