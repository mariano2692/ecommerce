package com.mmenendez.microservices.cart_microservice.exceptions;

public class CartException extends RuntimeException {

    public CartException(String message) {
        super(message);
    }
}
