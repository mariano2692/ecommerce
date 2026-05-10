package com.mmenendez.microservices.order_microservice.payment;

import com.mmenendez.microservices.order_microservice.exceptions.OrderException;

import org.springframework.stereotype.Component;

@Component
public class PaymentClientFallback implements PaymentClient {

    @Override
    public void processPayment(PaymentRequest request) {
        throw new OrderException("Payment service is unavailable");
    }
}
