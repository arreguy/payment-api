package com.paymentapi.integration;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste de integração para verificar métricas HTTP após chamadas REST.
 * Valida que métricas de requisição são registradas corretamente.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HttpRequestMetricsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MeterRegistry meterRegistry;

    @Test
    void shouldRecordHttpRequestMetrics() throws Exception {
        // Realiza requisição HTTP para gerar métrica
        mockMvc.perform(get("/health/ready"))
            .andExpect(status().isOk());

        // Verifica que a métrica foi registrada
        Timer timer = meterRegistry.find("http.server.requests")
            .tag("uri", "/health/ready")
            .tag("method", "GET")
            .tag("status", "200")
            .timer();

        assertThat(timer).isNotNull();
        assertThat(timer.count()).isGreaterThanOrEqualTo(1L);
    }

    @Test
    void shouldRecordHttpRequestDuration() throws Exception {
        mockMvc.perform(get("/health/live"))
            .andExpect(status().isOk());

        Timer timer = meterRegistry.find("http.server.requests")
            .tag("uri", "/health/live")
            .timer();

        assertThat(timer).isNotNull();
        assertThat(timer.totalTime(java.util.concurrent.TimeUnit.MILLISECONDS)).isGreaterThan(0.0);
    }

    @Test
    void shouldIncludeOutcomeTag() throws Exception {
        mockMvc.perform(get("/health/ready"))
            .andExpect(status().isOk());

        Timer timer = meterRegistry.find("http.server.requests")
            .tag("outcome", "SUCCESS")
            .timer();

        assertThat(timer).isNotNull();
    }

    @Test
    void shouldIncludeApplicationTags() throws Exception {
        mockMvc.perform(get("/health/live"))
            .andExpect(status().isOk());

        Timer timer = meterRegistry.find("http.server.requests")
            .tag("application", "payment-api")
            .timer();

        assertThat(timer).isNotNull();
    }

    @Test
    void shouldRecordNotFoundRequests() throws Exception {
        // Requisição a endpoint inexistente retorna 401 (unauthorized) porque não está na whitelist
        // Usando o 401 em vez do 404 para testar o CLIENT_ERROR
        mockMvc.perform(get("/nonexistent"))
            .andExpect(status().isUnauthorized());

        Timer timer = meterRegistry.find("http.server.requests")
            .tag("status", "401")
            .tag("outcome", "CLIENT_ERROR")
            .timer();

        assertThat(timer).isNotNull();
        assertThat(timer.count()).isGreaterThanOrEqualTo(1L);
    }

    @Test
    void shouldDifferentiateEndpoints() throws Exception {
        mockMvc.perform(get("/health/live"));
        mockMvc.perform(get("/health/ready"));

        Timer livenessTimer = meterRegistry.find("http.server.requests")
            .tag("uri", "/health/live")
            .timer();

        Timer readinessTimer = meterRegistry.find("http.server.requests")
            .tag("uri", "/health/ready")
            .timer();

        assertThat(livenessTimer).isNotNull();
        assertThat(readinessTimer).isNotNull();
        assertThat(livenessTimer).isNotEqualTo(readinessTimer);
    }
}
