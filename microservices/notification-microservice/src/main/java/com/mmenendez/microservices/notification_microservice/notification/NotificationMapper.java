package com.mmenendez.microservices.notification_microservice.notification;

import org.springframework.stereotype.Service;

@Service
public class NotificationMapper {

    public NotificationResponse toNotificationResponse(Notification notification) {
        return new NotificationResponse(
            notification.getId(),
            notification.getOrderId(),
            notification.getCustomerId(),
            notification.getCustomerEmail(),
            notification.getSubject(),
            notification.getStatus(),
            notification.getSentAt()
        );
    }
}
