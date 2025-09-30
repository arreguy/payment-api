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

/**
 * Controller responsável pelos endpoints de health check.
 * Endpoints /health/live, /health/ready e /health/startup
 * para serem consumidos pelo Kubernetes
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);

    // Registro com todos os health indicators da aplicação
    private final HealthContributorRegistry healthContributorRegistry;

    public HealthController(HealthContributorRegistry healthContributorRegistry) {
        this.healthContributorRegistry = healthContributorRegistry;
    }

    /**
     * Endpoint de liveness - verifica se a aplicação tá rodando.
     * O Kubernetes usa esse endpoint pra saber se precisa reiniciar o pod.
     */
    @GetMapping("/live")
    public ResponseEntity<HealthResponse> live() {
        return performHealthCheck("liveness", this::checkLiveness);
    }

    /**
     * Endpoint de readiness - verifica se a aplicação está pronta pra receber requisições.
     * Checa se o banco de dados e a aplicação tão OK.
     */
    @GetMapping("/ready")
    public ResponseEntity<HealthResponse> ready() {
        return performHealthCheck("readiness", () -> checkComponents("db", "application"));
    }

    /**
     * Endpoint de startup - verifica se a aplicação terminou de inicializar.
     */
    @GetMapping("/startup")
    public ResponseEntity<HealthResponse> startup() {
        return performHealthCheck("startup", () -> checkComponents("application"));
    }

    /**
     * Método genérico pra executar health checks com logging
     */
    private ResponseEntity<HealthResponse> performHealthCheck(String checkType, Supplier<ResponseEntity<HealthResponse>> healthCheckSupplier) {
        // Gera um correlation ID pra rastrear a requisição
        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);
        try {
            logger.info("Health {} check initiated", checkType);
            // Executa o health check
            ResponseEntity<HealthResponse> response = healthCheckSupplier.get();
            logger.info("Health {} check completed - status: {}", checkType, response.getBody() != null ? response.getBody().status() : "UNKNOWN");
            return response;
        } finally {
            // Limpa o correlation ID do MDC
            MDC.remove("correlationId");
        }
    }

    /**
     * Verifica vários componentes e retorna o status geral
     */
    private ResponseEntity<HealthResponse> checkComponents(String... componentNames) {
        Map<String, HealthResponse.HealthComponent> components = new HashMap<>();
        boolean isOverallHealthy = true;

        // Percorre cada componente pra verificar
        for (String componentName : componentNames) {
            HealthComponent health = getHealthComponent(componentName);
            boolean isComponentUp = health != null && Status.UP.equals(getHealthStatus(health));

            // Monta o status do componente
            String status = isComponentUp ? "UP" : "DOWN";
            Map<String, Object> details = extractDetails(health);
            components.put(componentName, HealthResponse.HealthComponent.withDetails(status, details));

            // Se algum componente tá DOWN, o geral fica DOWN também
            isOverallHealthy &= isComponentUp;
        }

        // Define o status geral e o HTTP status code
        String overallStatus = isOverallHealthy ? "UP" : "DOWN";
        HttpStatus httpStatus = isOverallHealthy ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;

        return ResponseEntity.status(httpStatus)
            .body(HealthResponse.withComponents(overallStatus, components));
    }

    /**
     * Verifica o liveness (só vê se a aplicação tá respondendo)
     */
    private ResponseEntity<HealthResponse> checkLiveness() {
        // Liveness é sempre UP se chegou aqui (significa que a aplicação tá respondendo)
        Map<String, HealthResponse.HealthComponent> components = Map.of(
            "application", HealthResponse.HealthComponent.withDetails("UP", Map.of(
                "service", "payment-api",
                "status", "running"
            ))
        );
        return ResponseEntity.ok(HealthResponse.withComponents("UP", components));
    }

    /**
     * Pega um componente de health do registry
     */
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

    /**
     * Extrai o status de um health component
     */
    private Status getHealthStatus(HealthComponent health) {
        if (health instanceof Health h) {
            return h.getStatus();
        }
        return Status.UNKNOWN;
    }

    /**
     * Extrai os detalhes de um health component
     */
    private Map<String, Object> extractDetails(HealthComponent health) {
        if (health instanceof Health h) {
            return new HashMap<>(h.getDetails());
        }
        return Map.of();
    }
}
