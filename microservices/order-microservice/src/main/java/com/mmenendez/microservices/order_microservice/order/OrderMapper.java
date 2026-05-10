package com.mmenendez.microservices.order_microservice.order;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mmenendez.microservices.order_microservice.orderItem.OrderItem;
import com.mmenendez.microservices.order_microservice.orderItem.OrderItemResponse;

@Service
public class OrderMapper {

    public OrderResponse toOrderResponse(Order order) {
        return new OrderResponse(
            order.getId(),
            order.getCustomerId(),
            order.getStatus(),
            order.getCreatedAt(),
            order.getTotalAmount(),
            toOrderItemResponseList(order.getItems())
        );
    }

    private List<OrderItemResponse> toOrderItemResponseList(List<OrderItem> items) {
        if (items == null) return List.of();
        return items.stream()
            .map(item -> new OrderItemResponse(
                item.getId(),
                item.getVariantId(),
                item.getVariantSku(),
                item.getProductName(),
                item.getQuantity(),
                item.getUnitPrice()
            ))
            .toList();
    }
}
