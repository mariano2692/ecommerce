package com.mmenendez.microservices.cart_microservice.products;

import java.util.Optional;

import org.springframework.stereotype.Component;

@Component
public class ProductClientFallback implements ProductClient {

    @Override
    public Optional<ProductResponse> getVariantById(Integer variantId) {
        return Optional.empty();
    }
}
