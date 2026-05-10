package com.mmenendez.microservices.notification_microservice.notification;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Notification {

    @Id
    private String id;

    private Long orderId;
    private String customerId;
    private String customerEmail;
    private String subject;
    private NotificationStatus status;
    private LocalDateTime sentAt;
}
