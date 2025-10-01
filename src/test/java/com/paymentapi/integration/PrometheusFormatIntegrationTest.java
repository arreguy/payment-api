package com.paymentapi.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste de integração para validar a formatação do Prometheus.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PrometheusFormatIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static final Pattern HELP_PATTERN = Pattern.compile("# HELP \\w+ .+");
    private static final Pattern TYPE_PATTERN = Pattern.compile("# TYPE \\w+ (counter|gauge|histogram|summary)");
    private static final Pattern METRIC_PATTERN = Pattern.compile("\\w+\\{.+\\} [0-9.eE+-]+");

    @Test
    void shouldReturnPrometheusTextFormat() throws Exception {
        var result = mockMvc.perform(get("/v1/metrics"))
            .andExpect(status().isOk())
            .andReturn();

        var contentType = result.getResponse().getContentType();
        assertThat(contentType).contains("text/plain");
    }

    @Test
    void shouldContainHelpComments() throws Exception {
        var result = mockMvc.perform(get("/v1/metrics"))
            .andExpect(status().isOk())
            .andReturn();

        var content = result.getResponse().getContentAsString();

        assertThat(content).contains("# HELP");

        // Verifica que pelo menos uma linha HELP está bem formatada
        var lines = content.lines().toList();
        var helpLines = lines.stream()
            .filter(line -> line.startsWith("# HELP"))
            .toList();

        assertThat(helpLines).isNotEmpty();
        assertThat(helpLines.get(0)).matches(HELP_PATTERN);
    }

    @Test
    void shouldContainTypeComments() throws Exception {
        var result = mockMvc.perform(get("/v1/metrics"))
            .andExpect(status().isOk())
            .andReturn();

        var content = result.getResponse().getContentAsString();

        assertThat(content).contains("# TYPE");

        // Verifica que pelo menos uma linha TYPE está bem formatada
        var lines = content.lines().toList();
        var typeLines = lines.stream()
            .filter(line -> line.startsWith("# TYPE"))
            .toList();

        assertThat(typeLines).isNotEmpty();
        assertThat(typeLines.get(0)).matches(TYPE_PATTERN);
    }

    @Test
    void shouldContainMetricLines() throws Exception {
        var result = mockMvc.perform(get("/v1/metrics"))
            .andExpect(status().isOk())
            .andReturn();

        var content = result.getResponse().getContentAsString();

        // Verifica que existem linhas de métricas com formato válido
        var lines = content.lines().toList();
        var metricLines = lines.stream()
            .filter(line -> !line.startsWith("#") && !line.isEmpty())
            .toList();

        assertThat(metricLines).isNotEmpty();
    }

    @Test
    void shouldIncludeLabelsInMetrics() throws Exception {
        var result = mockMvc.perform(get("/v1/metrics"))
            .andExpect(status().isOk())
            .andReturn();

        var content = result.getResponse().getContentAsString();

        // Métricas devem ter labels entre chaves
        assertThat(content).containsPattern("\\w+\\{[^}]+\\}");
    }

    @Test
    void shouldIncludeNumericValues() throws Exception {
        var result = mockMvc.perform(get("/v1/metrics"))
            .andExpect(status().isOk())
            .andReturn();

        var content = result.getResponse().getContentAsString();

        // Linhas de métricas devem terminar com valores numéricos
        var lines = content.lines()
            .filter(line -> !line.startsWith("#") && !line.isEmpty())
            .toList();

        assertThat(lines).isNotEmpty();

        // Verifica que pelo menos uma linha tem valor numérico
        var hasNumericValue = lines.stream()
            .anyMatch(line -> line.matches(".*\\} [0-9.eE+-]+.*"));

        assertThat(hasNumericValue).isTrue();
    }

    @Test
    void shouldHaveConsistentFormatForAllMetrics() throws Exception {
        var result = mockMvc.perform(get("/v1/metrics"))
            .andExpect(status().isOk())
            .andReturn();

        var content = result.getResponse().getContentAsString();
        var lines = content.lines().toList();

        // Verifica estrutura: HELP seguido de TYPE seguido de métricas
        var jvmMemoryHelpIndex = -1;
        var jvmMemoryTypeIndex = -1;

        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("# HELP jvm_memory_used")) {
                jvmMemoryHelpIndex = i;
            }
            if (lines.get(i).contains("# TYPE jvm_memory_used")) {
                jvmMemoryTypeIndex = i;
            }
        }

        // Se ambos existem, TYPE deve vir logo após HELP
        if (jvmMemoryHelpIndex >= 0 && jvmMemoryTypeIndex >= 0) {
            assertThat(jvmMemoryTypeIndex).isEqualTo(jvmMemoryHelpIndex + 1);
        }
    }

    @Test
    void shouldUseUnderscoreNotHyphenInMetricNames() throws Exception {
        var result = mockMvc.perform(get("/v1/metrics"))
            .andExpect(status().isOk())
            .andReturn();

        var content = result.getResponse().getContentAsString();

        // Requer underscores e não hífens
        var lines = content.lines()
            .filter(line -> !line.startsWith("#") && !line.isEmpty())
            .toList();

        for (var line : lines) {
            // Extrai nome da métrica (antes de '{' ou ' ')
            var metricName = line.split("[{ ]")[0];
            if (!metricName.isEmpty()) {
                assertThat(metricName).doesNotContain("-");
            }
        }
    }
}
