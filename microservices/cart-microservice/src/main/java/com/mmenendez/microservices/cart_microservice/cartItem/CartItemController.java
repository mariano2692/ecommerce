package com.mmenendez.microservices.cart_microservice.cartItem;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/{customerId}/cart/items")
@RequiredArgsConstructor
@Tag(name = "Cart Items", description = "Cart item management endpoints")
public class CartItemController {

    private final CartItemService cartItemService;

    @PostMapping
    @Operation(summary = "Add item to cart", description = "Validates customer and product existence and stock before adding")
    @ApiResponse(responseCode = "200", description = "Item added, returns cart ID")
    @ApiResponse(responseCode = "400", description = "Customer or product not found, or insufficient stock")
    public ResponseEntity<String> addItemToCart(
            @PathVariable("customerId") String customerId,
            @Valid @RequestBody CartItemRequest cartItemRequest) {
        return ResponseEntity.ok(cartItemService.addItemToCart(customerId, cartItemRequest));
    }

    @PutMapping
    @Operation(summary = "Update item quantity in cart")
    @ApiResponse(responseCode = "202", description = "Item quantity updated")
    @ApiResponse(responseCode = "400", description = "Cart or product not found, or insufficient stock")
    public ResponseEntity<Void> updateItemFromCart(
            @PathVariable("customerId") String customerId,
            @Valid @RequestBody CartItemRequest cartItemRequest) {
        cartItemService.updateItemFromCart(customerId, cartItemRequest);
        return ResponseEntity.accepted().build();
    }

    @DeleteMapping("/{variantId}")
    @Operation(summary = "Remove item from cart")
    @ApiResponse(responseCode = "202", description = "Item removed from cart")
    @ApiResponse(responseCode = "400", description = "Cart or variant not found in cart")
    public ResponseEntity<Void> removeItemFromCart(
            @PathVariable("customerId") String customerId,
            @PathVariable("variantId") Integer variantId) {
        cartItemService.removeItemFromCart(customerId, variantId);
        return ResponseEntity.accepted().build();
    }
}
