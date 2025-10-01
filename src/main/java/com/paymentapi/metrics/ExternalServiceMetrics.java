package com.paymentapi.metrics;

import com.paymentapi.service.MetricsService;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;

/**
 * Métricas específicas para integrações com serviços externos.
 * Monitora chamadas a serviços de autorização e notificação.
 */
@Component
public class ExternalServiceMetrics {

    private static final String EXTERNAL_SERVICE_CALLS = "payment_external_service_calls_total";
    private static final String EXTERNAL_SERVICE_FAILURES = "payment_external_service_failures_total";
    private static final String EXTERNAL_SERVICE_DURATION = "payment_external_service_duration_seconds";

    private final MetricsService metricsService;

    public ExternalServiceMetrics(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    /**
     * Registra uma chamada a serviço externo.
     *
     * @param serviceName Nome do serviço (authorization, notification)
     * @param status Status da chamada (success, failure, timeout)
     */
    public void recordServiceCall(String serviceName, String status) {
        metricsService.incrementCounter(EXTERNAL_SERVICE_CALLS, Map.of(
            "service", serviceName,
            "status", status
        ));
    }

    /**
     * Registra uma falha em chamada a serviço externo.
     *
     * @param serviceName Nome do serviço
     * @param reason Razão da falha (timeout, connection_error, etc)
     */
    public void recordServiceFailure(String serviceName, String reason) {
        metricsService.incrementCounter(EXTERNAL_SERVICE_FAILURES, Map.of(
            "service", serviceName,
            "reason", reason
        ));
    }

    /**
     * Registra a duração de uma chamada a serviço externo.
     *
     * @param serviceName Nome do serviço
     * @param duration Duração da chamada
     * @param status Status final da chamada
     */
    public void recordServiceDuration(String serviceName, Duration duration, String status) {
        metricsService.recordTiming(EXTERNAL_SERVICE_DURATION, duration, Map.of(
            "service", serviceName,
            "status", status
        ));
    }
}
