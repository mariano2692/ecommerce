package com.mmenendez.microservices.customer_microservice.auth;

import com.mmenendez.microservices.customer_microservice.customer.Customer;
import com.mmenendez.microservices.customer_microservice.customer.CustomerRepository;
import com.mmenendez.microservices.customer_microservice.customer.Role;
import com.mmenendez.microservices.customer_microservice.exceptions.CustomerAlreadyExistsException;
import com.mmenendez.microservices.customer_microservice.exceptions.CustomerNotFoundException;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        if (customerRepository.findByEmail(request.email()).isPresent()) {
            throw new CustomerAlreadyExistsException("Email already registered");
        }

        Customer customer = Customer.builder()
            .firstName(request.firstName())
            .lastName(request.lastName())
            .email(request.email())
            .password(passwordEncoder.encode(request.password()))
            .phone(request.phone())
            .address(request.address())
            .city(request.city())
            .role(Role.USER)
            .build();

        Customer saved = customerRepository.save(customer);
        String token = jwtUtil.generateToken(saved.getId(), saved.getEmail(), saved.getRole().name());
        return new AuthResponse(token, saved.getId());
    }

    public AuthResponse login(LoginRequest request) {
        Customer customer = customerRepository.findByEmail(request.email())
            .orElseThrow(() -> new CustomerNotFoundException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), customer.getPassword())) {
            throw new CustomerNotFoundException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(customer.getId(), customer.getEmail(), customer.getRole().name());
        return new AuthResponse(token, customer.getId());
    }
}
