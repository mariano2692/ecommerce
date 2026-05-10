package com.mmenendez.microservices.order_microservice.exceptions;

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


@RestControllerAdvice(basePackages = "com.mmenendez.microservices.order_microservice")
@Primary
@Slf4j
public class OrderExceptionHandler extends GlobalExceptionHandler {

    @ExceptionHandler(OrderException.class)
    public ResponseEntity<ErrorResponse> handleOrderException(OrderException exception) {
        var errors = new HashMap<String, String>();
        errors.put("order", exception.getMessage());
        log.warn("Order error: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(errors));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(UnauthorizedException exception) {
        var errors = new HashMap<String, String>();
        errors.put("access", exception.getMessage());
        log.warn("Access denied: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(errors));
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> handleFeignException(FeignException exception) {
        var errors = new HashMap<String, String>();
        errors.put("Error communicating with microservice", exception.getMessage());
        log.warn("Error communicating with microservice: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(errors));
    }
}
