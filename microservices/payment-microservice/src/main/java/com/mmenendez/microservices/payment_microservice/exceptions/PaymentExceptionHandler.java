package com.mmenendez.microservices.payment_microservice.exceptions;

import java.util.HashMap;

import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.mmenendez.common_exceptions.ErrorResponse;
import com.mmenendez.common_exceptions.GlobalExceptionHandler;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice(basePackages = "com.mmenendez.microservices.payment_microservice")
@Primary
@Slf4j
public class PaymentExceptionHandler extends GlobalExceptionHandler {

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ErrorResponse> handlePaymentException(PaymentException exception) {
        var errors = new HashMap<String, String>();
        errors.put("payment", exception.getMessage());
        log.warn("Payment error: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(errors));
    }
}
