package com.mmenendez.microservices.order_microservice.product;

import java.util.List;
import java.util.Optional;

import com.mmenendez.microservices.order_microservice.exceptions.OrderException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ProductClientFallback implements ProductClient {

    @Override
    public Optional<ProductResponse> getVariantById(Integer variantId) {
        return Optional.empty();
    }

    @Override
    public void purchaseProducts(List<ProductPurchaseRequest> request) {
        throw new OrderException("Product service is unavailable");
    }

    @Override
    public void restockProducts(List<ProductPurchaseRequest> request) {
        log.warn("Product service unavailable during restock — manual stock correction may be required for items: {}", request);
    }
}
