package com.mmenendez.microservices.payment_microservice.payment;

import com.mmenendez.microservices.payment_microservice.exceptions.PaymentException;
import com.mmenendez.microservices.payment_microservice.notification.OrderConfirmedEvent;
import com.mmenendez.microservices.payment_microservice.notification.OrderConfirmedProducer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final OrderConfirmedProducer orderConfirmedProducer;

    public PaymentResponse processPayment(PaymentRequest request) {
        Payment payment = Payment.builder()
                .orderId(request.orderId())
                .customerId(request.customerId())
                .amount(request.amount())
                .paymentMethod(request.paymentMethod())
                .status(PaymentStatus.PROCESSING)
                .build();

        paymentRepository.save(payment);

        // Simulated payment processing — always succeeds
        // In production this would call Stripe/MercadoPago/etc.
        payment.setStatus(PaymentStatus.COMPLETED);
        paymentRepository.save(payment);

        log.info("Payment completed for order id: {}", request.orderId());

        orderConfirmedProducer.publish(new OrderConfirmedEvent(
            request.orderId(),
            request.customerId(),
            request.amount(),
            request.paymentMethod().name()
        ));

        return paymentMapper.toPaymentResponse(payment);
    }

    public PaymentResponse getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentException("Payment for order with id " + orderId + " does not exist"));
        return paymentMapper.toPaymentResponse(payment);
    }

    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentException("Payment with id " + id + " does not exist"));
        return paymentMapper.toPaymentResponse(payment);
    }
}
