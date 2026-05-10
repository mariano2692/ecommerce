package com.mmenendez.microservices.notification_microservice.notification;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.mmenendez.microservices.notification_microservice.channel.NotificationChannel;
import com.mmenendez.microservices.notification_microservice.customer.CustomerClient;
import com.mmenendez.microservices.notification_microservice.customer.CustomerResponse;
import com.mmenendez.microservices.notification_microservice.exceptions.NotificationException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final CustomerClient customerClient;
    private final List<NotificationChannel> channels; // Spring inyecta todos los @Component que implementan la interfaz

    public NotificationResponse sendOrderConfirmation(NotificationRequest request) {
        CustomerResponse customer = customerClient.getCustomerById(request.customerId())
            .orElseThrow(() -> new NotificationException("Customer with id " + request.customerId() + " does not exist"));

        // El status final refleja el canal principal (email). Cada canal loguea su propio resultado.
        NotificationStatus status = channels.stream()
            .map(channel -> channel.send(customer, request))
            .filter(s -> s != NotificationStatus.SKIPPED)
            .findFirst()
            .orElse(NotificationStatus.FAILED);

        Notification notification = Notification.builder()
            .orderId(request.orderId())
            .customerId(request.customerId())
            .customerEmail(customer.email())
            .subject("Order #" + request.orderId() + " confirmed")
            .status(status)
            .sentAt(LocalDateTime.now())
            .build();

        notificationRepository.save(notification);
        return notificationMapper.toNotificationResponse(notification);
    }

    public List<NotificationResponse> getNotificationsByCustomerId(String customerId) {
        return notificationRepository.findByCustomerId(customerId).stream()
            .map(notificationMapper::toNotificationResponse)
            .toList();
    }

    public List<NotificationResponse> getNotificationsByOrderId(Long orderId) {
        return notificationRepository.findByOrderId(orderId).stream()
            .map(notificationMapper::toNotificationResponse)
            .toList();
    }
}
