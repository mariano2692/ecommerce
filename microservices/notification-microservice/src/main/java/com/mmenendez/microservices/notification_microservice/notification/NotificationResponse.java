package com.mmenendez.microservices.notification_microservice.notification;

import java.time.LocalDateTime;

public record NotificationResponse(
    String id,
    Long orderId,
    String customerId,
    String customerEmail,
    String subject,
    NotificationStatus status,
    LocalDateTime sentAt
) {
}
