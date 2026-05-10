package com.mmenendez.microservices.customer_microservice.customer;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.mmenendez.microservices.customer_microservice.exceptions.CustomerAlreadyExistsException;
import com.mmenendez.microservices.customer_microservice.exceptions.CustomerNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository repository;
    private final CustomerMapper mapper;

    public String saveCustomer(CustomerRequest request) {
        repository.findByEmail(request.email()).ifPresent(existing -> {
            // En update, el email puede ser el mismo del propio customer — eso está permitido
            if (request.id() == null || !existing.getId().equals(request.id())) {
                throw new CustomerAlreadyExistsException("Email " + request.email() + " is already in use");
            }
        });
        return repository.save(mapper.toCustomer(request)).getId();
    }

    public CustomerResponse getCustomerById(String customerId) {
        return repository
            .findById(customerId)
            .map(mapper::toCustomerResponse)
            .orElseThrow(() -> new CustomerNotFoundException(
                String.format("Customer with id %s not found", customerId)));
    }

    public Page<CustomerResponse> getCustomers(int page, int size) {
        return repository
            .findAll(PageRequest.of(page, size))
            .map(mapper::toCustomerResponse);
    }

    public void deleteCustomerById(String customerId) {
        repository
            .findById(customerId)
            .orElseThrow(() -> new CustomerNotFoundException(
                String.format("Customer with id %s not found", customerId)));
        repository.deleteById(customerId);
    }
}
