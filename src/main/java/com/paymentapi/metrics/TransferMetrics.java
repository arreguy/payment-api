package com.paymentapi.metrics;

import com.paymentapi.service.MetricsService;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;

/**
 * Métricas específicas para operações de transferência.
 * Fornece contadores e timers para monitoramento do fluxo de transferências.
 */
@Component
public class TransferMetrics {

    private static final String TRANSFER_TOTAL = "payment_transfer_total";
    private static final String TRANSFER_FAILED = "payment_transfer_failed_total";
    private static final String TRANSFER_DURATION = "payment_transfer_duration_seconds";

    private final MetricsService metricsService;

    public TransferMetrics(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    /**
     * Registra uma transferência total realizada.
     *
     * @param status Status da transferência (success, failed, etc)
     */
    public void recordTransferTotal(String status) {
        metricsService.incrementCounter(TRANSFER_TOTAL, Map.of(
            "service", "transfer-service",
            "operation", "execute-transfer",
            "status", status
        ));
    }

    /**
     * Registra uma transferência falhada.
     *
     * @param reason Razão da falha (insufficient_funds, authorization_denied, etc)
     */
    public void recordTransferFailed(String reason) {
        metricsService.incrementCounter(TRANSFER_FAILED, Map.of(
            "service", "transfer-service",
            "operation", "execute-transfer",
            "reason", reason
        ));
    }

    /**
     * Registra a duração de uma operação de transferência.
     *
     * @param duration Duração da operação
     * @param status Status final da transferência
     */
    public void recordTransferDuration(Duration duration, String status) {
        metricsService.recordTiming(TRANSFER_DURATION, duration, Map.of(
            "service", "transfer-service",
            "operation", "execute-transfer",
            "status", status
        ));
    }
}
