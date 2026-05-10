package com.mmenendez.microservices.notification_microservice.channel;

import com.mmenendez.microservices.notification_microservice.customer.CustomerResponse;
import com.mmenendez.microservices.notification_microservice.notification.NotificationRequest;
import com.mmenendez.microservices.notification_microservice.notification.NotificationStatus;

public interface NotificationChannel {
    NotificationStatus send(CustomerResponse customer, NotificationRequest request);
}
