package com.paymentapi.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.observation.DefaultMeterObservationHandler;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuração do Micrometer com registro Prometheus para a coleta de métricas.
 * Expõe métricas no endpoint /v1/metrics.
 */
@Configuration
public class MetricsConfig {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    /**
     * Configura o registro Prometheus para exposição de métricas.
     * O registro é criado com configuração padrão do Prometheus.
     */
    @Bean
    public PrometheusMeterRegistry prometheusMeterRegistry() {
        return new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    }

    /**
     * Customiza o MeterRegistry para adicionar tags comuns a todas as métricas.
     * Tags incluem: application, environment e instance.
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        String instanceId = System.getenv("HOSTNAME") != null
            ? System.getenv("HOSTNAME")
            : "localhost";

        return registry -> registry.config()
            .commonTags(List.of(
                Tag.of("application", applicationName),
                Tag.of("environment", activeProfile),
                Tag.of("instance", instanceId)
            ));
    }

    /**
     * Configura filtros de métricas para excluir métricas sensíveis.
     * Remove métricas que podem explodir a cardinality
     */
    @Bean
    public MeterFilter metricsFilter() {
        return MeterFilter.denyNameStartsWith("jvm.buffer");
    }

    /**
     * Configura o ObservationRegistry com handlers de observação.
     * Isso permite configurar percentis e histogramas de forma programática.
     */
    @Bean
    public ObservationRegistry observationRegistry(MeterRegistry meterRegistry) {
        ObservationRegistry registry = ObservationRegistry.create();

        // Configura o handler de métricas com percentis personalizados
        DefaultMeterObservationHandler handler = new DefaultMeterObservationHandler(meterRegistry);
        registry.observationConfig().observationHandler(handler);

        return registry;
    }

    /**
     * Habilita o suporte a anotações @Observed para observabilidade.
     */
    @Bean
    public ObservedAspect observedAspect(ObservationRegistry registry) {
        return new ObservedAspect(registry);
    }
}
