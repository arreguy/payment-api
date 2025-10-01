package com.paymentapi.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes para MetricsService.
 * Verifica criação e registro de métricas customizadas.
 */
@SpringBootTest
@ActiveProfiles("test")
class MetricsServiceTest {

    @Autowired
    private MetricsService metricsService;

    @Autowired
    private MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        meterRegistry.clear();
    }

    @Test
    void shouldIncrementCounter() {
        String metricName = "test_counter_total";
        Map<String, String> tags = Map.of("service", "test-service", "status", "success");

        metricsService.incrementCounter(metricName, tags);

        Counter counter = meterRegistry.find(metricName)
            .tag("service", "test-service")
            .tag("status", "success")
            .counter();

        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    void shouldIncrementCounterMultipleTimes() {
        String metricName = "test_counter_multiple";
        Map<String, String> tags = Map.of("service", "test-service");

        metricsService.incrementCounter(metricName, tags);
        metricsService.incrementCounter(metricName, tags);
        metricsService.incrementCounter(metricName, tags);

        Counter counter = meterRegistry.find(metricName)
            .tag("service", "test-service")
            .counter();

        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(3.0);
    }

    @Test
    void shouldRecordTiming() {
        String metricName = "test_duration_seconds";
        Duration duration = Duration.ofMillis(500);
        Map<String, String> tags = Map.of("service", "test-service", "operation", "test-op");

        metricsService.recordTiming(metricName, duration, tags);

        Timer timer = meterRegistry.find(metricName)
            .tag("service", "test-service")
            .tag("operation", "test-op")
            .timer();

        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1L);
        assertThat(timer.totalTime(java.util.concurrent.TimeUnit.MILLISECONDS)).isGreaterThanOrEqualTo(500.0);
    }

    @Test
    void shouldStartAndStopTimer() {
        String metricName = "test_timer_sample";
        Map<String, String> tags = Map.of("service", "test-service");

        Timer.Sample sample = metricsService.startTimer();

        // Simula operação com algum tempo
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        metricsService.stopTimer(sample, metricName, tags);

        Timer timer = meterRegistry.find(metricName)
            .tag("service", "test-service")
            .timer();

        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1L);
        assertThat(timer.totalTime(java.util.concurrent.TimeUnit.MILLISECONDS)).isGreaterThanOrEqualTo(100.0);
    }

    @Test
    void shouldCreateSeparateCountersForDifferentTags() {
        String metricName = "test_counter_tags";

        metricsService.incrementCounter(metricName, Map.of("status", "success"));
        metricsService.incrementCounter(metricName, Map.of("status", "failure"));

        Counter successCounter = meterRegistry.find(metricName)
            .tag("status", "success")
            .counter();

        Counter failureCounter = meterRegistry.find(metricName)
            .tag("status", "failure")
            .counter();

        assertThat(successCounter).isNotNull();
        assertThat(successCounter.count()).isEqualTo(1.0);

        assertThat(failureCounter).isNotNull();
        assertThat(failureCounter.count()).isEqualTo(1.0);
    }

    @Test
    void shouldHandleEmptyTags() {
        String metricName = "test_counter_no_tags";

        metricsService.incrementCounter(metricName, Map.of());

        Counter counter = meterRegistry.find(metricName).counter();

        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }
}
