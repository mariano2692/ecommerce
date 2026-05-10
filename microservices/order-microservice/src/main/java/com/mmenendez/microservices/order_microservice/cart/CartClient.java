package com.mmenendez.microservices.order_microservice.cart;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "CART-MICROSERVICE", fallback = CartClientFallback.class)
public interface CartClient {

    @GetMapping("/api/v1/{customerId}/cart/")
    CartResponse getCartByCustomerId(@PathVariable("customerId") String customerId);

    @DeleteMapping("/api/v1/{customerId}/cart/")
    void clearCart(@PathVariable("customerId") String customerId);
}
