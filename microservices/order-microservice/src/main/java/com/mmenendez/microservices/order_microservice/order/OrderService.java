package com.mmenendez.microservices.order_microservice.order;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.mmenendez.microservices.order_microservice.cart.CartClient;
import com.mmenendez.microservices.order_microservice.cart.CartResponse;
import com.mmenendez.microservices.order_microservice.customer.CustomerClient;
import com.mmenendez.microservices.order_microservice.exceptions.OrderException;
import com.mmenendez.microservices.order_microservice.orderItem.OrderItem;
import com.mmenendez.microservices.order_microservice.payment.PaymentClient;
import com.mmenendez.microservices.order_microservice.payment.PaymentRequest;
import com.mmenendez.microservices.order_microservice.product.ProductClient;
import com.mmenendez.microservices.order_microservice.product.ProductPurchaseRequest;
import com.mmenendez.microservices.order_microservice.product.ProductResponse;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.mmenendez.microservices.order_microservice.exceptions.UnauthorizedException;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final CustomerClient customerClient;
    private final ProductClient productClient;
    private final CartClient cartClient;
    private final PaymentClient paymentClient;

    private static final String ROLE_ADMIN = "ADMIN";

    private void requireSelfOrAdmin(String resourceCustomerId, String requestingCustomerId, String requestingRole) {
        if (!ROLE_ADMIN.equals(requestingRole) && !resourceCustomerId.equals(requestingCustomerId)) {
            throw new UnauthorizedException("Access denied to this resource");
        }
    }

    private void requireAdmin(String requestingRole) {
        if (!ROLE_ADMIN.equals(requestingRole)) {
            throw new UnauthorizedException("Admin access required");
        }
    }

    @Transactional
    public Long checkout(String customerId, CheckoutRequest checkoutRequest, String requestingCustomerId) {
        if (!customerId.equals(requestingCustomerId)) {
            throw new UnauthorizedException("Access denied to this resource");
        }
        
        customerClient.getCustomerById(customerId)
            .orElseThrow(() -> new OrderException("Customer with id " + customerId + " does not exist"));

        CartResponse cart = cartClient.getCartByCustomerId(customerId);

        if (cart.cartItems() == null || cart.cartItems().isEmpty()) {
            throw new OrderException("Cart for customer with id " + customerId + " is empty");
        }

        // getVariantById solo trae los datos del producto (nombre, precio, sku).
        // La validación de stock NO se hace acá — purchaseProducts la hace dentro de una
        // transacción con lock pesimista, evitando race conditions entre checkouts concurrentes.
        List<OrderItem> orderItems = cart.cartItems().stream()
            .map(cartItem -> {
                ProductResponse variant = productClient.getVariantById(cartItem.variantId())
                    .orElseThrow(() -> new OrderException("Variant with id " + cartItem.variantId() + " does not exist"));

                return OrderItem.builder()
                    .variantId(variant.id())
                    .variantSku(variant.sku())
                    .productName(variant.productName())
                    .quantity(cartItem.quantity())
                    .unitPrice(variant.price())
                    .build();
            })
            .toList();

        double totalAmount = orderItems.stream()
            .mapToDouble(item -> item.getUnitPrice() * item.getQuantity())
            .sum();

        Order order = Order.builder()
            .customerId(customerId)
            .totalAmount(totalAmount)
            .items(orderItems)
            .build();

        orderItems.forEach(item -> item.setOrder(order));

        Order savedOrder = orderRepository.save(order);

        List<ProductPurchaseRequest> purchaseRequests = cart.cartItems().stream()
            .map(item -> new ProductPurchaseRequest(item.variantId(), item.quantity()))
            .toList();

        // purchaseProducts valida stock y lo descuenta dentro de una sola transacción con lock —
        // si hay stock insuficiente por una race condition, falla acá antes de tocar el pago
        try {
            productClient.purchaseProducts(purchaseRequests);
        } catch (Exception e) {
            log.error("Stock error for order id: {}. Reason: {}", savedOrder.getId(), e.getMessage());
            savedOrder.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(savedOrder);
            throw new OrderException("Insufficient stock for one or more products in order " + savedOrder.getId());
        }

        cartClient.clearCart(customerId);

        try {
            paymentClient.processPayment(new PaymentRequest(
                savedOrder.getId(),
                customerId,
                totalAmount,
                checkoutRequest.paymentMethod()
            ));
            savedOrder.setStatus(OrderStatus.CONFIRMED);
        } catch (Exception e) {
            log.error("Payment failed for order id: {}. Reason: {}", savedOrder.getId(), e.getMessage());
            productClient.restockProducts(purchaseRequests);
            savedOrder.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(savedOrder);
            throw new OrderException("Payment failed for order with id " + savedOrder.getId());
        }

        orderRepository.save(savedOrder);
        return savedOrder.getId();
    }

    public OrderResponse getOrderById(Long id, String requestingCustomerId, String requestingRole) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new OrderException("Order with id " + id + " does not exist"));
        requireSelfOrAdmin(order.getCustomerId(), requestingCustomerId, requestingRole);
        return orderMapper.toOrderResponse(order);
    }

    public List<OrderResponse> getOrdersByCustomerId(String customerId, String requestingCustomerId, String requestingRole) {
        requireSelfOrAdmin(customerId, requestingCustomerId, requestingRole);
        customerClient.getCustomerById(customerId)
            .orElseThrow(() -> new OrderException("Customer with id " + customerId + " does not exist"));
        return orderRepository.findByCustomerId(customerId).stream()
            .map(orderMapper::toOrderResponse)
            .toList();
    }

    public Page<OrderResponse> getAllOrders(int page, int size, String requestingRole) {
        requireAdmin(requestingRole);
        return orderRepository.findAll(PageRequest.of(page, size))
            .map(orderMapper::toOrderResponse);
    }

    @Transactional
    public void cancelOrder(Long id, String requestingCustomerId, String requestingRole) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new OrderException("Order with id " + id + " does not exist"));
        requireSelfOrAdmin(order.getCustomerId(), requestingCustomerId, requestingRole);

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new OrderException("Order with id " + id + " is already cancelled");
        }

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new OrderException("Order with id " + id + " cannot be cancelled because it has already been delivered");
        }

        List<ProductPurchaseRequest> restockRequests = order.getItems().stream()
            .map(item -> new ProductPurchaseRequest(item.getVariantId(), item.getQuantity()))
            .toList();
        productClient.restockProducts(restockRequests);

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }
}
