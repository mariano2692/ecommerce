package com.mmenendez.microservices.notification_microservice.exceptions;

public class NotificationException extends RuntimeException {

    public NotificationException(String message) {
        super(message);
    }
}
