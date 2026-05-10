package com.mmenendez.microservices.payment_microservice.exceptions;

public class PaymentException extends RuntimeException {

    public PaymentException(String message) {
        super(message);
    }
}
