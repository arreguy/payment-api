package com.paymentapi.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

/**
 * DTO pra resposta de health check com records.
 */
public record HealthResponse(
    String status,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Map<String, HealthComponent> components
) {

    /**
     * Cria uma resposta com status UP
     */
    public static HealthResponse up() {
        return new HealthResponse("UP", null);
    }

    /**
     * Cria uma resposta com status DOWN
     */
    public static HealthResponse down() {
        return new HealthResponse("DOWN", null);
    }

    /**
     * Cria uma resposta com status e componentes customizados
     */
    public static HealthResponse withComponents(String status, Map<String, HealthComponent> components) {
        return new HealthResponse(status, components);
    }

    /**
     * Representa um componente individual do health check (tipo database, aplicação, etc)
     */
    public record HealthComponent(
        String status,
        @JsonInclude(JsonInclude.Include.NON_NULL) // Só inclui os detalhes se tiver
        Map<String, Object> details
    ) {

        /**
         * Cria um componente com status UP
         */
        public static HealthComponent up() {
            return new HealthComponent("UP", null);
        }

        /**
         * Cria um componente com status DOWN
         */
        public static HealthComponent down() {
            return new HealthComponent("DOWN", null);
        }

        /**
         * Cria um componente com status e detalhes customizados
         */
        public static HealthComponent withDetails(String status, Map<String, Object> details) {
            return new HealthComponent(status, details);
        }
    }
}