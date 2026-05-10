package com.mmenendez.api_gateway.security;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Intercepts every request before Spring Security to enforce two responsibilities:
 * sanitize untrusted headers and propagate verified identity to downstream services.
 *
 * Runs at order -1 so it executes before the Spring Security filter chain, which means
 * downstream services can trust that X-Customer-* headers were set by this gateway
 * and never by the caller.
 */
@Component
@RequiredArgsConstructor
public class AuthHeaderFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // A caller could forge X-Customer-Id or X-Customer-Role to impersonate another
        // user or escalate privileges. Stripping them here makes the gateway the only
        // authority that can set these headers.
        ServerHttpRequest sanitizedRequest = exchange.getRequest().mutate()
                .headers(headers -> {
                    headers.remove("X-Customer-Id");
                    headers.remove("X-Customer-Role");
                })
                .build();
        ServerWebExchange sanitizedExchange = exchange.mutate().request(sanitizedRequest).build();

        String authHeader = sanitizedRequest.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // No token present — Spring Security will enforce authorization rules downstream.
            // Public routes (e.g. /auth/**) will pass; protected routes will receive a 401.
            return chain.filter(sanitizedExchange);
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.isValid(token)) {
            // Fail fast: an expired or tampered token is rejected here rather than letting
            // it propagate deeper into the filter chain. Defense in depth — Spring Security
            // would also catch it, but short-circuiting avoids unnecessary processing.
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // Translate the verified JWT into internal headers so microservices can identify
        // the caller without parsing or trusting JWTs themselves.
        Claims claims = jwtUtil.extractClaims(token);
        ServerHttpRequest mutatedRequest = sanitizedRequest.mutate()
                .header("X-Customer-Id", claims.getSubject())
                .header("X-Customer-Role", claims.get("role", String.class))
                .build();
        return chain.filter(sanitizedExchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        // Negative value guarantees execution before the Spring Security filter chain
        // (which runs at order 0). Without this, Security might evaluate access rules
        // before headers are populated, causing false 403s on valid requests.
        return -1;
    }
}
