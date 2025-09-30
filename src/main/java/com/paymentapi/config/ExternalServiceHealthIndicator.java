package com.paymentapi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.time.Duration;
import java.time.Instant;

/**
 * Indicador de saúde dos serviços externos.
 * Verifica se os serviços externos (API de autorização por exemplo) estão respondendo.
 */
@Component("externalServices")
public class ExternalServiceHealthIndicator implements HealthIndicator {

    private static final Logger logger = LoggerFactory.getLogger(ExternalServiceHealthIndicator.class);
    private static final String AUTHORIZATION_SERVICE_URL = "https://util.devi.tools/api/v2/authorize";
    private static final int TIMEOUT_MS = 5000; // Timeout de 5 segundos

    private final RestTemplate restTemplate;

    public ExternalServiceHealthIndicator() {
        // Configura o RestTemplate com timeout
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(TIMEOUT_MS);
        factory.setReadTimeout(TIMEOUT_MS);

        this.restTemplate = new RestTemplate(factory);
        // Adiciona um interceptor pra colocar o User-Agent customizado
        this.restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().add("User-Agent", "PaymentAPI-HealthCheck/1.0");
            return execution.execute(request, body);
        });
    }

    /**
     * Verifica a saúde dos serviços externos
     */
    @Override
    public Health health() {
        // Checa o serviço de autorização
        Health.Builder authServiceHealth = checkAuthorizationService();

        // Retorna UP ou DOWN dependendo do resultado
        if (authServiceHealth.build().getStatus().getCode().equals("UP")) {
            return Health.up()
                .withDetail("authorizationService", authServiceHealth.build().getDetails())
                .build();
        } else {
            return Health.down()
                .withDetail("authorizationService", authServiceHealth.build().getDetails())
                .build();
        }
    }

    /**
     * Verifica se o serviço de autorização tá respondendo
     */
    private Health.Builder checkAuthorizationService() {
        try {
            // Marca o horário de início pra calcular o tempo de resposta
            Instant start = Instant.now();

            try {
                // Tenta fazer uma requisição pro serviço
                restTemplate.getForEntity(AUTHORIZATION_SERVICE_URL, String.class);
                Duration responseTime = Duration.between(start, Instant.now());

                // Se chegou aqui, o serviço tá UP
                return Health.up()
                    .withDetail("url", AUTHORIZATION_SERVICE_URL)
                    .withDetail("status", "reachable")
                    .withDetail("responseTime", responseTime.toMillis() + "ms");

            } catch (Exception e) {
                // Se deu erro, o serviço tá DOWN
                Duration responseTime = Duration.between(start, Instant.now());
                logger.warn("Authorization service health check failed: {}", e.getMessage());

                return Health.down()
                    .withDetail("url", AUTHORIZATION_SERVICE_URL)
                    .withDetail("status", "unreachable")
                    .withDetail("responseTime", responseTime.toMillis() + "ms")
                    .withDetail("error", e.getMessage());
            }

        } catch (Exception e) {
            // Se deu algum erro inesperado
            logger.error("External service health check failed", e);
            return Health.down()
                .withDetail("url", AUTHORIZATION_SERVICE_URL)
                .withDetail("status", "error")
                .withDetail("error", e.getMessage());
        }
    }
}