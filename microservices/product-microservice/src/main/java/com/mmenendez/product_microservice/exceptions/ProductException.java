package com.mmenendez.product_microservice.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ProductException extends RuntimeException {
    private final String message;
}
