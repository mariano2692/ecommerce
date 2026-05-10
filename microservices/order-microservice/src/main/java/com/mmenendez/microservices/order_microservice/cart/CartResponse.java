package com.mmenendez.microservices.order_microservice.cart;

import java.util.List;

public record CartResponse(
    String id,
    String customerId,
    List<CartItemResponse> cartItems
) {
}
