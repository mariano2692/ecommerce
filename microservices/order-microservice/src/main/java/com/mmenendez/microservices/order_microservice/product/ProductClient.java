package com.mmenendez.microservices.order_microservice.product;

import java.util.List;
import java.util.Optional;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "PRODUCT-MICROSERVICE", fallback = ProductClientFallback.class)
public interface ProductClient {

    @GetMapping("/api/v1/products/variants/{id}")
    Optional<ProductResponse> getVariantById(@PathVariable("id") Integer variantId);

    @PostMapping("/api/v1/products/purchase")
    void purchaseProducts(@RequestBody List<ProductPurchaseRequest> request);

    @PostMapping("/api/v1/products/restock")
    void restockProducts(@RequestBody List<ProductPurchaseRequest> request);
}
