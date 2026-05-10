package com.mmenendez.microservices.notification_microservice.notification;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification management endpoints")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    @Operation(
        summary = "Send order confirmation notification",
        description = "Triggered internally via Kafka after a successful payment. Sends email and WhatsApp to the customer."
    )
    @ApiResponse(responseCode = "200", description = "Notification sent successfully")
    @ApiResponse(responseCode = "400", description = "Customer not found")
    public ResponseEntity<NotificationResponse> sendOrderConfirmation(@Valid @RequestBody NotificationRequest request) {
        return ResponseEntity.ok(notificationService.sendOrderConfirmation(request));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get all notifications for a customer")
    @ApiResponse(responseCode = "200", description = "List of notifications for the customer")
    @ApiResponse(responseCode = "400", description = "Customer not found")
    public ResponseEntity<List<NotificationResponse>> getNotificationsByCustomerId(
            @Parameter(description = "MongoDB ID of the customer") @PathVariable String customerId) {
        return ResponseEntity.ok(notificationService.getNotificationsByCustomerId(customerId));
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get all notifications for an order")
    @ApiResponse(responseCode = "200", description = "List of notifications for the order")
    @ApiResponse(responseCode = "400", description = "Order not found")
    public ResponseEntity<List<NotificationResponse>> getNotificationsByOrderId(
            @Parameter(description = "ID of the order") @PathVariable Long orderId) {
        return ResponseEntity.ok(notificationService.getNotificationsByOrderId(orderId));
    }
}
