package com.paymentapi.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;

/**
 * Serviço para gerenciamento das métricas customizadas da aplicação.
 * Fornece abstração para criação e registro de contadores e timers seguindo as convenções do Prometheus.
 */
@Service
public class MetricsService {

    private final MeterRegistry meterRegistry;

    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Incrementa um contador com tags personalizadas.
     *
     * @param metricName Nome da métrica
     * @param tags Tags adicionais para dimensionar a métrica
     */
    public void incrementCounter(String metricName, Map<String, String> tags) {
        meterRegistry.counter(metricName, convertTags(tags))
            .increment();
    }

    /**
     * Registra o tempo de execução de uma operação.
     *
     * @param metricName Nome da métrica de timer
     * @param duration Duração da operação
     * @param tags Tags adicionais para dimensionar a métrica
     */
    public void recordTiming(String metricName, Duration duration, Map<String, String> tags) {
        meterRegistry.timer(metricName, convertTags(tags))
            .record(duration);
    }

    /**
     * Cria um timer e retorna uma amostra para medição.
     * Uso: var sample = startTimer(); ... stopTimer(sample, "metric_name", tags);
     *
     * @return Sample para medição de tempo
     */
    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }

    /**
     * Para o timer iniciado e registra a métrica.
     *
     * @param sample Amostra retornada por startTimer()
     * @param metricName Nome da métrica de timer
     * @param tags Tags adicionais para dimensionar a métrica
     */
    public void stopTimer(Timer.Sample sample, String metricName, Map<String, String> tags) {
        sample.stop(meterRegistry.timer(metricName, convertTags(tags)));
    }

    /**
     * Converte Map de tags em array de strings no formato esperado pelo Micrometer.
     */
    private String[] convertTags(Map<String, String> tags) {
        return tags.entrySet().stream()
            .flatMap(entry -> java.util.stream.Stream.of(entry.getKey(), entry.getValue()))
            .toArray(String[]::new);
    }
}
