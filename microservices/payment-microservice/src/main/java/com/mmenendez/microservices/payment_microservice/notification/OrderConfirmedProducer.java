package com.mmenendez.microservices.payment_microservice.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderConfirmedProducer {

    static final String TOPIC = "order-confirmed";

    private final KafkaTemplate<String, OrderConfirmedEvent> kafkaTemplate;

    public void publish(OrderConfirmedEvent event) {
        kafkaTemplate.send(TOPIC, event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish OrderConfirmedEvent for order {}: {}", event.orderId(), ex.getMessage());
                } else {
                    log.info("OrderConfirmedEvent published for order {} to partition {}",
                        event.orderId(), result.getRecordMetadata().partition());
                }
            });
    }
}
