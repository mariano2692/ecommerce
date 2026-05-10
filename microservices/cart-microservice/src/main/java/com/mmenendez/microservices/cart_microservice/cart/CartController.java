package com.mmenendez.microservices.cart_microservice.cart;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/{customerId}/cart/")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Shopping cart management endpoints")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Get cart by customer ID")
    @ApiResponse(responseCode = "200", description = "Cart found")
    @ApiResponse(responseCode = "400", description = "Cart not found for this customer")
    public ResponseEntity<CartResponse> getCartByCustomerId(@PathVariable("customerId") String customerId) {
        return ResponseEntity.ok(cartService.getCartByCustomerId(customerId));
    }

    @DeleteMapping
    @Operation(summary = "Clear cart", description = "Removes all items from the customer's cart")
    @ApiResponse(responseCode = "204", description = "Cart cleared")
    @ApiResponse(responseCode = "400", description = "Cart not found for this customer")
    public ResponseEntity<Void> clearCart(@PathVariable("customerId") String customerId) {
        cartService.clearCart(customerId);
        return ResponseEntity.noContent().build();
    }
}
