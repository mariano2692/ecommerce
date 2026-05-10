package com.mmenendez.microservices.order_microservice.payment;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "PAYMENT-MICROSERVICE", fallback = PaymentClientFallback.class)
public interface PaymentClient {

    @PostMapping("/api/v1/payments")
    void processPayment(@RequestBody PaymentRequest request);
}
