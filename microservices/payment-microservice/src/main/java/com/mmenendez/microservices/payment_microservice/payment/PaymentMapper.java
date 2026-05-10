package com.mmenendez.microservices.payment_microservice.payment;

import org.springframework.stereotype.Service;

@Service
public class PaymentMapper {

    public PaymentResponse toPaymentResponse(Payment payment) {
        return new PaymentResponse(
            payment.getId(),
            payment.getOrderId(),
            payment.getCustomerId(),
            payment.getAmount(),
            payment.getPaymentMethod(),
            payment.getStatus(),
            payment.getCreatedAt()
        );
    }
}
