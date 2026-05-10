package com.mmenendez.microservices.notification_microservice.exceptions;

import java.util.HashMap;

import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.mmenendez.common_exceptions.ErrorResponse;
import com.mmenendez.common_exceptions.GlobalExceptionHandler;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice(basePackages = "com.mmenendez.microservices.notification_microservice")
@Primary
@Slf4j
public class NotificationExceptionHandler extends GlobalExceptionHandler {

    @ExceptionHandler(NotificationException.class)
    public ResponseEntity<ErrorResponse> handleNotificationException(NotificationException exception) {
        var errors = new HashMap<String, String>();
        errors.put("notification", exception.getMessage());
        log.warn("Notification error: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(errors));
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> handleFeignException(FeignException exception) {
        var errors = new HashMap<String, String>();
        errors.put("Error communicating with microservice", exception.getMessage());
        log.warn("Error communicating with microservice: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(errors));
    }
}
