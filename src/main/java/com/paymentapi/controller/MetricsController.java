package com.paymentapi.controller;

import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller responsável pelo endpoint de métricas Prometheus.
 * Expõe as métricas da aplicação no /v1/metrics.
 */
@RestController
@RequestMapping("/v1")
public class MetricsController {

    private final PrometheusMeterRegistry prometheusMeterRegistry;

    public MetricsController(PrometheusMeterRegistry prometheusMeterRegistry) {
        this.prometheusMeterRegistry = prometheusMeterRegistry;
    }

    /**
     * Endpoint que retorna todas as métricas do Prometheus.
     * @return ResponseEntity com métricas do Prometheus
     */
    @GetMapping(value = "/metrics", produces = "text/plain; version=0.0.4; charset=utf-8")
    public ResponseEntity<String> metrics() {
        String prometheusMetrics = prometheusMeterRegistry.scrape();
        return ResponseEntity
            .ok()
            .contentType(MediaType.valueOf("text/plain; version=0.0.4; charset=utf-8"))
            .body(prometheusMetrics);
    }
}
