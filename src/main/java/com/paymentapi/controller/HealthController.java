package com.paymentapi.controller;

import com.paymentapi.dto.response.HealthResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthContributor;
import org.springframework.boot.actuate.health.HealthContributorRegistry;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

@RestController
@RequestMapping("/health")
public class HealthController {

    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);

    private final HealthContributorRegistry healthContributorRegistry;

    public HealthController(HealthContributorRegistry healthContributorRegistry) {
        this.healthContributorRegistry = healthContributorRegistry;
    }

    @GetMapping("/live")
    public ResponseEntity<HealthResponse> live() {
        return performHealthCheck("liveness", this::checkLiveness);
    }

    @GetMapping("/ready")
    public ResponseEntity<HealthResponse> ready() {
        return performHealthCheck("readiness", () -> checkComponents("db", "application"));
    }

    @GetMapping("/startup")
    public ResponseEntity<HealthResponse> startup() {
        return performHealthCheck("startup", () -> checkComponents("application"));
    }

    private ResponseEntity<HealthResponse> performHealthCheck(String checkType, Supplier<ResponseEntity<HealthResponse>> healthCheckSupplier) {
        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);
        try {
            logger.info("Health {} check initiated", checkType);
            ResponseEntity<HealthResponse> response = healthCheckSupplier.get();
            logger.info("Health {} check completed - status: {}", checkType, response.getBody() != null ? response.getBody().status() : "UNKNOWN");
            return response;
        } finally {
            MDC.remove("correlationId");
        }
    }

    private ResponseEntity<HealthResponse> checkComponents(String... componentNames) {
        Map<String, HealthResponse.HealthComponent> components = new HashMap<>();
        boolean isOverallHealthy = true;

        for (String componentName : componentNames) {
            HealthComponent health = getHealthComponent(componentName);
            boolean isComponentUp = health != null && Status.UP.equals(getHealthStatus(health));

            String status = isComponentUp ? "UP" : "DOWN";
            Map<String, Object> details = extractDetails(health);
            components.put(componentName, HealthResponse.HealthComponent.withDetails(status, details));

            isOverallHealthy &= isComponentUp;
        }

        String overallStatus = isOverallHealthy ? "UP" : "DOWN";
        HttpStatus httpStatus = isOverallHealthy ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;

        return ResponseEntity.status(httpStatus)
            .body(HealthResponse.withComponents(overallStatus, components));
    }

    private ResponseEntity<HealthResponse> checkLiveness() {
        Map<String, HealthResponse.HealthComponent> components = Map.of(
            "application", HealthResponse.HealthComponent.withDetails("UP", Map.of(
                "service", "payment-api",
                "status", "running"
                                                                                  ))
                                                                       );
        return ResponseEntity.ok(HealthResponse.withComponents("UP", components));
    }

    private HealthComponent getHealthComponent(String name) {
        try {
            HealthContributor contributor = healthContributorRegistry.getContributor(name);
            if (contributor instanceof HealthIndicator) {
                return ((HealthIndicator) contributor).health();
            }
            logger.warn("Health contributor '{}' is not a HealthIndicator", name);
            return null;
        } catch (Exception e) {
            logger.warn("Failed to get health component '{}'", name, e);
            return null;
        }
    }

    private Status getHealthStatus(HealthComponent health) {
        if (health instanceof Health h) {
            return h.getStatus();
        }
        return Status.UNKNOWN;
    }

    private Map<String, Object> extractDetails(HealthComponent health) {
        if (health instanceof Health h) {
            return new HashMap<>(h.getDetails());
        }
        return Map.of();
    }
}
