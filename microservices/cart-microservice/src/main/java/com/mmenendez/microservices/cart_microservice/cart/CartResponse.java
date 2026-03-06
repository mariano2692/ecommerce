package com.mmenendez.microservices.cart_microservice.cart;

import java.util.List;

import com.mmenendez.microservices.cart_microservice.cartItem.CartItemResponse;

public record CartResponse(
    String id,
    String customerId,
    List<CartItemResponse> cartItems
) {

}
