package com.mmenendez.microservices.notification_microservice.customer;

import java.util.Optional;

import org.springframework.stereotype.Component;

@Component
public class CustomerClientFallback implements CustomerClient {

    @Override
    public Optional<CustomerResponse> getCustomerById(String customerId) {
        return Optional.empty();
    }
}
