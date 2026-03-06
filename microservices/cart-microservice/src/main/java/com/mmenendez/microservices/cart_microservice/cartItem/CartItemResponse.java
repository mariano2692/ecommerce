package com.mmenendez.microservices.cart_microservice.cartItem;

public record CartItemResponse(
    Integer productId,
    Integer quantity
) {

}
