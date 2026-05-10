package com.mmenendez.api_gateway.security;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtSecurityContextRepository jwtSecurityContextRepository;

    // Comma-separated list to support multiple frontends (e.g. web + mobile web).
    // Never use "*" in production: wildcards disallow credentials and leak which
    // origins are trusted to anyone inspecting the response headers.
    @Value("${cors.allowed-origins:http://localhost:3000}")
    private String allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // X-Customer-Id and X-Customer-Role are internal headers set by AuthHeaderFilter.
        // Exposing them here allows the frontend to read them from responses if needed,
        // though microservices should never accept them directly from external callers.
        config.setAllowedHeaders(Arrays.asList(
                HttpHeaders.AUTHORIZATION,
                HttpHeaders.CONTENT_TYPE,
                "X-Customer-Id",
                "X-Customer-Role"
        ));

        // Required so browsers include the Authorization header in cross-origin requests.
        // Without this, the JWT token is stripped by the browser before reaching the gateway.
        config.setAllowCredentials(true);

        // Browsers cache the preflight response for this duration (seconds).
        // 3600 reduces OPTIONS round-trips without holding stale policy too long
        // if origins change during a deploy.
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
            .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
            .securityContextRepository(jwtSecurityContextRepository)
            .authorizeExchange(exchanges -> exchanges
                // Auth endpoints are intentionally public; JWT validation inside them
                // is handled by CustomerService, not by the gateway.
                .pathMatchers("/api/v1/auth/**").permitAll()
                .pathMatchers("GET", "/api/v1/customers").hasRole("ADMIN")
                .pathMatchers("DELETE", "/api/v1/customers/**").hasRole("ADMIN")
                .anyExchange().authenticated()
            )
            .build();
    }
}
