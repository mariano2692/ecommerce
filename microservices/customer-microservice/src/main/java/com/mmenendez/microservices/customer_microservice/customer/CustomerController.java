package com.mmenendez.microservices.customer_microservice.customer;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Customers", description = "Customer management endpoints")
public class CustomerController {

    private final CustomerService service;

    @PostMapping
    @Operation(summary = "Create a new customer")
    @ApiResponse(responseCode = "200", description = "Customer created, returns customer ID")
    @ApiResponse(responseCode = "400", description = "Validation error in request fields")
    public ResponseEntity<String> createCustomer(@RequestBody @Valid CustomerRequest request) {
        return ResponseEntity.ok(service.saveCustomer(request));
    }

    @GetMapping("/{customerId}")
    @Operation(summary = "Get customer by ID")
    @ApiResponse(responseCode = "200", description = "Customer found")
    @ApiResponse(responseCode = "400", description = "Customer not found")
    public ResponseEntity<CustomerResponse> getCustomerById(
            @Parameter(description = "MongoDB ID of the customer") @PathVariable("customerId") String customerId) {
        return ResponseEntity.ok(service.getCustomerById(customerId));
    }

    @GetMapping
    @Operation(summary = "Get all customers (paginated)")
    @ApiResponse(responseCode = "200", description = "Page of customers")
    public ResponseEntity<Page<CustomerResponse>> getCustomers(
            @Parameter(description = "Page number, zero-based") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.getCustomers(page, size));
    }

    @PutMapping
    @Operation(summary = "Update an existing customer")
    @ApiResponse(responseCode = "202", description = "Customer updated")
    @ApiResponse(responseCode = "400", description = "Customer not found or validation error")
    public ResponseEntity<Void> updateCustomer(@RequestBody @Valid CustomerRequest request) {
        service.saveCustomer(request);
        return ResponseEntity.accepted().build();
    }

    @DeleteMapping("/{customerId}")
    @Operation(summary = "Delete customer by ID")
    @ApiResponse(responseCode = "202", description = "Customer deleted")
    @ApiResponse(responseCode = "400", description = "Customer not found")
    public ResponseEntity<Void> deleteCustomerById(
            @Parameter(description = "MongoDB ID of the customer") @PathVariable("customerId") String customerId) {
        service.deleteCustomerById(customerId);
        return ResponseEntity.accepted().build();
    }
}
