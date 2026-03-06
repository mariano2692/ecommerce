package com.mmenendez.microservices.cart_microservice.products;

public record ProductResponse(
    Integer id,
    String name,
    String description,
    Double price,
    Integer stock,
    String imageUrl,
    Integer categoryId,
    String categoryName,
    String categoryDescription
) {

}
