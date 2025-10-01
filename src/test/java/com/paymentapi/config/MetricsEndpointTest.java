package com.paymentapi.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de integração para o endpoint /v1/metrics.
 * Verifica acessibilidade, formatação do Prometheus e conteúdo básico.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MetricsEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldExposeMetricsEndpoint() throws Exception {
        mockMvc.perform(get("/v1/metrics"))
            .andExpect(status().isOk());
    }

    @Test
    void shouldReturnPrometheusTextFormat() throws Exception {
        mockMvc.perform(get("/v1/metrics"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith("text/plain"));
    }

    @Test
    void shouldContainJvmMetrics() throws Exception {
        var result = mockMvc.perform(get("/v1/metrics"))
            .andExpect(status().isOk())
            .andReturn();

        var content = result.getResponse().getContentAsString();

        // Verifica presença das métricas básicas da JVM
        assertThat(content).contains("jvm_memory", "jvm_threads", "system_cpu");
    }

    @Test
    void shouldContainHikariMetrics() throws Exception {
        var result = mockMvc.perform(get("/v1/metrics"))
            .andExpect(status().isOk())
            .andReturn();

        var content = result.getResponse().getContentAsString();

        // Verifica presença das métricas do HikariCP
        assertThat(content).containsAnyOf("hikari", "HikariCP");
    }

    @Test
    void shouldContainApplicationTags() throws Exception {
        var result = mockMvc.perform(get("/v1/metrics"))
            .andExpect(status().isOk())
            .andReturn();

        var content = result.getResponse().getContentAsString();

        // Verifica que as tags comuns estão presentes nas métricas
        assertThat(content).contains("application=\"payment-api\"");
        // "Dev ou "test"
        assertThat(content).contains("environment=\"");
        assertThat(content).containsAnyOf("dev", "test");
    }

    @Test
    void shouldContainHelpAndTypeComments() throws Exception {
        var result = mockMvc.perform(get("/v1/metrics"))
            .andExpect(status().isOk())
            .andReturn();

        var content = result.getResponse().getContentAsString();

        // Verifica formatação do Prometheus
        assertThat(content).contains("# HELP", "# TYPE");
    }

    @Test
    void shouldAllowUnauthenticatedAccess() throws Exception {
        // Verifica que o endpoint é acessível sem autenticação
        mockMvc.perform(get("/v1/metrics"))
            .andExpect(status().isOk());
    }
}
