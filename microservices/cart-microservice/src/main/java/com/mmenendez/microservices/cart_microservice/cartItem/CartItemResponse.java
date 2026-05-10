package com.mmenendez.microservices.cart_microservice.cartItem;

public record CartItemResponse(
    Integer variantId,
    Integer quantity
) {}
