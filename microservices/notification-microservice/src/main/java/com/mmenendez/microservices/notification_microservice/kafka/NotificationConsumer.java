package com.mmenendez.microservices.notification_microservice.kafka;

import com.mmenendez.microservices.notification_microservice.notification.NotificationRequest;
import com.mmenendez.microservices.notification_microservice.notification.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = OrderConfirmedEvent.TOPIC, groupId = "notification-group")
    public void consume(OrderConfirmedEvent event) {
        log.info("Received OrderConfirmedEvent for order {}", event.orderId());
        notificationService.sendOrderConfirmation(new NotificationRequest(
            event.orderId(),
            event.customerId(),
            event.amount(),
            event.paymentMethod()
        ));
    }
}
