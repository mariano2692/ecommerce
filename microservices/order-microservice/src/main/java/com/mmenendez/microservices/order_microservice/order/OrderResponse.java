package com.mmenendez.microservices.order_microservice.order;

import java.time.LocalDateTime;
import java.util.List;

import com.mmenendez.microservices.order_microservice.orderItem.OrderItemResponse;

public record OrderResponse(
    Long id,
    String customerId,
    OrderStatus status,
    LocalDateTime createdAt,
    Double totalAmount,
    List<OrderItemResponse> items
) {
}
