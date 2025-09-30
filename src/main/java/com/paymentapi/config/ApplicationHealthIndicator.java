package com.paymentapi.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Indicador de saúde da aplicação.
 * Mostra informações sobre o status da aplicação, memória, tempo de atividade, etc.
 */
@Component("application")
public class ApplicationHealthIndicator implements HealthIndicator {

    // Começo da aplicação
    private final Instant startTime;

    public ApplicationHealthIndicator() {
        // Horário que a JVM foi iniciada
        this.startTime = Instant.ofEpochMilli(ManagementFactory.getRuntimeMXBean().getStartTime());
    }

    /**
     * Verifica a saúde da aplicação
     */
    @Override
    public Health health() {
        try {
            // Informações de memória
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            long uptime = ManagementFactory.getRuntimeMXBean().getUptime();

            // Monta a resposta com as informações
            return Health.up()
                .withDetail("service", "payment-api")
                .withDetail("version", "1.0.0")
                .withDetail("status", "running")
                .withDetail("startTime", LocalDateTime.ofInstant(startTime, ZoneOffset.UTC).toString())
                .withDetail("uptime", formatUptime(uptime))
                .withDetail("memoryUsed", formatBytes(memoryBean.getHeapMemoryUsage().getUsed()))
                .withDetail("memoryMax", formatBytes(memoryBean.getHeapMemoryUsage().getMax()))
                .build();

        } catch (Exception e) {
            // Se der erro, retorna DOWN
            return Health.down()
                .withDetail("service", "payment-api")
                .withDetail("error", e.getMessage())
                .build();
        }
    }

    /**
     * Formata o tempo de atividade (uptime) de forma legível
     * Exemplo: 2d 5h 30m
     */
    private String formatUptime(long uptimeMs) {
        // Converte milissegundos pra segundos, minutos, horas e dias
        long seconds = uptimeMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return String.format("%dd %dh %dm", days, hours % 24, minutes % 60);
        } else if (hours > 0) {
            return String.format("%dh %dm", hours, minutes % 60);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds % 60);
        } else {
            return String.format("%ds", seconds);
        }
    }

    /**
     * Formata bytes pra formato legível (KB, MB, GB, etc)
     * Exemplo: 1536 -> 1.5 KB
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}