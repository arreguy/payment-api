package com.paymentapi.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes para MetricsConfig.
 * Verifica configuração do Prometheus registry e tags comuns.
 */
@SpringBootTest
@ActiveProfiles("test")
class MetricsConfigTest {

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private PrometheusMeterRegistry prometheusMeterRegistry;

    @Test
    void shouldConfigurePrometheusMeterRegistry() {
        assertThat(prometheusMeterRegistry).isNotNull();
        assertThat(meterRegistry).isInstanceOf(PrometheusMeterRegistry.class);
    }

    @Test
    void shouldConfigureCommonTags() {
        // Verifica que as tags comuns estão configuradas através de um contador de teste
        var counter = meterRegistry.counter("test.counter");
        var tags = convertIterableToList(counter.getId().getTags());

        assertThat(tags)
            .extracting(Tag::getKey)
            .contains("application", "environment", "instance");
    }

    @Test
    void shouldHaveApplicationTag() {
        var counter = meterRegistry.counter("test.application.tag");
        var tags = convertIterableToList(counter.getId().getTags());
        var applicationTag = tags.stream()
            .filter(tag -> tag.getKey().equals("application"))
            .findFirst();

        assertThat(applicationTag).isPresent();
        assertThat(applicationTag.get().getValue()).isEqualTo("payment-api");
    }

    @Test
    void shouldHaveEnvironmentTag() {
        var counter = meterRegistry.counter("test.environment.tag");
        var tags = convertIterableToList(counter.getId().getTags());
        var environmentTag = tags.stream()
            .filter(tag -> tag.getKey().equals("environment"))
            .findFirst();

        assertThat(environmentTag).isPresent();
        // Environment pode ser "test" ou "dev" dependendo da configuração
        assertThat(environmentTag.get().getValue()).isIn("test", "dev");
    }

    @Test
    void shouldHaveInstanceTag() {
        var counter = meterRegistry.counter("test.instance.tag");
        var tags = convertIterableToList(counter.getId().getTags());
        var instanceTag = tags.stream()
            .filter(tag -> tag.getKey().equals("instance"))
            .findFirst();

        assertThat(instanceTag).isPresent();
        assertThat(instanceTag.get().getValue()).isNotNull();
    }

    private List<Tag> convertIterableToList(Iterable<Tag> iterable) {
        List<Tag> list = new ArrayList<>();
        iterable.forEach(list::add);
        return list;
    }
}
