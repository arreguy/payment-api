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

@Component("externalServices")
public class ExternalServiceHealthIndicator implements HealthIndicator {

    private static final Logger logger = LoggerFactory.getLogger(ExternalServiceHealthIndicator.class);
    private static final String AUTHORIZATION_SERVICE_URL = "https://util.devi.tools/api/v2/authorize";
    private static final int TIMEOUT_MS = 5000; // 5 seconds as per Dev Notes

    private final RestTemplate restTemplate;

    public ExternalServiceHealthIndicator() {
        // Configure RestTemplate with proper timeouts
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(TIMEOUT_MS);
        factory.setReadTimeout(TIMEOUT_MS);

        this.restTemplate = new RestTemplate(factory);
        this.restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().add("User-Agent", "PaymentAPI-HealthCheck/1.0");
            return execution.execute(request, body);
        });
    }

    @Override
    public Health health() {
        Health.Builder authServiceHealth = checkAuthorizationService();

        // Aggregate external service health
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

    private Health.Builder checkAuthorizationService() {
        try {
            Instant start = Instant.now();

            // Simple HEAD request to check if service is reachable
            // We use a simple GET as the authorization endpoint might not support HEAD
            try {
                restTemplate.getForEntity(AUTHORIZATION_SERVICE_URL, String.class);
                Duration responseTime = Duration.between(start, Instant.now());

                return Health.up()
                    .withDetail("url", AUTHORIZATION_SERVICE_URL)
                    .withDetail("status", "reachable")
                    .withDetail("responseTime", responseTime.toMillis() + "ms");

            } catch (Exception e) {
                Duration responseTime = Duration.between(start, Instant.now());
                logger.warn("Authorization service health check failed: {}", e.getMessage());

                return Health.down()
                    .withDetail("url", AUTHORIZATION_SERVICE_URL)
                    .withDetail("status", "unreachable")
                    .withDetail("responseTime", responseTime.toMillis() + "ms")
                    .withDetail("error", e.getMessage());
            }

        } catch (Exception e) {
            logger.error("External service health check failed", e);
            return Health.down()
                .withDetail("url", AUTHORIZATION_SERVICE_URL)
                .withDetail("status", "error")
                .withDetail("error", e.getMessage());
        }
    }
}