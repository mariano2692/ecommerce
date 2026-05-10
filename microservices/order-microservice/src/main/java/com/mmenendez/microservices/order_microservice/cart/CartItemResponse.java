package com.mmenendez.microservices.order_microservice.cart;

public record CartItemResponse(
    Integer variantId,
    Integer quantity
) {}
