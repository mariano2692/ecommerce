package com.mmenendez.microservices.notification_microservice.notification;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface NotificationRepository extends MongoRepository<Notification, String> {

    List<Notification> findByCustomerId(String customerId);

    List<Notification> findByOrderId(Long orderId);
}
