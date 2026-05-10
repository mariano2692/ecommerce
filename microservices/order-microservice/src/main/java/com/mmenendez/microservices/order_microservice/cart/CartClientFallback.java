package com.mmenendez.microservices.order_microservice.cart;

import com.mmenendez.microservices.order_microservice.exceptions.OrderException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CartClientFallback implements CartClient {

    @Override
    public CartResponse getCartByCustomerId(String customerId) {
        throw new OrderException("Cart service is unavailable");
    }

    @Override
    public void clearCart(String customerId) {
        log.warn("Cart service unavailable — cart for customer {} was not cleared", customerId);
    }
}
