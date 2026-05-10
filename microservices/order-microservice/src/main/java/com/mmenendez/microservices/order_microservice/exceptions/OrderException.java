package com.mmenendez.microservices.order_microservice.exceptions;

public class OrderException extends RuntimeException {

    public OrderException(String message) {
        super(message);
    }
}
