package com.mmenendez.microservices.cart_microservice.products;

import java.util.Optional;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="PRODUCT-MICROSERVICE")
public interface ProductClient {
    @GetMapping("/api/v1/products/{id}")
    Optional <ProductResponse> getProductById(@PathVariable("id") Integer productId);
}
