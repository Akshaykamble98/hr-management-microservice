package com.company.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Duration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // HR Management Service Routes
                .route("hr-service", r -> r
                        .path("/api/v1/employees/**", "/api/v1/departments/**", 
                              "/api/v1/leaves/**", "/api/v1/attendances/**")
                        .filters(f -> f
                                        .circuitBreaker(config -> config
                                        .setName("hrServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/hr"))
                                .retry(retryConfig -> retryConfig
                                        .setRetries(3)
                                        .setBackoff(Duration.ofMillis(100), Duration.ofSeconds(2), 2, false)))
                        .uri("lb://hr-management-service"))

                // Payroll Service Routes
                .route("payroll-service", r -> r
                        .path("/api/v1/payroll/**", "/api/v1/salary/**")
                        .filters(f -> f
                                         .circuitBreaker(config -> config
                                        .setName("payrollServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/payroll"))
                                .retry(retryConfig -> retryConfig
                                        .setRetries(3)
                                        .setBackoff(Duration.ofMillis(100), Duration.ofSeconds(2), 2, false)))
                        .uri("lb://payroll-service"))

                // Auth Routes (No authentication needed)
                .route("auth-route", r -> r
                        .path("/api/v1/auth/**")
                        .filters(f -> f
                                .rewritePath("/api/v1/auth/(?<segment>.*)", "/api/v1/auth/${segment}"))
                        .uri("lb://hr-management-service"))

                .build();
    }
}
