package com.paymentapi.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

public record HealthResponse(
    String status,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Map<String, HealthComponent> components
) {

    public static HealthResponse up() {
        return new HealthResponse("UP", null);
    }

    public static HealthResponse down() {
        return new HealthResponse("DOWN", null);
    }

    public static HealthResponse withComponents(String status, Map<String, HealthComponent> components) {
        return new HealthResponse(status, components);
    }

    public record HealthComponent(
        String status,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        Map<String, Object> details
    ) {

        public static HealthComponent up() {
            return new HealthComponent("UP", null);
        }

        public static HealthComponent down() {
            return new HealthComponent("DOWN", null);
        }

        public static HealthComponent withDetails(String status, Map<String, Object> details) {
            return new HealthComponent(status, details);
        }
    }
}