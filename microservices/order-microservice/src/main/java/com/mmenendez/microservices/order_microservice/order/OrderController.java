package com.mmenendez.microservices.order_microservice.order;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management endpoints")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout/{customerId}")
    @Operation(
        summary = "Checkout cart",
        description = "Creates an order from the customer's cart, processes payment and clears the cart. Returns the order ID."
    )
    @ApiResponse(responseCode = "200", description = "Order created and payment confirmed, returns order ID")
    @ApiResponse(responseCode = "400", description = "Customer not found, cart empty, insufficient stock or payment failed")
    public ResponseEntity<Long> checkout(
            @PathVariable String customerId,
            @Valid @RequestBody CheckoutRequest checkoutRequest,
            @RequestHeader("X-Customer-Id") String requestingCustomerId) {
        return ResponseEntity.ok(orderService.checkout(customerId, checkoutRequest, requestingCustomerId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    @ApiResponse(responseCode = "200", description = "Order found")
    @ApiResponse(responseCode = "400", description = "Order not found")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable Long id,
            @RequestHeader("X-Customer-Id") String requestingCustomerId,
            @RequestHeader("X-Customer-Role") String requestingRole) {
        return ResponseEntity.ok(orderService.getOrderById(id, requestingCustomerId, requestingRole));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get all orders for a customer")
    @ApiResponse(responseCode = "400", description = "Customer not found")
    public ResponseEntity<List<OrderResponse>> getOrdersByCustomerId(
            @PathVariable String customerId,
            @RequestHeader("X-Customer-Id") String requestingCustomerId,
            @RequestHeader("X-Customer-Role") String requestingRole) {
        return ResponseEntity.ok(orderService.getOrdersByCustomerId(customerId, requestingCustomerId, requestingRole));
    }

    @GetMapping
    @Operation(summary = "Get all orders")
    public ResponseEntity<Page<OrderResponse>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader("X-Customer-Role") String requestingRole) {
        return ResponseEntity.ok(orderService.getAllOrders(page, size, requestingRole));
    }

    @DeleteMapping("/{id}/cancel")
    @Operation(summary = "Cancel an order", description = "Only PENDING or CONFIRMED orders can be cancelled")
    @ApiResponse(responseCode = "204", description = "Order cancelled")
    @ApiResponse(responseCode = "400", description = "Order not found, already cancelled or already delivered")
    public ResponseEntity<Void> cancelOrder(
            @PathVariable Long id,
            @RequestHeader("X-Customer-Id") String requestingCustomerId,
            @RequestHeader("X-Customer-Role") String requestingRole) {
        orderService.cancelOrder(id, requestingCustomerId, requestingRole);
        return ResponseEntity.noContent().build();
    }
}
