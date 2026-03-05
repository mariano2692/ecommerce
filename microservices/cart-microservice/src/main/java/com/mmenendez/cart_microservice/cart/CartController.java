package com.mmenendez.cart_microservice.cart;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/{customerId}/cart/")
@RequiredArgsConstructor
public class CartController {

}
