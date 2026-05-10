package com.mmenendez.microservices.cart_microservice.cartItem;

import java.util.ArrayList;

import org.springframework.stereotype.Service;

import com.mmenendez.microservices.cart_microservice.cart.Cart;
import com.mmenendez.microservices.cart_microservice.cart.CartRepository;
import com.mmenendez.microservices.cart_microservice.customer.CustomerClient;
import com.mmenendez.microservices.cart_microservice.customer.CustomerResponse;
import com.mmenendez.microservices.cart_microservice.exceptions.CartException;
import com.mmenendez.microservices.cart_microservice.products.ProductClient;
import com.mmenendez.microservices.cart_microservice.products.ProductResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartItemService {

    private final CartRepository cartRepository;
    private final CustomerClient customerClient;    
    private final ProductClient productClient;

    public String addItemToCart(String customerId, CartItemRequest cartItemRequest) {

        CustomerResponse customerResponse = customerClient.getCustomerById(customerId)
            .orElseThrow(() -> new CartException("Customer with id " + customerId + " does not exist"));

        ProductResponse variant = productClient.getVariantById(cartItemRequest.variantId())
            .orElseThrow(() -> new CartException("Variant with id " + cartItemRequest.variantId() + " does not exist"));

        if (variant.stock() < cartItemRequest.quantity()) {
            throw new CartException("Variant with id " + cartItemRequest.variantId() + " does not have enough stock");
        }

        Cart cart = cartRepository.findByCustomerId(customerResponse.id())
            .orElse(Cart.builder()
                .customerId(customerId)
                .items(new ArrayList<>())
                .build()
            );

        boolean variantExists = cart.getItems().stream()
            .anyMatch(item -> item.getVariantId().equals(cartItemRequest.variantId()));

        if (variantExists) {
            throw new CartException("Variant with id " + cartItemRequest.variantId() + " is already in the cart");
        }

        cart.getItems().add(
            CartItem.builder()
                .variantId(cartItemRequest.variantId())
                .quantity(cartItemRequest.quantity())
                .build()
        );

        cartRepository.save(cart);
        return cart.getId();
    }

    public void updateItemFromCart(String customerId, CartItemRequest cartItemRequest) {

        Cart cart = cartRepository.findByCustomerId(customerId)
            .orElseThrow(() -> new CartException("Cart for customer with id " + customerId + " does not exist"));

        CartItem itemToUpdate = cart.getItems().stream()
            .filter(item -> item.getVariantId().equals(cartItemRequest.variantId()))
            .findFirst()
            .orElseThrow(() -> new CartException("Variant with id " + cartItemRequest.variantId() + " is not in the cart"));

        ProductResponse variant = productClient.getVariantById(cartItemRequest.variantId())
            .orElseThrow(() -> new CartException("Variant with id " + cartItemRequest.variantId() + " does not exist"));

        if (variant.stock() < cartItemRequest.quantity()) {
            throw new CartException("Variant with id " + cartItemRequest.variantId() + " does not have enough stock");
        }

        itemToUpdate.setQuantity(cartItemRequest.quantity());
        cartRepository.save(cart);
    }

    public void removeItemFromCart(String customerId, Integer variantId) {
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new CartException("Cart for customer with id " + customerId + " does not exist"));

        CartItem itemToRemove = cart.getItems().stream()
            .filter(item -> item.getVariantId().equals(variantId))
            .findFirst()
            .orElseThrow(() -> new CartException("Variant with id " + variantId + " is not in the cart"));

        cart.getItems().remove(itemToRemove);
        cartRepository.save(cart);
    }
}
