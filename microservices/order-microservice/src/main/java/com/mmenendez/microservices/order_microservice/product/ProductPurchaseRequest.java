package com.mmenendez.microservices.order_microservice.product;

public record ProductPurchaseRequest(
    Integer variantId,
    Integer quantity
) {}
